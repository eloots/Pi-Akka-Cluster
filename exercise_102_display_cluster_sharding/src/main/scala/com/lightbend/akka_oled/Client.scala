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
package com.lightbend.akka_oled

import akka.actor.{ActorRef, Props}
import akka.persistence.PersistentActor
import com.lightbend.akka_oled.Client.{Get, PostPoints, Stop, PointsAdded}
import com.lightbend.akka_oled.ClusterShardingStatus.Notification

object Client {

   trait Message

   case class PostPoints(name: String, amount: Int) extends Message

   case object Stop extends Message

   case class Get(name: String) extends Message

   final case class PointsAdded(name: String, points: Int) extends Message

   def props(ref: ActorRef) = Props(new Client(ref))
}

class Client(ref: ActorRef) extends PersistentActor {
   override def persistenceId: String = self.path.name

   def updateState(event: PointsAdded): Unit = {
      name = name.orElse(Some(event.name))
      total += event.points
      ref ! Notification(persistenceId, total)
   }


   var name: Option[String] = None
   var total: Int = 0

   override def receiveRecover: Receive = {
      case evt: PointsAdded => updateState(evt)
   }

   override def receiveCommand: Receive = {
      case PostPoints(name, amount) =>
         persist(PointsAdded(name, amount)) {
            a =>
               updateState(a)
               sender() ! "Ok\n"
         }
      case Get(name) =>
         sender() ! total
         ref ! Notification(name, total)
      case Stop => context.stop(self)
   }
}
