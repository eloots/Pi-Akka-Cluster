/**
 * Copyright © 2016-2020 Lightbend, Inc.
 *
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
 * NO COMMERCIAL SUPPORT OR ANY OTHER FORM OF SUPPORT IS OFFERED ON
 * THIS SOFTWARE BY LIGHTBEND, Inc.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package akkapi.cluster

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object OledClusterVisualizer {

  def apply(screenNumber: Int,
            settings: Settings,
            oledDriver: ActorRef[OledDriver.Command]): Behavior[ClusterStatusTracker.NodeState] =
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

  private implicit def nodeIdToName(nodeId: Option[Int]): String = nodeId.map(n => "Node " + (n + 1)).getOrElse("N/A")

  private implicit def nodeIdToName(nodeId: Int): String = "Node " + (nodeId + 1)

  private def updateState(nodeId: Int, status: String)(implicit nodes: Map[Int, String]): Map[Int, String] = {
    nodes + (nodeId -> status)
  }

  def running(implicit nodes: Map[Int, String],
              leader: Option[Int]
             ): Behavior[ClusterStatusTracker.NodeState] = Behaviors
    .receiveMessage[ClusterStatusTracker.NodeState] {
      case ClusterStatusTracker.NodeUp(nodeLedId) =>
        setClusterViewState(updateState(nodeLedId, "Up"))
      case ClusterStatusTracker.NodeJoining(nodeLedId) =>
        setClusterViewState(updateState(nodeLedId, "Joining"))
      case ClusterStatusTracker.NodeLeaving(nodeLedId) =>
        setClusterViewState(updateState(nodeLedId, "Left"))
      case ClusterStatusTracker.NodeExiting(nodeLedId) =>
        setClusterViewState(updateState(nodeLedId, "Exited"))
      case ClusterStatusTracker.NodeRemoved(nodeLedId) =>
        setClusterViewState(updateState(nodeLedId, "Removed"))
      case ClusterStatusTracker.NodeDown(nodeLedId) =>
        setClusterViewState(updateState(nodeLedId, "Down"))
      case ClusterStatusTracker.NodeUnreachable(nodeLedId) =>
        setClusterViewState(updateState(nodeLedId, "Unreachable"))
      case ClusterStatusTracker.NodeWeaklyUp(nodeLedId) =>
        setClusterViewState(updateState(nodeLedId, "Weakly Up"))
      case ClusterStatusTracker.IsLeader =>
        setLeader(Some(settings.HostToLedMapping(thisHost)))
      case ClusterStatusTracker.IsNoLeader(address) =>
        setLeader(address)
      case ClusterStatusTracker.PiClusterSingletonRunning =>
        Behaviors.same
      case ClusterStatusTracker.PiClusterSingletonNotRunning =>
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
                                 (implicit leader: Option[Int]): Behavior[ClusterStatusTracker.NodeState] = {
    oledDriver ! OledDriver.UpdateView(screenNumber,render(nodes, leader))
    running(nodes, leader)
  }

  private def setLeader(leader: Option[Int])
                       (implicit nodes: Map[Int, String]): Behavior[ClusterStatusTracker.NodeState] = {
    oledDriver ! OledDriver.UpdateView(screenNumber,render(nodes, leader))
    running(nodes, leader)
  }
}
