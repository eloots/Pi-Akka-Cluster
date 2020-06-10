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

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.ddata.typed.scaladsl.{DistributedData, Replicator}
import akka.cluster.ddata.{LWWMap, LWWMapKey, SelfUniqueAddress}
import akkapi.cluster.OledDriver
import akkapi.cluster.OledDriver.UpdateView

object DistributedDataTracker {

  sealed trait Command

  case class UpdateStatus(name: String, status: String) extends Command

  case class Get(name: String, replyTo: ActorRef[String]) extends Command

  case class SubscribeResponse(rsp: Replicator.SubscribeResponse[LWWMap[String, String]]) extends Command

  case class InternalUpdateResponse(rsp: Replicator.UpdateResponse[LWWMap[String, String]]) extends Command

  private val NO_DATA = "No data"

  def apply(screenIndex: Int,
            key: LWWMapKey[String, String],
            oledDriver: ActorRef[OledDriver.Command]): Behavior[DistributedDataTracker.Command] =
    Behaviors.setup { context =>

      oledDriver ! UpdateView(screenIndex, NO_DATA)

      implicit val node: SelfUniqueAddress = DistributedData(context.system).selfUniqueAddress
      DistributedData.withReplicatorMessageAdapter[Command, LWWMap[String, String]] {
        replicatorAdapter =>
          replicatorAdapter.subscribe(key, SubscribeResponse.apply)

          def updated(cachedValue: Map[String, String]): Behavior[Command] = {
            Behaviors.receiveMessage[Command] {
              case UpdateStatus(name, status) =>
                replicatorAdapter.askUpdate(
                  askReplyTo => Replicator.Update(key, LWWMap.empty[String, String],
                    Replicator.WriteLocal, askReplyTo)(_ :+ (name -> status)),
                  InternalUpdateResponse.apply)
                val updatedValue = cachedValue + (name -> status)
                oledDriver ! UpdateView(screenIndex, renderState(updatedValue))
                updated(updatedValue)

              case Get(name, replyTo) =>
                replyTo ! cachedValue.getOrElse(name, "")
                Behaviors.same

              case InternalUpdateResponse(_) =>
                Behaviors.same

              case SubscribeResponse(chg@Replicator.Changed(`key`)) =>
                val value = chg.get(key).entries
                oledDriver ! UpdateView(screenIndex, renderState(value))
                updated(value)
            }

          }

          updated(Map.empty[String, String])
      }

    }

  private def renderState(cachedValue: Map[String, String]): String = {
    if (cachedValue.nonEmpty)
      cachedValue.map[String] { case (key, value) => key + ": " + value + "        " }.mkString("\n")
    else
      NO_DATA
  }
}


