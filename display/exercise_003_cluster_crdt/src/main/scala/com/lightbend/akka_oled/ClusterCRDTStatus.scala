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

import akka.actor.{Actor, ActorLogging, ActorRef, Address, Props}
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent.{InitialStateAsEvents, LeaderChanged, MemberEvent, MemberExited, MemberJoined, MemberLeft, MemberRemoved, MemberUp, MemberWeaklyUp, ReachabilityEvent, ReachableMember, UnreachableMember}
import akka.cluster.MemberStatus.{Up, WeaklyUp}
import akka.cluster.ddata.Replicator.{Changed, Subscribe, Update, UpdateSuccess, WriteLocal, WriteTo}
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey}
import akka.cluster.sharding.ShardRegion
import akka_oled.ButtonPushHandlers.{NEXT_SCREEN, RESET_SCREEN}
import akka_oled.{ButtonPushHandlers, Logo}
import com.lightbend.akka_oled.ClusterCRDTStatus.{Get, SwitchFromTitleToScreen, UpdateStatus}
import eroled.{OLEDWindow, SmartOLED}

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

object  ClusterCRDTStatus{
   val ACTOR_NAME = "cluster-Sharding-status"
   def props() = Props(new ClusterCRDTStatus)
   case class SwitchFromTitleToScreen(screen: Int)

   case class UpdateStatus(name: String, status: String)
   case object Stop
   case class Get(name: String)
}
class ClusterCRDTStatus extends Actor with ActorLogging with Logo with ButtonPushHandlers{
   var oled: SmartOLED = new SmartOLED()
   val window: OLEDWindow = new OLEDWindow(oled, 0, 0, 256, 64)

   implicit val ec: ExecutionContext = context.dispatcher
   implicit val node = DistributedData(context.system).selfUniqueAddress
   val replicator = DistributedData(context.system).replicator

   val screens = Map(0 -> "Cluster State", 1 -> "Distributed Data")
   var currentScreen = 0
   var showingLogo = true
   var showingTitle = false
   val Cache = LWWMapKey[String,String]("cache")
   var currentValue = Map[String,String]()

   var cluster =  mutable.LinkedHashMap[String, String]("Node 0" -> "N/A", "Node 1" -> "N/A", "Node 2" -> "N/A")

   override def preStart(): Unit = {
      log.info("BasicOLED initialized!")
      replicator ! Subscribe(Cache, self)
      Cluster(context.system)
         .subscribe(self,
            InitialStateAsEvents,
            classOf[LeaderChanged],
            classOf[ReachabilityEvent],
            classOf[MemberEvent]
         )
      initButtonPush(self)
      renderLogo()

   }
   private def renderLogo() {
      oled.clearRam()
      window.drawBwImage(30, 2, 200, 60, 0xFF.toByte, logoBytes, 0)
      window.drawScreenBuffer()
      showingLogo = true
      context.system.scheduler.scheduleOnce(2 second, self, SWITCH_FROM_LOGO_TO_SCREEN)

   }

   private def renderTitle {
      showingLogo = false
      showingTitle = true
      oled.clearRam()
      oled.drawString(0, 21, "Screen " + (currentScreen) + ": " + screens(currentScreen))
      log.info("Rendered title")
      context.system.scheduler.scheduleOnce(2 second, self, SwitchFromTitleToScreen(currentScreen))
   }

   def mapHostToName(ip: String): String = {
      ip match {
         case "192.168.1.100" => "Node 0"
         case "192.168.1.101" => "Node 1"
         case "192.168.1.102" => "Node 2"
         case _ => "Node X"
      }
   }

   private def nodeStatus(member: Member, status: String): Unit = {
      cluster += mapHostToName(member.address.host.get) -> status
      renderState
   }

   private def changeLeader(address: Address): Unit = {
      cluster += "Leader" -> mapHostToName(address.host.getOrElse("N/A"))
      renderState
   }


   private def renderState: Unit = {
      if (!showingLogo && !showingTitle) {
         if(currentScreen == 0) {
            if (!cluster.isEmpty)
               oled.drawMultilineString(cluster.map[String] { case (key, value) => key + ": " + value + "        " }.mkString("\n"))
            else
               oled.drawString(0, 21, "Joining cluster")
         } else {
            if (!currentValue.isEmpty)
               oled.drawMultilineString(currentValue.map[String] { case (key, value) => key + ": " + value + "        " }.mkString("\n"))
            else
               oled.drawString(0, 0, "No data")
         }
      }
   }

   override def receive: Actor.Receive = {
      case UpdateStatus(name,status) =>
         currentValue += (name -> status)
         replicator ! Update(Cache, LWWMap.empty[String, String], WriteLocal)(_ :+ (name -> status))
         renderState

      case Get(name) =>
         sender ! currentValue.get(name)

      case c @ Changed(Cache) =>
         currentValue = c.get(Cache).entries
         renderState

      case RESET_SCREEN =>
         currentScreen = 0
         log.info("Reset Screen")
         renderTitle

      case NEXT_SCREEN =>
         currentScreen = if (currentScreen == screens.size - 1) 0 else currentScreen + 1
         log.info("Next screen")
         renderTitle

      case SWITCH_FROM_LOGO_TO_SCREEN =>
         renderTitle

      case SwitchFromTitleToScreen(screen) =>
         if (screen == currentScreen) {
            showingTitle = false
            log.info("Render screen")
            oled.clearRam()
            renderState
         }


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
}
