package org.neopixel

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent._
import akka.cluster.MemberStatus.Up
import neopixel.{rpi_ws281xConstants => wsC}

object ClusterStatusTracker {

  // LED strip configuration:
  val LED_COUNT = 8 // Number of LED pixels.
  val LED_PIN = 18 // GPIO pin connected to the pixels (must support PWM!).
  val LED_FREQ_HZ = 800000 // LED signal frequency in hertz (usually 800khz)
  val LED_DMA = 5 // DMA channel to use for generating signal (try 5)
  val LED_BRIGHTNESS = 2.toShort // Set to 0 for darkest and 255 for brightest
  val LED_INVERT = false // True to invert the signal (when using NPN transistor level shift)
  val LED_CHANNEL = 0
  val LED_STRIP: Int = wsC.WS2811_STRIP_RGB

  val HighestLedIndex = LED_COUNT - 1

  val LeaderLedNumber = HighestLedIndex - 5
  val HeartbeatLedNumber = HighestLedIndex - 7

  def resetAllLeds(strip: Adafruit_NeoPixel.type): Unit = {
    for {
      pixel <- 0 until strip.numPixels()
    } strip.setPixelColor(pixel, Black)
    strip.show()
  }

  val HostToLedMapping =
    Map(
      "kubernetes" -> 7,
      "node-0" -> 7,
      "192.168.0.101" -> 7,
      "node-1" -> 6,
      "192.168.0.102" -> 6,
      "node-2" -> 5,
      "192.168.0.103" -> 5,
      "node-3" -> 4,
      "192.168.0.104" -> 4,
      "node-4" -> 3,
      "192.168.0.105" -> 3
    )

  case object Heartbeat

  def props(): Props = Props(new ClusterStatusTracker)
}

class ClusterStatusTracker extends Actor with ActorLogging with SettingsActor {
  import ClusterStatusTracker._
  import scala.concurrent.ExecutionContext.Implicits.global

  private val thisHost = context.system.settings.config.getString("akka.remote.netty.tcp.hostname")
  log.debug(s"Starting ClusterStatus Actor on $thisHost")

  import scala.concurrent.duration._
  import settings._

  override def receive: Receive = idle

  def idle: Receive = akka.actor.Actor.emptyBehavior

  def running(strip: Adafruit_NeoPixel.type, hearbeatLEDOn: Boolean): Receive = {
    case Heartbeat if hearbeatLEDOn =>
      setPixelColorAndShow(strip, HeartbeatLedNumber, Black)
      context.become(running(strip, hearbeatLEDOn = false))

    case Heartbeat =>
      setPixelColorAndShow(strip, HeartbeatLedNumber, heartbeartIndicatorColor)
      context.become(running(strip, hearbeatLEDOn = true))

    case msg @ MemberUp(member) =>
      setPixelColorAndShow(strip, HostToLedMapping(member.address.host.get), nodeUpColor)
      log.debug(s"$msg")

    case msg @ MemberLeft(member) =>
      setPixelColorAndShow(strip, HostToLedMapping(member.address.host.get), nodeLeftColor)
      log.debug(s"$msg")

    case msg @ MemberExited(member) =>
      setPixelColorAndShow(strip, HostToLedMapping(member.address.host.get), nodeExitedColor)
      log.debug(s"$msg")

    case msg @ MemberJoined(member) =>
      setPixelColorAndShow(strip, HostToLedMapping(member.address.host.get), nodeJoinedColor)
      log.debug(s"$msg")

    case msg @ MemberRemoved(member, previousStatus) =>
      setPixelColorAndShow(strip, HostToLedMapping(member.address.host.get), nodeDownColor)
      log.debug(s"$msg")

    case msg @ MemberWeaklyUp(member) =>
      setPixelColorAndShow(strip, HostToLedMapping(member.address.host.get), nodeWeaklyUpColor)
      log.debug(s"$msg")

    case msg @ ReachableMember(member) if member.status == Up =>
      setPixelColorAndShow(strip, HostToLedMapping(member.address.host.get), nodeUpColor)
      log.debug(s"$msg")

    case msg @ UnreachableMember(member) =>
      setPixelColorAndShow(strip, HostToLedMapping(member.address.host.get), nodeUnreachableColor)
      log.debug(s"$msg")

    case msg @ LeaderChanged(Some(leader)) if leader.host.getOrElse("") == thisHost =>
      setPixelColorAndShow(strip, LeaderLedNumber, leaderIndicatorColor)
      log.debug(s"$msg")

    case msg @ LeaderChanged(Some(leader)) =>
      setPixelColorAndShow(strip, LeaderLedNumber, Black)
      log.debug(s"$msg")

    case msg @ LeaderChanged(None) =>
      setPixelColorAndShow(strip, LeaderLedNumber, Black)
      log.debug(s"$msg")

    case event =>
      log.debug(s"~~~> UNHANDLED CLUSTER DOMAIN EVENT: $event")

  }

  override def preStart(): Unit = {
    val strip = Adafruit_NeoPixel(LED_COUNT, LED_PIN, LED_FREQ_HZ, LED_DMA, LED_INVERT, LED_BRIGHTNESS, LED_CHANNEL, LED_STRIP)
    strip.begin()
    resetAllLeds(strip)
    Cluster(context.system)
      .subscribe(self,
        InitialStateAsEvents,
        classOf[LeaderChanged],
        classOf[ReachabilityEvent],
        classOf[MemberEvent]
      )
    context.system.scheduler.schedule(0.millis, heartbeatIndicatorInterval, self, Heartbeat)
    context.become(running(strip, hearbeatLEDOn = false))

  }

  override def postStop(): Unit = {
    Cluster(context.system).unsubscribe(self)
  }

  private def setPixelColorAndShow(strip: Adafruit_NeoPixel.type ,
                                   ledNumber: Int,
                                   ledColor: Long): Unit = {
    strip.setPixelColor(ledNumber, ledColor)
    strip.show()
  }
}
