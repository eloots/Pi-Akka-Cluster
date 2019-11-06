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
package com.lightbend.akka_oled

import akka.actor.{Actor, ActorLogging, Address, Props}
import akka.cluster.ClusterEvent._
import akka.cluster.MemberStatus.{Up, WeaklyUp}
import akka.cluster.{Cluster, Member}
import akka_oled.Logo
import eroled.{OLEDWindow, SmartOLED}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

object ClusterNodeStatus {

   val ACTOR_NAME = "cluster-node-status"

   def props(): Props =
      Props(new ClusterNodeStatus())

}

class ClusterNodeStatus extends Actor with ActorLogging with Logo {

   var oled: SmartOLED = new SmartOLED()
   val window: OLEDWindow = new OLEDWindow(oled, 0, 0, 256, 64)

   var state: Option[mutable.LinkedHashMap[String, String]] = None
   var showingLogo = true

   override def preStart(): Unit = {
      log.info("BasicOLED initialized!")
      Cluster(context.system)
         .subscribe(self,
            InitialStateAsEvents,
            classOf[LeaderChanged],
            classOf[ReachabilityEvent],
            classOf[MemberEvent]
         )
      context.become(running())
      renderLogo()
   }

   override def receive: Receive = idle

   def idle: Receive = akka.actor.Actor.emptyBehavior

   def mapHostToName(ip: String): String = {
      ip match {
         case "192.168.1.100" => "Node 0"
         case "192.168.1.101" => "Node 1"
         case "192.168.1.102" => "Node 2"
         case _ => "Node X"
      }
   }

   private def nodeStatus(member: Member, status: String): Unit = {
      if (state.isEmpty) {
         state = Some(
            mutable.LinkedHashMap[String, String]("Node 0" -> "N/A", "Node 1" -> "N/A", "Node 2" -> "N/A"))
         oled.clearRam()
      }

      state.get += mapHostToName(member.address.host.get) -> status

      renderState
   }

   private def renderState: Unit = {
      if (showingLogo) {
         oled.clearRam()
         showingLogo = false
      }

      if (state.isDefined)
         oled.drawMultilineString(state.get.map[String] { case (key, value) => key + ": " + value + "        " }.mkString("\n"))
      else
         oled.drawString(0, 21, "Joining cluster")
   }

   def running(): Receive = {
      case SWITCH_FROM_LOGO_TO_SCREEN =>
         renderState

      case msg@MemberUp(member) =>
         nodeStatus(member, "Up")
         log.debug(s"$msg")

      case msg@MemberLeft(member) =>
         nodeStatus(member, "Left")
         log.debug(s"$msg")

      case msg@MemberExited(member) =>
         nodeStatus(member, "Exited")
         log.debug(s"$msg")

      case msg@MemberJoined(member) =>
         nodeStatus(member, "Joined")
         log.debug(s"$msg")

      case msg@MemberRemoved(member, _) =>
         nodeStatus(member, "Removed")
         log.debug(s"$msg")

      case msg@MemberWeaklyUp(member) =>
         nodeStatus(member, "WeaklyUp")
         log.debug(s"$msg")

      case msg@ReachableMember(member) if member.status == Up =>
         nodeStatus(member, "Reachable")
         log.debug(s"$msg")

      case msg@ReachableMember(member) if member.status == WeaklyUp =>
         nodeStatus(member, "Reachable")
         log.debug(s"$msg")

      case msg@UnreachableMember(member) =>
         nodeStatus(member, "Unreachable")
         log.debug(s"$msg")

      case msg@LeaderChanged(Some(leader)) =>
         changeLeader(leader)
         log.debug(s"$msg")

      case msg@LeaderChanged(None) =>
         changeLeader(self.path.address)
         log.debug(s"$msg")

      case event =>
         log.debug(s"!Unknown event! $event")

   }

   private def changeLeader(address: Address): Unit = {
      if (state.isEmpty) {
         oled.clearRam()
         state = Some(
            mutable.LinkedHashMap[String, String]("Node 0" -> "N/A", "Node 1" -> "N/A", "Node 2" -> "N/A"))
      }
      state.get += "Leader" -> mapHostToName(address.host.getOrElse("N/A"))

      renderState
   }

   override def postStop(): Unit = {
      oled.resetOLED()
      Cluster(context.system).unsubscribe(self)
   }

   private def renderLogo() {
      oled.clearRam()
      window.drawBwImage(30, 2, 200, 60, 0xFF.toByte, logoBytes, 0)
      window.drawScreenBuffer()
      showingLogo = true
      import context.dispatcher
      context.system.scheduler.scheduleOnce(2 second, self, SWITCH_FROM_LOGO_TO_SCREEN)
   }

}
