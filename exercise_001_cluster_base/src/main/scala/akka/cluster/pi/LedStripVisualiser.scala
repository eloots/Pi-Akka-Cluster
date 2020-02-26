package akka.cluster.pi

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

/**
 * --------------------------------------------------------------------------------
 * The LedStripController visualises the state of cluster nodes in an Akka Cluster
 * on an LED strip with at least 8 LEDs.
 *
 * It will reflect the state of the cluster as seen be a node on which it runs and
 * based on events it receives from a `ClusterStatusAndConvergenceTracker`.
 *
 * Events that contain node-specific information (such as a node moving to the Up
 * state, use a `NodeLedId` (type Int`), which is the relative node number (or
 * logical node Id). (e.g. `node-0` -> `0`, ... `node-4` -> `4`)
 *
 * As there may be different types of LED strips, this code maps the `NodeLedId`
 * to a physical LED on an LED strip. This mapping is done via the
 * `logicalToPhysicalLEDMapping` function.
 *
 */

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import org.neopixel.Neopixel
import akkapi.cluster.Settings

object LedStripVisualiser {

  def apply(settings: Settings): Behavior[ClusterStatusTracker.NodeState] =
    Behaviors.setup { context =>
        new LedStripVisualiser(context, settings)
          .running()
  }
}

class LedStripVisualiser private(context: ActorContext[ClusterStatusTracker.NodeState],
                                 settings: Settings) {

  import settings._

  private val ledStripDriver = context.spawn(LedStripDriver(settings), "led-strip-driver")

  def running(): Behavior[ClusterStatusTracker.NodeState] = Behaviors
    .receiveMessage[ClusterStatusTracker.NodeState] {
      case ClusterStatusTracker.NodeUp(nodeLedId) =>
        setLedState(nodeLedId, nodeUpColor, None)
      case ClusterStatusTracker.NodeJoining(nodeLedId) =>
        setLedState(nodeLedId, nodeJoinedColor, None)
      case ClusterStatusTracker.NodeLeaving(nodeLedId) =>
        setLedState(nodeLedId, nodeLeftColor, None)
      case ClusterStatusTracker.NodeExiting(nodeLedId) =>
        setLedState(nodeLedId, nodeExitedColor, None)
      case ClusterStatusTracker.NodeRemoved(nodeLedId) =>
        setLedState(nodeLedId, nodeDownColor, None)
      case ClusterStatusTracker.NodeDown(nodeLedId) =>
        setLedState(nodeLedId, nodeDownColor, None)
      case ClusterStatusTracker.NodeUnreachable(nodeLedId) =>
        setLedState(nodeLedId, nodeUnreachableColor, None)
      case ClusterStatusTracker.NodeWeaklyUp(nodeLedId) =>
        setLedState(nodeLedId, nodeWeaklyUpColor, Some(LedStripDriver.WeaklyUpBlinker))
      case ClusterStatusTracker.IsLeader =>
        setLeaderIndicator(true)
      case ClusterStatusTracker.IsNoLeader =>
        setLeaderIndicator(false)
      case ClusterStatusTracker.PiClusterSingletonRunning =>
        setSingletonIndicator(singletonRunning = true)
      case ClusterStatusTracker.PiClusterSingletonNotRunning =>
        setSingletonIndicator(singletonRunning = false)

      case ClusterStatusTracker.ClusterConverged =>
        ledStripDriver ! LedStripDriver.ClusterConverged
        Behaviors.same
      case ClusterStatusTracker.ClusterNotConverged =>
        ledStripDriver ! LedStripDriver.ClusterNotConverged
        Behaviors.same
    }

  private def setLeaderIndicator(isLeader: Boolean): Behavior[ClusterStatusTracker.NodeState] = {
    if (isLeader)
      ledStripDriver ! LedStripDriver.SetLedState(logicalToPhysicalLEDMapping(LeaderLedNumber), leaderIndicatorColor, None)
    else
      ledStripDriver ! LedStripDriver.SetLedState(logicalToPhysicalLEDMapping(LeaderLedNumber), Neopixel.Black, None)
    Behaviors.same
  }

  private def setSingletonIndicator(singletonRunning: Boolean): Behavior[ClusterStatusTracker.NodeState] = {
    if (singletonRunning)
      ledStripDriver ! LedStripDriver.SetLedState(logicalToPhysicalLEDMapping(SingletonLedNumber), singletonIndicatorColor, None)
    else
      ledStripDriver ! LedStripDriver.SetLedState(logicalToPhysicalLEDMapping(SingletonLedNumber), Neopixel.Black, None)
    Behaviors.same
  }

  private def setLedState(nodeLedId: Int, color: Long, blinker: Option[LedStripDriver.Blinker]): Behavior[ClusterStatusTracker.NodeState] = {
    ledStripDriver ! LedStripDriver.SetLedState(logicalToPhysicalLEDMapping(nodeLedId), color, blinker)
    Behaviors.same
  }

  private val ledStripType = settings.config.getString("cluster-status-indicator.led-strip-type")

  private val logicalToPhysicalLEDMapping: Int => Int = ledStripType match {
    case "eight-led-reversed-order" =>
      (n: Int) => scala.math.abs(n - 7) % 8
    case "ten-led-non-reversed-order" =>
      identity
    case _ =>
      throw new IllegalArgumentException(s"Unknown LED strip type: $ledStripType")
      identity
  }
}
