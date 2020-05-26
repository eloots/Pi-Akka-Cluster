package com.lightbend.akka_oled

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.ShardRegion.CurrentShardRegionState
import akka.cluster.sharding.typed.{ClusterShardingQuery, GetShardRegionState}
import akka.util.Timeout
import com.lightbend.akka_oled.OledShardingVisualizer.ShardRegionState
import scala.concurrent.duration._

object ShardStateTracker {

  implicit val timeout: Timeout = 6.seconds

  def apply(visualizer: ActorRef[OledShardingVisualizer.Command]): Behavior[CurrentShardRegionState] = Behaviors.setup { context =>
    Behaviors.receiveMessage {
      message: CurrentShardRegionState =>
        visualizer.tell(ShardRegionState(message.shards))
        Behaviors.same
    }
  }
}

object ShardStateScheduler {

  implicit val timeout: Timeout = 6.seconds

  case class Tick()

  def apply(shardState: ActorRef[ClusterShardingQuery],
             shardTracker: ActorRef[CurrentShardRegionState]): Behavior[Tick] =
    Behaviors.withTimers { timer =>
      timer.startTimerAtFixedRate(Tick(), 1.second)
      Behaviors.receiveMessage { _: Tick =>
          shardState ! GetShardRegionState(ClientEntity.TypeKey, shardTracker)
          Behaviors.same
      }

  }
}