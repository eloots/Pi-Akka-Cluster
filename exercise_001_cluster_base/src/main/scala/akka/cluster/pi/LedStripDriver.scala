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

import akka.actor.typed.scaladsl.{ActorContext, Behaviors, TimerScheduler}
import akka.actor.typed.{Behavior, PostStop}
import neopixel.{rpi_ws281xConstants => wsC}
import org.neopixel.Neopixel.{Adafruit_NeoPixel, Black}
import akkapi.cluster.Settings

object LedStripDriver {

  sealed trait Command
  final case class SetLedState(ledId: Int, color: Long, optBlinker: Option[Blinker]) extends Command
  private final case object Heartbeat extends  Command
  private final case object WeaklyUpHeartBeat extends Command
  case object ClusterConverged extends Command
  case object ClusterNotConverged extends Command

  sealed trait Blinker
  case object WeaklyUpBlinker extends Blinker

  def apply(settings: Settings): Behavior[Command] = Behaviors.setup { context =>
    Behaviors.withTimers[Command] { timers =>
      new LedStripDriver(context, settings, timers)
        .run(hearbeatLedIsOn = false, heartbeatColor = settings.heartbeatIndicatorColor, fastbeatCounter = 0, weaklyUpLedIsOn = false, weaklyUpNodes = Set.empty[Int])
    }
  }
}

class LedStripDriver private(context: ActorContext[LedStripDriver.Command],
                             settings: Settings,
                             timers: TimerScheduler[LedStripDriver.Command]) {

  import LedStripDriver._
  import settings.LedStripConfig._
  import settings._

  private val strip = Adafruit_NeoPixel(ledCount, ledPin, ledFreqHz, ledDma, ledInvert, ledBrightness, ledChannel, wsC.WS2811_STRIP_RGB)

  strip.begin()
  resetAllLeds(strip)

  timers.startTimerAtFixedRate(Heartbeat, heartbeatIndicatorInterval)
  timers.startTimerAtFixedRate(WeaklyUpHeartBeat, weaklyUpIndicatorInterval)

  // TODO: Need to add customer supervision to handle IllegalArgumentException being thrown when a non-existing LED strip type is passed in
  def run(hearbeatLedIsOn: Boolean,
          heartbeatColor: Long,
          fastbeatCounter: Int,
          weaklyUpLedIsOn: Boolean,
          weaklyUpNodes: Set[Int]): Behavior[Command] = Behaviors.receiveMessage[Command] {

    case SetLedState(ledId, color, None) =>
      setPixelColorAndShow(ledId, color)
      run(hearbeatLedIsOn, heartbeatColor, fastbeatCounter, weaklyUpLedIsOn, weaklyUpNodes - ledId)
    case SetLedState(ledId, color, Some(WeaklyUpBlinker)) =>
      setPixelColorAndShow(ledId, color)
      run(hearbeatLedIsOn, heartbeatColor, fastbeatCounter, weaklyUpLedIsOn, weaklyUpNodes + ledId)

    case Heartbeat if hearbeatLedIsOn && fastbeatCounter != 0 =>
      setPixelColorAndShow(HeartbeatLedNumber, Black)
      run(hearbeatLedIsOn = false, heartbeatColor, fastbeatCounter - 1, weaklyUpLedIsOn, weaklyUpNodes)
    case Heartbeat if hearbeatLedIsOn =>
      setPixelColorAndShow(HeartbeatLedNumber, Black)
      timers.startTimerAtFixedRate(Heartbeat, heartbeatIndicatorInterval)
      run(hearbeatLedIsOn = false, heartbeatIndicatorColor, fastbeatCounter, weaklyUpLedIsOn, weaklyUpNodes)
    case Heartbeat =>
      setPixelColorAndShow(HeartbeatLedNumber, heartbeatColor)
      run(hearbeatLedIsOn = true, heartbeatColor, fastbeatCounter, weaklyUpLedIsOn, weaklyUpNodes)

    case ClusterConverged =>
      timers.startTimerAtFixedRate(Heartbeat, clusterStateConvergenceInterval)
      setPixelColorAndShow(HeartbeatLedNumber, heartbeatIndicatorConvergenceColor)
      run(hearbeatLedIsOn = true, heartbeatIndicatorConvergenceColor, fastbeatCounter = 3, weaklyUpLedIsOn, weaklyUpNodes)

    case ClusterNotConverged =>
      timers.startTimerAtFixedRate(Heartbeat, heartbeatIndicatorInterval)
      setPixelColorAndShow(HeartbeatLedNumber, heartbeatIndicatorNoConvergenceColor)
      run(hearbeatLedIsOn = true, heartbeatIndicatorNoConvergenceColor, fastbeatCounter, weaklyUpLedIsOn, weaklyUpNodes)

    case WeaklyUpHeartBeat if weaklyUpLedIsOn =>
      for (ledId <- weaklyUpNodes) setPixelColorAndShow(ledId, Black)
      run(hearbeatLedIsOn, heartbeatColor, fastbeatCounter, weaklyUpLedIsOn = false, weaklyUpNodes)
    case WeaklyUpHeartBeat =>
      for (ledId <- weaklyUpNodes) setPixelColorAndShow(ledId, nodeWeaklyUpColor)
      run(hearbeatLedIsOn, heartbeatColor, fastbeatCounter, weaklyUpLedIsOn = true, weaklyUpNodes)
  }.receiveSignal {
    case (_, signal) if signal == PostStop =>
      resetAllLeds(strip)
      Behaviors.same
  }

  private def setPixelColorAndShow(ledNumber: Int,
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
