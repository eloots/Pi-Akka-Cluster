package org.neopixel

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Timers}
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent._
import akka.cluster.MemberStatus.{Up, WeaklyUp}
import neopixel.{rpi_ws281xConstants => wsC}

object ClusterStatusTracker {

  // LED strip configuration:
  private val LED_COUNT = 8 // Number of LED pixels.
  private val LED_PIN = 18 // GPIO pin connected to the pixels (must support PWM!).
  private val LED_FREQ_HZ = 800000 // LED signal frequency in hertz (usually 800khz)
  private val LED_DMA = 5 // DMA channel to use for generating signal (try 5)
  private val LED_BRIGHTNESS = 10.toShort // Set to 0 for darkest and 255 for brightest
  private val LED_INVERT = false // True to invert the signal (when using NPN transistor level shift)
  private val LED_CHANNEL = 0
  private val LED_STRIP: Int = wsC.WS2811_STRIP_RGB

  private val HighestLedIndex = LED_COUNT - 1

  private val LeaderLedNumber = HighestLedIndex - 5
  private val HeartbeatLedNumber = HighestLedIndex - 7

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
  case object WeaklyUpBeat

  def props(): Props = Props(new ClusterStatusTracker)
}

class ClusterStatusTracker extends Actor with ActorLogging with SettingsActor with Timers {
  import ClusterStatusTracker._
  import scala.concurrent.ExecutionContext.Implicits.global

  private val thisHost = context.system.settings.config.getString("akka.remote.netty.tcp.hostname")
  log.debug(s"Starting ClusterStatus Actor on $thisHost")

  import scala.concurrent.duration._
  import settings._

  override def receive: Receive = idle

  def idle: Receive = akka.actor.Actor.emptyBehavior

