package akka.cluster.pi

/**
 * Copyright © 2016-2019 Lightbend, Inc.
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
 * based on events it receives from a `ClusterStatusTracker`.
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
import org.neopixel._

object LedStripVisualiser {

  def apply(settings: Settings): Behavior[ClusterStatusTracker.NodeState] =
    Behaviors.setup { context =>
        new LedStripVisualiser(context, settings)
          .running(heartbeatLedOn = false, weaklyUpIndicatorOn = false, weaklyUpMembers = Set.empty[Int])
  }
}

class LedStripVisualiser(context: ActorContext[ClusterStatusTracker.NodeState],
                         settings: Settings) {

  import settings._

  private val LeaderLedNumber = 5
  private val SingletonLedNumber = 6

  private val ledStripDriver = context.spawn(LedStripDriver(settings), "led-strip-driver")

  def running(heartbeatLedOn: Boolean, weaklyUpIndicatorOn: Boolean, weaklyUpMembers: Set[Int]): Behavior[ClusterStatusTracker.NodeState] = Behaviors
    .receiveMessage[ClusterStatusTracker.NodeState] {
      case ClusterStatusTracker.NodeUp(nodeLedId) =>
        setLedState(nodeLedId, nodeUpColor, None)
        running(heartbeatLedOn, weaklyUpIndicatorOn, weaklyUpMembers - nodeLedId)
      case ClusterStatusTracker.NodeJoining(nodeLedId) =>
        setLedState(nodeLedId, nodeJoinedColor, None)
        running(heartbeatLedOn, weaklyUpIndicatorOn, weaklyUpMembers - nodeLedId)
      case ClusterStatusTracker.NodeLeaving(nodeLedId) =>
        setLedState(nodeLedId, nodeLeftColor, None)
        running(heartbeatLedOn, weaklyUpIndicatorOn, weaklyUpMembers - nodeLedId)
      case ClusterStatusTracker.NodeExiting(nodeLedId) =>
        setLedState(nodeLedId, nodeExitedColor, None)
        running(heartbeatLedOn, weaklyUpIndicatorOn, weaklyUpMembers - nodeLedId)
      case ClusterStatusTracker.NodeRemoved(nodeLedId) =>
        setLedState(nodeLedId, nodeDownColor, None)
        running(heartbeatLedOn, weaklyUpIndicatorOn, weaklyUpMembers - nodeLedId)
      case ClusterStatusTracker.NodeDown(nodeLedId) =>
        setLedState(nodeLedId, nodeDownColor, None)
        running(heartbeatLedOn, weaklyUpIndicatorOn, weaklyUpMembers - nodeLedId)
      case ClusterStatusTracker.NodeUnreachable(nodeLedId) =>
        setLedState(nodeLedId, nodeUnreachableColor, None)
        running(heartbeatLedOn = true, weaklyUpIndicatorOn, weaklyUpMembers - nodeLedId)
      case ClusterStatusTracker.NodeWeaklyUp(nodeLedId) =>
        setLedState(nodeLedId, nodeWeaklyUpColor, Some(LedStripDriver.WeaklyUpBlinker))
        running(heartbeatLedOn, weaklyUpIndicatorOn, weaklyUpMembers + nodeLedId)
      case ClusterStatusTracker.IsLeader =>
        setLeaderIndicator(true)
        Behaviors.same
      case ClusterStatusTracker.IsNoLeader =>
        setLeaderIndicator(false)
        Behaviors.same
      case ClusterStatusTracker.PiClusterSingletonRunning =>
        setSingletonIndicator(singletonRunning = true)
        Behaviors.same
      case ClusterStatusTracker.PiClusterSingletonNotRunning =>
        setSingletonIndicator(singletonRunning = false)
        Behaviors.same
    }

  private def setLeaderIndicator(isLeader: Boolean): Unit = {
    if (isLeader)
      ledStripDriver ! LedStripDriver.SetLedState(logicalToPhysicalLEDMapping(LeaderLedNumber), leaderIndicatorColor, None)
    else
      ledStripDriver ! LedStripDriver.SetLedState(logicalToPhysicalLEDMapping(LeaderLedNumber), Black, None)
  }

  private def setSingletonIndicator(singletonRunning: Boolean): Unit = {
    if (singletonRunning)
      ledStripDriver ! LedStripDriver.SetLedState(logicalToPhysicalLEDMapping(SingletonLedNumber), LightBlue, None)
    else
      ledStripDriver ! LedStripDriver.SetLedState(logicalToPhysicalLEDMapping(SingletonLedNumber), Black, None)
  }
  private def setLedState(nodeLedId: Int, color: Long, blinker: Option[LedStripDriver.Blinker]): Unit = {
    ledStripDriver ! LedStripDriver.SetLedState(logicalToPhysicalLEDMapping(nodeLedId), color, blinker)
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
