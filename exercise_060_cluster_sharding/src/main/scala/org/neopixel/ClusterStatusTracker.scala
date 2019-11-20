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

package org.neopixel

import akka.actor.{Actor, ActorLogging, Props, Timers}

object ClusterStatusTracker {

  def resetAllLeds(strip: Adafruit_NeoPixel.type): Unit = {
    for {
      pixel <- 0 until strip.numPixels()
    } strip.setPixelColor(pixel, Black)
    strip.show()
  }

  case object Heartbeat
  case object WeaklyUpBeat

  def props(strip: Adafruit_NeoPixel.type, logicalToPhysicalLEDMapping: Int => Int): Props =
    Props(new ClusterStatusTracker(strip, logicalToPhysicalLEDMapping))
}

class ClusterStatusTracker(strip: Adafruit_NeoPixel.type, logicalToPhysicalLEDMapping: Int => Int)
  extends Actor with ActorLogging with SettingsActor with Timers {
  import ClusterStatusTracker._

  private val thisHost = context.system.settings.config.getString("akka.remote.artery.canonical.hostname")
  log.debug(s"Starting ClusterStatus Actor on $thisHost")

  import settings._

  private val HeartbeatLedNumber = if(strip.numPixels() == 8) 7 else 9

  override def receive: Receive = idle

  def idle: Receive = akka.actor.Actor.emptyBehavior

  def running(heartbeatLEDOn: Boolean): Receive = {

    case Heartbeat if heartbeatLEDOn =>
      setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(HeartbeatLedNumber), Black)

      context.become(running(heartbeatLEDOn = false))

    case Heartbeat =>
      setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(HeartbeatLedNumber), heartbeartIndicatorColor)

      context.become(running(heartbeatLEDOn = true))
  }

  override def preStart(): Unit = {

    log.info(s"LED brightness = ${settings.ledBrightness}")
    strip.begin()
    resetAllLeds(strip)

    timers.startPeriodicTimer("heartbeat-timer", Heartbeat, heartbeatIndicatorInterval)
    context.become(running(heartbeatLEDOn = false))


    context.actorOf(
      PiClusterSharding.props(strip, logicalToPhysicalLEDMapping),
      "pi-cluster-sharding"
    )
  }

  override def postStop(): Unit = {
    resetAllLeds(strip)
  }

  private def setPixelColorAndShow(strip: Adafruit_NeoPixel.type ,
                                   ledNumber: Int,
                                   ledColor: Long): Unit = {
    strip.setPixelColor(ledNumber, ledColor)
    strip.show()
  }

  def mapHostToLED(hostName: String): Int =
    logicalToPhysicalLEDMapping(HostToLedMapping(hostName))
}

