package org.neopixel

import akka.actor.{Actor, ActorIdentity, ActorLogging, Identify, Props, Timers}

import scala.concurrent.duration._

object ShardingCoordinatorTracker {

  case object AskForShardingCoordinator
  case object ShardingCoordinatorRunning
  case object NoShardingCoordinatorRunning

  def props: Props = Props(new ShardingCoordinatorTracker)
}

class ShardingCoordinatorTracker extends Actor with ActorLogging with Timers {

  import ShardingCoordinatorTracker._

  private val thisHost = context.system.settings.config.getString("akka.remote.netty.tcp.hostname")

  override def receive: Receive = noShardCoordinatorRunning

  def noShardCoordinatorRunning: Receive = {
    case AskForShardingCoordinator =>
      pollForCoordinator()

    case ActorIdentity(1, Some(coordinator)) =>
      context.parent ! ShardingCoordinatorRunning
      context.become(shardCoordinatorRunning)

    case ActorIdentity(1, None) =>
  }

  def shardCoordinatorRunning: Receive = {
    case AskForShardingCoordinator =>
      pollForCoordinator()

    case ActorIdentity(1, Some(coordinator)) =>

    case ActorIdentity(1, None) =>
      context.parent ! NoShardingCoordinatorRunning
      context.become(noShardCoordinatorRunning)
  }

  override def preStart(): Unit = {
    super.preStart()
    timers.startPeriodicTimer(self, AskForShardingCoordinator, 2.seconds)
  }

  def pollForCoordinator(): Unit = {
    val sel = context.actorSelection(s"akka.tcp://pi-cluster-0-system@${thisHost}:2550/system/sharding/ping-pongCoordinator/singleton/coordinator")
    sel ! Identify(1)
  }

}
