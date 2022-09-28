/**
  * Copyright © Eric Loots 2022 - eric.loots@gmail.com
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package akkapi.cluster

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akkapi.cluster.ClusterStatusTracker.{IsLeader, IsNoLeader, NodeDown, NodeExiting, NodeJoining, NodeLeaving, NodeRemoved, NodeState, NodeUnreachable, NodeUp, NodeWeaklyUp, PiClusterSingletonNotRunning, PiClusterSingletonRunning}

object OledClusterVisualizer {

  def apply(screenNumber: Int,
            settings: Settings,
            oledDriver: ActorRef[OledDriver.Command]): Behavior[NodeState] =
    Behaviors.setup { context =>
      new OledClusterVisualizer(screenNumber, settings, oledDriver).running(
        nodes = Map.empty[Int, String],
        leader = None
      )
    }
}

class OledClusterVisualizer private(screenNumber: Int,
                                    settings: Settings,
                                    oledDriver: ActorRef[OledDriver.Command]) {
  private val thisHost = settings.config.getString("akka.remote.artery.canonical.hostname")

  private def updateState(nodeId: Int, status: String)(implicit nodes: Map[Int, String]): Map[Int, String] = {
    nodes + (nodeId -> status)
  }

  def running(implicit nodes: Map[Int, String],
              leader: Option[Int]
             ): Behavior[NodeState] = Behaviors
    .receiveMessage[NodeState] {
      case NodeUp(nodeLedId) =>
        setClusterViewState(updateState(nodeLedId, "Up"))
      case NodeJoining(nodeLedId) =>
        setClusterViewState(updateState(nodeLedId, "Joining"))
      case NodeLeaving(nodeLedId) =>
        setClusterViewState(updateState(nodeLedId, "Left"))
      case NodeExiting(nodeLedId) =>
        setClusterViewState(updateState(nodeLedId, "Exited"))
      case NodeRemoved(nodeLedId) =>
        setClusterViewState(updateState(nodeLedId, "Removed"))
      case NodeDown(nodeLedId) =>
        setClusterViewState(updateState(nodeLedId, "Down"))
      case NodeUnreachable(nodeLedId) =>
        setClusterViewState(updateState(nodeLedId, "Unreachable"))
      case NodeWeaklyUp(nodeLedId) =>
        setClusterViewState(updateState(nodeLedId, "Weakly Up"))
      case IsLeader =>
        setLeader(Some(settings.HostToLedMapping(thisHost)))
      case IsNoLeader(address) =>
        setLeader(address)
      case PiClusterSingletonRunning =>
        Behaviors.same
      case PiClusterSingletonNotRunning =>
        Behaviors.same
    }

  private def render(nodes: Map[Int, String], leader: Option[Int]): String = {
    val stringBuilder = new StringBuilder
    //TODO
    (0 to 2).foreach(i => stringBuilder ++= "Node " + i + ": " + nodes.getOrElse(i, "N/A") + "\n")
    stringBuilder ++= "Leader: " + leader.getOrElse("N/A")
    stringBuilder.toString()
  }

  private def setClusterViewState(nodes: Map[Int, String])
                                 (implicit leader: Option[Int]): Behavior[NodeState] = {
    oledDriver ! OledDriver.UpdateView(screenNumber, render(nodes, leader))
    running(nodes, leader)
  }

  private def setLeader(leader: Option[Int])
                       (implicit nodes: Map[Int, String]): Behavior[NodeState] = {
    oledDriver ! OledDriver.UpdateView(screenNumber, render(nodes, leader))
    running(nodes, leader)
  }
}
