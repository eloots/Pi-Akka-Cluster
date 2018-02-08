/**
  * Copyright © 2018 Lightbend, Inc
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

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent._
import akka.cluster.MemberStatus.Up
import neopixel.{rpi_ws281xConstants => wsC}

object ClusterStatusTracker {

  // LED strip configuration:
  val LED_COUNT = 8 // Number of LED pixels.
  val LED_PIN = 18 // GPIO pin connected to the pixels (must support PWM!).
  val LED_FREQ_HZ = 800000 // LED signal frequency in hertz (usually 800khz)
  val LED_DMA = 5 // DMA channel to use for generating signal (try 5)
  val LED_BRIGHTNESS = 10.toShort // Set to 0 for darkest and 255 for brightest
  val LED_INVERT = false // True to invert the signal (when using NPN transistor level shift)
  val LED_CHANNEL = 0
  val LED_STRIP: Int = wsC.WS2811_STRIP_RGB

  val HighestLedIndex = LED_COUNT - 1

  val Black = color(0, 0, 0)
  val Green = color(255, 0, 0)
  val Red = color(0, 255, 0)
  val Blue = color(0, 0, 255)
  val Yellow = color(255, 255, 0)
  val Cyan = color(255, 0, 255)
  val Magenta = color(0, 255, 255)
  val White = color(255, 255, 0)
  val WhiteLow = color(100, 100, 100)
  val LeaderLedNumber = HighestLedIndex - 5

  def resetAllLeds(strip: Adafruit_NeoPixel.type): Unit = {
    for {
      pixel <- 0 until strip.numPixels()
    } strip.setPixelColor(pixel, Black)
    strip.show()
  }

  val HostToLedMapping =
    Map(
      "kubernetes" -> 7,
      "node-0" -> 7,
      "192.168.0.101" -> 7,
      "node-1" -> 6,
      "192.168.0.102" -> 6,
      "node-2" -> 5,
      "192.168.0.103" -> 5,
      "node-3" -> 4,
      "192.168.0.104" -> 4,
      "node-4" -> 3,
      "192.168.0.105" -> 3
    )

  def props(): Props = Props(new ClusterStatusTracker)
}

class ClusterStatusTracker extends Actor with ActorLogging {
  import ClusterStatusTracker._

  private val thisHost = context.system.settings.config.getString("akka.remote.netty.tcp.hostname")
  log.debug(s"Starting ClusterStatus Actor on $thisHost")

  override def receive: Receive = idle

  def idle: Receive = akka.actor.Actor.emptyBehavior

  def running(strip: Adafruit_NeoPixel.type): Receive = {
    case MemberUp(member) =>
      strip.setPixelColor(HostToLedMapping(member.address.host.get), Green)
      strip.show()
      log.debug(s"~~~> Member Up: $member")

    case MemberLeft(member) =>
      log.debug(s"~~~> Member Left: $member")

    case MemberExited(member) =>
      log.debug(s"~~~> Member Exited: $member")

    case MemberJoined(member) =>
      strip.setPixelColor(HostToLedMapping(member.address.host.get), Yellow)
      strip.show()
      log.debug(s"~~~> Member Joined: $member")

    case MemberRemoved(member, previousStatus) =>
      strip.setPixelColor(HostToLedMapping(member.address.host.get), Red)
      strip.show()
      log.debug(s"~~~> Member Joined: $member with previous status: $previousStatus")

    case MemberWeaklyUp(member) =>
      log.debug(s"~~~> Member Weakly Up: $member")

    case ReachableMember(member) if member.status == Up =>
      strip.setPixelColor(HostToLedMapping(member.address.host.get), Green)
      strip.show()
      log.debug(s"~~~> Member Reachable: $member")

    case UnreachableMember(member) =>
      strip.setPixelColor(HostToLedMapping(member.address.host.get), WhiteLow)
      strip.show()
      log.debug(s"~~~> Member Unreachable: $member")

    case LeaderChanged(Some(leader)) if leader.host.getOrElse("") == thisHost =>
      strip.setPixelColor(LeaderLedNumber, Cyan)
      strip.show()
      log.debug(s"~~~> Leader Changed: $leader")

    case LeaderChanged(Some(leader)) =>
      strip.setPixelColor(LeaderLedNumber, Black)
      strip.show()
      log.debug(s"~~~> Leader Changed: $leader")

    case event =>
      log.debug(s"~~~> UNHANDLED CLUSTER DOMAIN EVENT: $event")
  }

  override def preStart(): Unit =
    Cluster(context.system).subscribe(self, InitialStateAsEvents, classOf[ClusterDomainEvent])

    val strip = Adafruit_NeoPixel(LED_COUNT, LED_PIN, LED_FREQ_HZ, LED_DMA, LED_INVERT, LED_BRIGHTNESS, LED_CHANNEL, LED_STRIP)
    strip.begin()
    resetAllLeds(strip)
    context.become(running(strip))

  override def postStop(): Unit =
    Cluster(context.system).unsubscribe(self)
}
