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
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.MemberStatus.{Up, WeaklyUp}

object ClusterStatusTracker {

  def resetAllLeds(strip: Adafruit_NeoPixel.type): Unit = {
    for {
      pixel <- 0 until strip.numPixels()
    } strip.setPixelColor(pixel, Black)
    strip.show()
  }

  case object Heartbeat

  def props(strip: Adafruit_NeoPixel.type, logicalToPhysicalLEDMapping: Int => Int): Props =
    Props(new ClusterStatusTracker(strip, logicalToPhysicalLEDMapping))
}

class ClusterStatusTracker(strip: Adafruit_NeoPixel.type, logicalToPhysicalLEDMapping: Int => Int) extends Actor with ActorLogging with SettingsActor with Timers {
  import ClusterStatusTracker._

  private val thisHost = context.system.settings.config.getString("akka.remote.artery.canonical.hostname")
  log.debug(s"Starting ClusterStatus Actor on $thisHost")

  import settings._

  private val LeaderLedNumber = 5
  private val HeartbeatLedNumber = 7

  override def receive: Receive = idle

  def idle: Receive = akka.actor.Actor.emptyBehavior

  def running(hearbeatLEDOn: Boolean): Receive = {
    case Heartbeat if hearbeatLEDOn =>
      setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(HeartbeatLedNumber), Black)
      context.become(running(hearbeatLEDOn = false))

    case Heartbeat =>
      setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(HeartbeatLedNumber), heartbeartIndicatorColor)
      context.become(running(hearbeatLEDOn = true))

    case msg @ MemberUp(member) =>
      setPixelColorAndShow(strip, mapHostToLED(member.address.host.get), nodeUpColor)
      log.debug(s"$msg")

    case msg @ MemberLeft(member) =>
      setPixelColorAndShow(strip, mapHostToLED(member.address.host.get), nodeLeftColor)
      log.debug(s"$msg")

    case msg @ MemberExited(member) =>
      setPixelColorAndShow(strip, mapHostToLED(member.address.host.get), nodeExitedColor)
      log.debug(s"$msg")

    case msg @ MemberJoined(member) =>
      setPixelColorAndShow(strip, mapHostToLED(member.address.host.get), nodeJoinedColor)
      log.debug(s"$msg")

    case msg @ MemberRemoved(member, previousStatus) =>
      setPixelColorAndShow(strip, mapHostToLED(member.address.host.get), nodeDownColor)
      log.debug(s"$msg")

    case msg @ MemberWeaklyUp(member) =>
      setPixelColorAndShow(strip, mapHostToLED(member.address.host.get), nodeWeaklyUpColor)
      log.debug(s"$msg")

    case msg @ ReachableMember(member) if member.status == Up =>
      setPixelColorAndShow(strip, mapHostToLED(member.address.host.get), nodeUpColor)
      log.debug(s"$msg")

    case msg @ ReachableMember(member) if member.status == WeaklyUp =>
      setPixelColorAndShow(strip, mapHostToLED(member.address.host.get), nodeWeaklyUpColor)
      log.debug(s"$msg")

    case msg @ UnreachableMember(member) =>
      setPixelColorAndShow(strip, mapHostToLED(member.address.host.get), nodeUnreachableColor)
      log.debug(s"$msg")

    case msg @ LeaderChanged(Some(leader)) if leader.host.getOrElse("") == thisHost =>
      setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(LeaderLedNumber), leaderIndicatorColor)
      log.debug(s"$msg")

    case msg @ LeaderChanged(Some(leader)) =>
      setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(LeaderLedNumber), Black)
      log.debug(s"$msg")

    case msg @ LeaderChanged(None) =>
      setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(LeaderLedNumber), Black)
      log.debug(s"$msg")

    case event =>
      log.debug(s"~~~> UNHANDLED CLUSTER DOMAIN EVENT: $event")

  }

  override def preStart(): Unit = {
    strip.begin()
    resetAllLeds(strip)
    Cluster(context.system)
      .subscribe(self,
        InitialStateAsEvents,
        classOf[LeaderChanged],
        classOf[ReachabilityEvent],
        classOf[MemberEvent]
      )
    timers.startPeriodicTimer("heartbeat-timer", Heartbeat, heartbeatIndicatorInterval)
    context.become(running(hearbeatLEDOn = false))

  }

  override def postStop(): Unit = {
    resetAllLeds(strip)
    Cluster(context.system).unsubscribe(self)
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

