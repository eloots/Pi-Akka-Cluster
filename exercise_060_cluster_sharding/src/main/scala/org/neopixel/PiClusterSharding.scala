package org.neopixel

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props, Terminated, Timers}
import akka.cluster.sharding.ClusterSharding
import akka.cluster.sharding.ShardRegion.{HashCodeMessageExtractor, MessageExtractor}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}

import scala.concurrent.duration._
import scala.util.Random

object PiClusterSharding {

  val ClusterSize = 5

  val DefaultEntitiesPerNode = 3

  def props(strip: Adafruit_NeoPixel.type, logicalToPhysicalLEDMapping: Int => Int): Props =
    Props(new PiClusterSharding(strip, logicalToPhysicalLEDMapping))

  final case class Blink(id: Int)
  final case object GetEntityIndex
  final case class EntityIndex(index: Int)

}

class PiClusterSharding(strip: Adafruit_NeoPixel.type, logicalToPhysicalLEDMapping: Int => Int) extends Actor {

  import PiClusterSharding._

  val region: ActorRef = ClusterSharding(context.system).start(
    typeName = "led",
    entityProps = LEDEntity.props(self, strip, logicalToPhysicalLEDMapping),
    messageExtractor = new MessageExtractor {
      override def entityId(message: Any): String = message match {
        case Blink(id) => id.toString
      }

      override def entityMessage(message: Any): Any = message

      override def shardId(message: Any): String = message match {
        // one shard per entity
        case Blink(id) => id.toString
      }
    }
  )

  context.actorOf(ClusterSingletonManager.props(
    singletonProps = LEDEntityPinger.props(region, ClusterSize),
    terminationMessage = PoisonPill,
    settings = ClusterSingletonManagerSettings(context.system)
  ), "pinger")

  val entities = Array.fill[ActorRef](strip.numPixels() - 1)((ActorRef.noSender))

  def receive: Receive = {
    case GetEntityIndex =>
      val index = entities.indexOf(ActorRef.noSender)
      if (index >= 0) {
        entities(index) = sender()
        context.watch(sender())
      }
      sender() ! EntityIndex(index)
    case Terminated(ref) =>
      val index = entities.indexOf(ref)
      entities(index) = ActorRef.noSender
  }

}

object LEDEntity {
  def props(sharding: ActorRef, strip: Adafruit_NeoPixel.type, logicalToPhysicalLEDMapping: Int => Int): Props =
    Props(new LEDEntity(sharding, strip, logicalToPhysicalLEDMapping))
}

class LEDEntity(sharding: ActorRef, strip: Adafruit_NeoPixel.type, logicalToPhysicalLEDMapping: Int => Int) extends Actor with ActorLogging {

  import PiClusterSharding._

  val colors = List(Green, Cyan, DarkOrange, Magenta, DarkRed, Yellow, Blue, White, DarkBlue)

  var entityIndex = 0
  var entityColor = colors(self.path.name.toInt % colors.size)

  sharding ! GetEntityIndex

  def receive: Receive = {
    case EntityIndex(idx) if idx >= 0 =>
      entityIndex = idx
      log.info("Entity {} started at index {} with color {}", self.path.name, entityIndex, entityColor)
      setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(entityIndex), entityColor)
      context.become(ready)
  }

  def ready: Receive = {
    case Blink(_) if(entityIndex >= 0) =>
      setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(entityIndex), Black)
      Thread.sleep(150)
      setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(entityIndex), entityColor)
  }


  override def postStop(): Unit = {
    setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(entityIndex), Black)
  }

  private def setPixelColorAndShow(strip: Adafruit_NeoPixel.type ,
                                   ledNumber: Int,
                                   ledColor: Long): Unit = {
    strip.setPixelColor(ledNumber, ledColor)
    strip.show()
  }


}

object LEDEntityPinger {
  final case object Tick

  def props(region: ActorRef, clusterSize: Int) = Props(new LEDEntityPinger(region, clusterSize))
}

class LEDEntityPinger(region: ActorRef, clusterSize: Int) extends Actor with Timers {
  import LEDEntityPinger._
  import PiClusterSharding._

  timers.startPeriodicTimer(Tick, Tick, 2.seconds)

  def receive = {
    case Tick =>
      for(i <- 0 to (clusterSize * DefaultEntitiesPerNode)) {
        region ! Blink(i)
        // jitter
        Thread.sleep(Random.nextInt(50))
      }
  }
}
