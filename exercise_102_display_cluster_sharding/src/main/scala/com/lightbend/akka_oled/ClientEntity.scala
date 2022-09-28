/**
  * Copyright © Eric Loots 2022 - eric.loots@gmail.com
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
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.lightbend.akka_oled

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import com.lightbend.akka_oled.OledShardingVisualizer.Notification

object ClientEntity {

  sealed trait Command

  case class PostPoints(name: String, amount: Int)(val replyTo: ActorRef[String]) extends Command

  case class Get(name: String)(val replyTo: ActorRef[Int]) extends Command

  final case class PointsAdded(name: String, points: Int)

  val TypeKey: EntityTypeKey[Command] =
    EntityTypeKey[Command]("ClientEntity")

  final case class ClientPoints(name: String, points: Int, visualizer: ActorRef[Notification]) {
    def add(delta: Int) = copy(points = points + delta)
  }

  private val commandHandler: (ClientPoints, Command) => Effect[PointsAdded, ClientPoints] = {
    (state, cmd) =>
      cmd match {
        case pp@PostPoints(name, amount) =>
          Effect.persist(PointsAdded(name, amount)).thenRun(s => {
            state.visualizer ! Notification(s.name, s.points)
            pp.replyTo ! "Ok\n"
          })
        case g@Get(_) =>
          g.replyTo ! state.points
          state.visualizer ! Notification(state.name, state.points)
          Effect.none
      }
  }

  private val eventHandler: (ClientPoints, PointsAdded) => ClientPoints = {
    (state, evt) => state.add(evt.points)
  }


  def apply(entityId: String, persistenceId: PersistenceId, visualizer: ActorRef[Notification]): Behavior[ClientEntity.Command] =
    Behaviors.setup { _ =>
      EventSourcedBehavior(persistenceId, ClientPoints(entityId, 0, visualizer), commandHandler, eventHandler)
    }
}

