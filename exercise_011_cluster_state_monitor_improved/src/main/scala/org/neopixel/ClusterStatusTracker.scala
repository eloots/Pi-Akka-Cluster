package org.neopixel

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent._
import akka.cluster.MemberStatus.Up
import akka.event.LoggingAdapter
import neopixel.{rpi_ws281xConstants => wsC}

object ClusterStatusTracker {

  case object SendUpdatedStatus
  case class NodeUpdate(updates: List[Long])

  // LED strip configuration:
  val LED_COUNT = 8 // Number of LED pixels.
  val LED_PIN = 18 // GPIO pin connected to the pixels (must support PWM!).
  val LED_FREQ_HZ = 800000 // LED signal frequency in hertz (usually 800khz)
  val LED_DMA = 5 // DMA channel to use for generating signal (try 5)
  val LED_BRIGHTNESS = 10.toShort // Set to 0 for darkest and 255 for brightest
  val LED_INVERT = false // True to invert the signal (when using NPN transistor level shift)
  val LED_CHANNEL = 0
  val LED_STRIP: Int = wsC.WS2811_STRIP_RGB

  val HighestLedIndex = LED_COUNT - 1

  val LED_Black = color(0, 0, 0)
  val LED_Green = color(255, 0, 0)
  val LED_DarkGreen = color(100, 0, 0)
  val LED_Red = color(0, 255, 0)
  val LED_Blue = color(0, 0, 255)
  val LED_DarkBlue = color(0, 0, 100)
  val LED_Yellow = color(255, 255, 0)
  val LED_Cyan = color(255, 0, 255)
  val LED_Magenta = color(0, 255, 255)
  val LED_White = color(255, 255, 0)
  val LED_WhiteLow = color(100, 100, 100)
  val LeaderLedNumber = HighestLedIndex - 5

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

  def props(clusterNodeAddress: InetSocketAddress, ledStatusIndicatorAddress: InetSocketAddress): Props =
    Props(new ClusterStatusTracker(clusterNodeAddress, ledStatusIndicatorAddress))
}

class ClusterStatusTracker(clusterNodeAddress: InetSocketAddress, ledStatusIndicatorAddress: InetSocketAddress) extends Actor with ActorLogging {
  import ClusterStatusTracker._

  private val thisHost = context.system.settings.config.getString("akka.remote.netty.tcp.hostname")
  log.debug(s"Starting ClusterStatus Actor on $thisHost")

  private val nodeState = Array.fill(8)(0L)

  override def receive: Receive = idle

  def idle: Receive = akka.actor.Actor.emptyBehavior

  def running(clusterStatusLEDRequestOutChannel: ActorRef): Receive = {
    case SendUpdatedStatus => clusterStatusLEDRequestOutChannel ! NodeUpdate(nodeState.toList)

    case MemberUp(member) =>
      nodeState.update(HostToLedMapping(member.address.host.get), LED_Green)
//      log.debug(s"~~~> Member Up: $member")

    case MemberLeft(member) =>
      nodeState.update(HostToLedMapping(member.address.host.get), LED_Cyan)
//      log.debug(s"~~~> Member Left: $member")

    case MemberExited(member) =>
      nodeState.update(HostToLedMapping(member.address.host.get), LED_Magenta)
//      log.debug(s"~~~> Member Exited: $member")

    case MemberJoined(member) =>
      nodeState.update(HostToLedMapping(member.address.host.get), LED_Yellow)
//      log.debug(s"~~~> Member Joined: $member")

    case MemberRemoved(member, previousStatus) =>
      nodeState.update(HostToLedMapping(member.address.host.get), LED_Red)
//      log.debug(s"~~~> Member Removed: $member with previous status: $previousStatus")

    case MemberWeaklyUp(member) =>
      nodeState.update(HostToLedMapping(member.address.host.get), LED_DarkBlue)
//      log.debug(s"~~~> Member Weakly Up: $member")

    case ReachableMember(member) if member.status == Up =>
      nodeState.update(HostToLedMapping(member.address.host.get), LED_Green)
//      log.debug(s"~~~> Member Reachable: $member")

    case UnreachableMember(member) =>
      nodeState.update(HostToLedMapping(member.address.host.get), LED_WhiteLow)
//      log.debug(s"~~~> Member Unreachable: $member")

    case LeaderChanged(Some(leader)) if leader.host.getOrElse("") == thisHost =>
      nodeState.update(LeaderLedNumber, LED_Cyan)
//      log.debug(s"~~~> Leader Changed: $leader")

    case LeaderChanged(Some(leader)) =>
      nodeState.update(LeaderLedNumber, LED_Black)
//      log.debug(s"~~~> Leader Changed: $leader")

    case LeaderChanged(None) =>
      nodeState.update(LeaderLedNumber, LED_Black)

    case event =>
      log.debug(s"~~~> UNHANDLED CLUSTER DOMAIN EVENT: $event")
  }

  override def preStart(): Unit = {
    Cluster(context.system)
      .subscribe(
        self,
        InitialStateAsEvents,
        classOf[LeaderChanged],
        classOf[ReachabilityEvent],
        classOf[MemberEvent]
      )

    val clusterStatusLEDRequestInChannel =
      context.actorOf(ClusterStatusLEDRequestInChannel.props(clusterNodeAddress), "cluster-status-request-in-channel")
    val clusterStatusLEDRequestOutChannel =
      context.actorOf(ClusterStatusLEDRequestOutChannel.props(ledStatusIndicatorAddress), "cluster-status-request-out-channel")

    context.become(running(clusterStatusLEDRequestOutChannel))
    super.preStart()
  }

  override def postStop(): Unit =
    Cluster(context.system).unsubscribe(self)

}
