package com.lightbend.akka_oled

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.ShardRegion.ShardState
import akkapi.cluster.OledDriver
import akkapi.cluster.OledDriver.UpdateView
import com.lightbend.akka_oled.OledShardingVisualizer.{Notification, ShardRegionState}

object OledShardingVisualizer {

  sealed trait Command

  case class ShardRegionState(shards: Set[ShardState]) extends Command

  case class Notification(name: String, total: Int) extends Command

  def apply(screenIndex: Int,
            oledDriver: ActorRef[OledDriver.Command]): Behavior[OledShardingVisualizer.Command] =
    Behaviors.setup { context =>
      Behaviors.withTimers[Command] { timer =>
        new OledShardingVisualizer(screenIndex, oledDriver).running(
          clients = Map.empty[String, Int],
          shardToClientName = Map.empty[String, Set[String]]
        )
      }
    }
}

class OledShardingVisualizer private(screenIndex: Int,
                                     oledDriver: ActorRef[OledDriver.Command]) {

  def running(clients: Map[String, Int],
              shardToClientName: Map[String, Set[String]]
             ): Behavior[OledShardingVisualizer.Command] = Behaviors
    .receiveMessage[OledShardingVisualizer.Command] {
      case Notification(name, total) =>
        val newClients = clients + (name -> total)
        oledDriver ! UpdateView(screenIndex, renderState(newClients, shardToClientName))
        running(newClients, shardToClientName)
      case ShardRegionState(shards: Set[ShardState]) =>
        val entityIds: Set[String] = shards.flatMap(_.entityIds)

        val newShardToClientName = shards.foldLeft(Map.empty[String, Set[String]]) {
          case (map, value) =>
            map + (value.shardId.toString -> value.entityIds.map(_.toString))
        }

        val withNewClients: Map[String, Int] =
          entityIds.foldLeft(clients)((map, a) => if (clients.get(a).isEmpty) map + (a -> 0) else map)
        //remove old shards
        val updatedClients = withNewClients.filter { case (k, _) => entityIds.contains(k) }

        oledDriver ! UpdateView(screenIndex, renderState(updatedClients, newShardToClientName))
        running(updatedClients, newShardToClientName)
    }

  private def renderState(clients: Map[String, Int],
                          shardToClientName: Map[String, Set[String]]): String = {
    if (clients.nonEmpty)
      shardToClientName.flatMap[String] {
        case (key, names) => names.map {
          name => "Shard#" + key + "->" + name + ": " + clients.getOrElse(name, 0)
        }
      }.mkString("\n")
    else
      "No data"
  }

}
