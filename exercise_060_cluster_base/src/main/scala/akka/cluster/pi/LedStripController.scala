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
 * TODO: Add back support cluster cluster singleton visualisation
 */

import akka.actor.typed.scaladsl.{ActorContext, Behaviors, TimerScheduler}
import akka.actor.typed.{Behavior, PostStop}
import neopixel.{rpi_ws281xConstants => wsC}
import org.neopixel._

object LedStripController {

  sealed trait LEDStripCommands
  final case object Unknown extends LEDStripCommands
  final case class Joining(nodeLedId: Int) extends LEDStripCommands
  final case class Up(nodeLedId: Int) extends LEDStripCommands
  final case class Leaving(nodeLedId: Int) extends LEDStripCommands
  final case class Exiting(nodeLedId: Int) extends LEDStripCommands
  final case class Removed(nodeLedId: Int) extends LEDStripCommands
  final case class Down(nodeLedId: Int) extends LEDStripCommands
  final case class Unreachable(nodeLedId: Int) extends LEDStripCommands
  final case class WeaklyUp(nodeLedId: Int) extends LEDStripCommands
  final case object IsLeader extends LEDStripCommands
  final case object IsNoLeader extends LEDStripCommands

  private final case object Heartbeat extends LEDStripCommands
  private final case object WeaklyUpHeartbeat extends LEDStripCommands

  def apply(settings: Settings): Behavior[LEDStripCommands] =
    Behaviors.setup { context =>
      Behaviors.withTimers[LEDStripCommands] { timers =>
        new LedStripController(context, settings, timers: TimerScheduler[LEDStripCommands])
          .running(heartbeatLedOn = false, weaklyUpIndicatorOn = false, weaklyUpMembers = Set.empty[Int])
    }
  }

  private def setPixelColorAndShow(strip: Adafruit_NeoPixel.type ,
                                   ledNumber: Int,
                                   ledColor: Long): Unit = {
    strip.setPixelColor(ledNumber, ledColor)
    strip.show()
  }

  def resetAllLeds(strip: Adafruit_NeoPixel.type): Unit = {
    for {
      pixel <- 0 until strip.numPixels()
    } strip.setPixelColor(pixel, Black)
    strip.show()
  }
}

class LedStripController(context: ActorContext[LedStripController.LEDStripCommands],
                         settings: Settings,
                         timers: TimerScheduler[LedStripController.LEDStripCommands]) {

  import LedStripController._
  import settings.LedStripConfig._
  import settings._

  private val LeaderLedNumber = 5
  private val HeartbeatLedNumber = 7

  private val strip = Adafruit_NeoPixel(ledCount, ledPin, ledFreqHz, ledDma, ledInvert, ledBrightness, ledChannel, wsC.WS2811_STRIP_RGB)

  // Initialise & reset LED Strip
  strip.begin()
  resetAllLeds(strip)

  timers.startTimerAtFixedRate(Heartbeat, heartbeatIndicatorInterval)
  timers.startTimerAtFixedRate(WeaklyUpHeartbeat, weaklyUpIndicatorInterval)

  def running(heartbeatLedOn: Boolean, weaklyUpIndicatorOn: Boolean, weaklyUpMembers: Set[Int]): Behavior[LEDStripCommands] = Behaviors
    .receiveMessage[LEDStripCommands] {
      case Up(nodeLedId) =>
        setNodeLedStatus(nodeLedId, nodeUpColor)
        running(heartbeatLedOn, weaklyUpIndicatorOn, weaklyUpMembers - nodeLedId)
      case Joining(nodeLedId) =>
        setNodeLedStatus(nodeLedId, nodeJoinedColor)
        running(heartbeatLedOn, weaklyUpIndicatorOn, weaklyUpMembers - nodeLedId)
      case Leaving(nodeLedId) =>
        setNodeLedStatus(nodeLedId, nodeLeftColor)
        running(heartbeatLedOn, weaklyUpIndicatorOn, weaklyUpMembers - nodeLedId)
      case Exiting(nodeLedId) =>
        setNodeLedStatus(nodeLedId, nodeExitedColor)
        running(heartbeatLedOn, weaklyUpIndicatorOn, weaklyUpMembers - nodeLedId)
      case Removed(nodeLedId) =>
        setNodeLedStatus(nodeLedId, nodeDownColor)
        running(heartbeatLedOn, weaklyUpIndicatorOn, weaklyUpMembers - nodeLedId)
      case Down(nodeLedId) =>
        setNodeLedStatus(nodeLedId, nodeDownColor)
        running(heartbeatLedOn, weaklyUpIndicatorOn, weaklyUpMembers - nodeLedId)
      case Heartbeat if heartbeatLedOn =>
        setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(HeartbeatLedNumber), Black)
        running(heartbeatLedOn = false, weaklyUpIndicatorOn, weaklyUpMembers)
      case Heartbeat =>
        setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(HeartbeatLedNumber), heartbeatIndicatorColor)
        running(heartbeatLedOn = true, weaklyUpIndicatorOn, weaklyUpMembers)
      case Unreachable(nodeLedId) =>
        setNodeLedStatus(nodeLedId, nodeUnreachableColor)
        running(heartbeatLedOn = true, weaklyUpIndicatorOn, weaklyUpMembers - nodeLedId)
      case WeaklyUp(nodeLedId) =>
        setNodeLedStatus(nodeLedId, nodeWeaklyUpColor)
        running(heartbeatLedOn, weaklyUpIndicatorOn, weaklyUpMembers + nodeLedId)
      case WeaklyUpHeartbeat if weaklyUpIndicatorOn =>
        for {nodeLedId <- weaklyUpMembers}
          setNodeLedStatus(nodeLedId, Black)
        running(heartbeatLedOn, ! weaklyUpIndicatorOn, weaklyUpMembers)
      case WeaklyUpHeartbeat =>
        for {nodeLedId <- weaklyUpMembers}
          setNodeLedStatus(nodeLedId, nodeWeaklyUpColor)
        running(heartbeatLedOn, ! weaklyUpIndicatorOn, weaklyUpMembers)
      case IsLeader =>
        setLeaderIndicator(true)
        Behaviors.same
      case IsNoLeader =>
        setLeaderIndicator(false)
        Behaviors.same
    }
    .receiveSignal {
      case (_, signal) if signal == PostStop =>
        resetAllLeds(strip)
        Behaviors.same
    }

  private def setLeaderIndicator(isLeader: Boolean): Unit = {
    if (isLeader)
      setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(LeaderLedNumber), leaderIndicatorColor)
    else
      setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(LeaderLedNumber), Black)
  }

  private def setNodeLedStatus(nodeLedId: Int, color: Long): Unit = {
    setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(nodeLedId), color)
  }

  private val ledStripType = settings.config.getString("cluster-status-indicator.led-strip-type")

  // TODO: this should be a pure function so that it can be move to an object.
  // In case the strip is misconfigured, an IllegalArgumentException should be thrown
  // Supervision is then used to terminate the ActorSystem
  private val logicalToPhysicalLEDMapping: Int => Int = ledStripType match {
    case "eight-led-reversed-order" =>
      (n: Int) => scala.math.abs(n - 7) % 8
    case "ten-led-non-reversed-order" =>
      identity
    case _ =>
      context.system.terminate()
      println(s"Unknown LED strip type: $ledStripType")
      System.exit(-1)
      identity
  }
}