  def running(strip: Adafruit_NeoPixel.type, heartbeatLEDOn: Boolean, weaklyUpIndicatorOn: Boolean, weaklyUpMembers: Set[Member]): Receive = {
    case Heartbeat if heartbeatLEDOn =>
      setPixelColorAndShow(strip, HeartbeatLedNumber, Black)

      context.become(running(strip, heartbeatLEDOn = false, weaklyUpIndicatorOn, weaklyUpMembers))

    case Heartbeat =>
      setPixelColorAndShow(strip, HeartbeatLedNumber, heartbeartIndicatorColor)

      context.become(running(strip, heartbeatLEDOn = true, weaklyUpIndicatorOn, weaklyUpMembers))

    case WeaklyUpBeat if weaklyUpIndicatorOn =>
      for {
        weaklyUpMember <- weaklyUpMembers
      } setPixelColorAndShow(strip, HostToLedMapping(weaklyUpMember.address.host.get), Black)

      context.become(running(strip, heartbeatLEDOn, ! weaklyUpIndicatorOn, weaklyUpMembers))

    case WeaklyUpBeat =>
      for {
        weaklyUpMember <- weaklyUpMembers
      } setPixelColorAndShow(strip, HostToLedMapping(weaklyUpMember.address.host.get), nodeWeaklyUpColor)

      context.become(running(strip, heartbeatLEDOn, ! weaklyUpIndicatorOn, weaklyUpMembers))

    case msg @ MemberUp(member) =>
      setPixelColorAndShow(strip, HostToLedMapping(member.address.host.get), nodeUpColor)

      log.info(s"$msg\n${weaklyUpMembers - member}")

      context.become(running(strip, heartbeatLEDOn, weaklyUpIndicatorOn, weaklyUpMembers - member))

    case msg @ MemberLeft(member) =>
      setPixelColorAndShow(strip, HostToLedMapping(member.address.host.get), nodeLeftColor)

      log.info(s"$msg\n${weaklyUpMembers - member}")

      context.become(running(strip, heartbeatLEDOn, weaklyUpIndicatorOn, weaklyUpMembers - member))

    case msg @ MemberExited(member) =>
      setPixelColorAndShow(strip, HostToLedMapping(member.address.host.get), nodeExitedColor)

      log.info(s"$msg\n${weaklyUpMembers - member}")

      context.become(running(strip, heartbeatLEDOn, weaklyUpIndicatorOn, weaklyUpMembers - member))

    case msg @ MemberJoined(member) =>
      setPixelColorAndShow(strip, HostToLedMapping(member.address.host.get), nodeJoinedColor)

      log.info(s"$msg\n${weaklyUpMembers - member}")

      context.become(running(strip, heartbeatLEDOn, weaklyUpIndicatorOn, weaklyUpMembers - member))

    case msg @ MemberRemoved(member, previousStatus) =>
      setPixelColorAndShow(strip, HostToLedMapping(member.address.host.get), nodeDownColor)

      log.info(s"$msg\n${weaklyUpMembers - member}")

      context.become(running(strip, heartbeatLEDOn, weaklyUpIndicatorOn, weaklyUpMembers - member))

    case msg @ MemberWeaklyUp(member) =>
      setPixelColorAndShow(strip, HostToLedMapping(member.address.host.get), nodeWeaklyUpColor)

      log.info(s"$msg\n${weaklyUpMembers + member}")

      context.become(running(strip, heartbeatLEDOn, weaklyUpIndicatorOn, weaklyUpMembers + member))

    case msg @ ReachableMember(member) if member.status == Up =>
      setPixelColorAndShow(strip, HostToLedMapping(member.address.host.get), nodeUpColor)

      log.info(s"$msg\n${weaklyUpMembers - member}")

      context.become(running(strip, heartbeatLEDOn, weaklyUpIndicatorOn, weaklyUpMembers - member))

    case msg @ ReachableMember(member) if member.status == WeaklyUp =>
      setPixelColorAndShow(strip, HostToLedMapping(member.address.host.get), nodeWeaklyUpColor)

      log.info(s"$msg\n${weaklyUpMembers + member }")

      context.become(running(strip, heartbeatLEDOn, weaklyUpIndicatorOn, weaklyUpMembers + member))

    case msg @ UnreachableMember(member) =>
      setPixelColorAndShow(strip, HostToLedMapping(member.address.host.get), nodeUnreachableColor)

      log.info(s"$msg\n${weaklyUpMembers - member}")

      context.become(running(strip, heartbeatLEDOn, weaklyUpIndicatorOn, weaklyUpMembers - member))

    case msg @ LeaderChanged(Some(leader)) if leader.host.getOrElse("") == thisHost =>
      setPixelColorAndShow(strip, LeaderLedNumber, leaderIndicatorColor)

      log.debug(s"$msg")

    case msg @ LeaderChanged(Some(leader)) =>
      setPixelColorAndShow(strip, LeaderLedNumber, Black)

      log.info(s"$msg")

    case msg @ LeaderChanged(None) =>
      setPixelColorAndShow(strip, LeaderLedNumber, Black)

      log.info(s"$msg")

    case event =>

      log.info(s"~~~> UNHANDLED CLUSTER DOMAIN EVENT: $event")

  }

  override def preStart(): Unit = {

    log.info(s"LED brightness = ${settings.ledBrightness}")

    val strip = Adafruit_NeoPixel(LED_COUNT, LED_PIN, LED_FREQ_HZ, LED_DMA, LED_INVERT, settings.ledBrightness, LED_CHANNEL, LED_STRIP)
    strip.begin()
    resetAllLeds(strip)

    Cluster(context.system)
      .subscribe(self,
        InitialStateAsEvents,
        classOf[LeaderChanged],
        classOf[ReachabilityEvent],
        classOf[MemberEvent]
      )
    timers.startPeriodicTimer("heartbeat-timer", Heartbeat, heartbeatIndicatorInterval)
    timers.startPeriodicTimer("weakly-up-beat", WeaklyUpBeat, weaklyUpIndicatorInterval)
    context.become(running(strip, heartbeatLEDOn = false, weaklyUpIndicatorOn = false, weaklyUpMembers = Set.empty[Member]))

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
