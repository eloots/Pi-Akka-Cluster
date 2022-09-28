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

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.actor.typed.{ActorSystem, Behavior, Terminated}
import akka.cluster.ddata.LWWMapKey
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.management.scaladsl.AkkaManagement
import akka.stream.Materializer
import akkapi.cluster.{ClusterStatusTracker, OledClusterVisualizer, OledDriver, Settings}
import spray.json.DefaultJsonProtocol

object Main extends SprayJsonSupport with DefaultJsonProtocol {

  case class NodeStatus(status: String)

  implicit val transactionFormat = jsonFormat1(NodeStatus)

  def apply(settings: Settings): Behavior[NotUsed] = Behaviors.setup { ctx =>

    val oledDriver = ctx.spawn(OledDriver(settings), "oled-driver")
    oledDriver ! OledDriver.RegisterView("Cluster State", 0)
    oledDriver ! OledDriver.RegisterView("Distributed Data State", 1)

    val clusterView = ctx.spawn(OledClusterVisualizer(0, settings, oledDriver), "oled-cluster-view")
    val clusterStatusTracker = ctx.spawn(ClusterStatusTracker(settings, None), "cluster-status-tracker")
    clusterStatusTracker ! ClusterStatusTracker.SubscribeVisualiser(clusterView)

    val ddataTracker = ctx.spawn(
      DistributedDataTracker(1, LWWMapKey[String, String]("cache"), oledDriver),
      "oled-ddata-view")

    val routes = new Routes(ddataTracker)(ctx.system)

    implicit val untypedSystem: akka.actor.ActorSystem = ctx.system.toClassic
    implicit val mat: Materializer = Materializer.createMaterializer(ctx.system.toClassic)
    Http()(ctx.system.toClassic).bindAndHandle(routes.route,
      settings.config.getString("cluster-node-configuration.external-ip"), 8080)

    Behaviors.receiveSignal {
      case (_, Terminated(_)) =>
        Behaviors.stopped
    }
  }
}

object DisplayDistributedDataMain {
  def main(args: Array[String]): Unit = {
    val settings = Settings()
    val system = ActorSystem[NotUsed](Main(settings), "akka-oled", settings.config)

    // Start Akka HTTP Management extension
    AkkaManagement(system).start()
  }
}
