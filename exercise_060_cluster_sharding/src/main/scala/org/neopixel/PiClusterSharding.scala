package org.neopixel

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props, Terminated, Timers}
import akka.cluster.sharding.ClusterSharding
import akka.cluster.sharding.ShardRegion.MessageExtractor
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}

import scala.concurrent.duration._
import scala.util.Random

object PiClusterSharding {

  val ClusterSize = 5

  val DefaultEntitiesPerNode = 3

  // shards with two entities in them
  val Shards = (0 to 100).sliding(2, 2).toArray


  def props(strip: Adafruit_NeoPixel.type, logicalToPhysicalLEDMapping: Int => Int): Props =
    Props(new PiClusterSharding(strip, logicalToPhysicalLEDMapping))

  final case class Blink(id: Int)
  final case object GetEntityIndex
  final case class EntityIndex(index: Int)
  final case class EntityIndexChanged(newIndex: Int)
  final case class ActuateLED(index: Int, color: Long)
  final case object CompactationTick

}

class PiClusterSharding(strip: Adafruit_NeoPixel.type, logicalToPhysicalLEDMapping: Int => Int) extends Actor with Timers with ActorLogging {

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
        case Blink(id) =>
          Shards.indexWhere(_.contains(id)).toString
      }
    }
  )

  context.actorOf(ClusterSingletonManager.props(
    singletonProps = LEDEntityPinger.props(region, ClusterSize),
    terminationMessage = PoisonPill,
    settings = ClusterSingletonManagerSettings(context.system)
  ), "pinger")

  // the maximal size is the total amount of entities created
  val entities = Array.fill[ActorRef](ClusterSize * DefaultEntitiesPerNode)(ActorRef.noSender)

  timers.startPeriodicTimer(CompactationTick, CompactationTick, 1.second)

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
      if (index > -1) {
        entities(index) = ActorRef.noSender
      }
    case ActuateLED(index, color) =>
      // add a bit of delay before actuation to ensure that the strip will respond well
      Thread.sleep(20)
      setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(index), color)
    case CompactationTick =>
      slidingCompactation()
  }

  def slidingCompactation(): Unit = {
    val Empty = ActorRef.noSender

    // find all gaps and shift everyone until no gaps are left
    // explicitly don't compact at once to create a "sliding" effect
    val hasGaps = entities.zipWithIndex.sliding(3, 1).exists {
      case Array((_, _), (Empty, _), (led, _)) if led != Empty => true
      case Array((Empty, _), (led1, _), (led2, _)) if led1 != Empty || led2 != Empty => true
      case _ => false
    }

    if (hasGaps) {
      for (i <- entities.indices.dropRight(1)) {
        if (entities(i) == Empty) {
          entities(i) = entities(i + 1)
          entities(i + 1) = Empty
          if (entities(i) != Empty) entities(i) ! EntityIndexChanged(i)
        }
      }
      slidingCompactation()
    }
  }

  private def setPixelColorAndShow(strip: Adafruit_NeoPixel.type ,
                                   ledNumber: Int,
                                   ledColor: Long): Unit = {
    strip.setPixelColor(ledNumber, ledColor)
    strip.show()
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
  var entityColor = {
    val shardId = Shards.indexWhere(_.contains(self.path.name.toInt))
    colors(shardId % colors.size)
  }

  sharding ! GetEntityIndex

  def receive: Receive = {
    case EntityIndex(idx) if isDisplayable(idx) =>
      entityIndex = idx
      log.info("Entity {} started at index {} with color {}", self.path.name, entityIndex, entityColor)
      turnOn()
      context.become(ready)
    case EntityIndex(idx) =>
      entityIndex = idx
      log.info("Entity {} started at index {} but not displayed", self.path.name, entityIndex)
      context.become(ready)
  }

  def ready: Receive = {
    case Blink(_) if(isDisplayable(entityIndex)) =>
      turnOff()
      Thread.sleep(100)
      turnOn()
    case EntityIndexChanged(newIndex) if isDisplayable(newIndex) =>
      log.debug("Entity {} is now being displayed at index {}", self.path.name, newIndex)
      turnOff()
      entityIndex = newIndex
      turnOn()
    case EntityIndexChanged(newIndex) =>
      log.debug("Entity {} index changed to {} but not displayable", self.path.name, entityIndex)
      entityIndex = newIndex

  }

  private def turnOn(): Unit =
    sharding ! ActuateLED(entityIndex, entityColor)

  private def turnOff(): Unit =
    sharding ! ActuateLED(entityIndex, Black)

  override def postStop(): Unit = {
    if(isDisplayable(entityIndex)) {
      log.debug("Entity {} stopping and not displaying itself anymore", self.path.name)
      turnOff()
    } else {
      log.debug("Entity {} stopping, was not displayed", self.path.name)
    }
  }

  private def isDisplayable(idx: Int): Boolean = idx > -1 && idx < strip.numPixels() - 1


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
