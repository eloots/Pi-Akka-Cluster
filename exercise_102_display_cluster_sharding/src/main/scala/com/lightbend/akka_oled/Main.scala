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
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.management.scaladsl.AkkaManagement
import akka.persistence.typed.PersistenceId
import akka.stream.Materializer
import akkapi.cluster.{ClusterStatusTracker, OledClusterVisualizer, OledDriver, Settings}
import spray.json._

import scala.concurrent.ExecutionContextExecutor

object Main extends SprayJsonSupport with DefaultJsonProtocol {

  case class AddPoints(points: Int)

  implicit val transactionFormat = jsonFormat1(AddPoints)

  def apply(settings: Settings): Behavior[NotUsed] = Behaviors.setup { ctx =>
    implicit val system = ctx.system
    implicit val untypedSystem: akka.actor.ActorSystem = ctx.system.toClassic
    implicit val ec: ExecutionContextExecutor = ctx.system.executionContext

    val oledDriver = ctx.spawn(OledDriver(settings), "oled-driver")
    oledDriver ! OledDriver.RegisterView("Cluster State", 0)
    oledDriver ! OledDriver.RegisterView("Sharding State", 1)

    val clusterView = ctx.spawn(OledClusterVisualizer(0, settings, oledDriver), "oled-cluster-view")
    val clusterStatusTracker = ctx.spawn(ClusterStatusTracker(settings, None), "cluster-status-tracker")
    clusterStatusTracker ! ClusterStatusTracker.SubscribeVisualiser(clusterView)

    val shardVisualizer = ctx.spawn(OledShardingVisualizer(1, oledDriver), "oled-sharding-view")

    val sharding = ClusterSharding(ctx.system)
    sharding.init(Entity(typeKey = ClientEntity.TypeKey) { entityContext =>
      ClientEntity(entityContext.entityId,
        PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId),
        shardVisualizer)
    })
    val tracker = ctx.spawn(ShardStateTracker(shardVisualizer), "oled-sharding-tracker")
    ctx.spawn(ShardStateScheduler(sharding.shardState, tracker), "oled-sharding-scheduler")

    val routes = new Routes(sharding)

    //materializer
    Materializer.createMaterializer(ctx.system.toClassic)
    implicit val mat: Materializer = Materializer.createMaterializer(ctx.system.toClassic)
    Http()(ctx.system.toClassic).bindAndHandle(routes.route,
      settings.config.getString("cluster-node-configuration.external-ip"), 8080)

    Behaviors.receiveSignal {
      case (_, Terminated(_)) =>
        Behaviors.stopped
    }
  }
}

object DisplayClusterShardingMain {
  def main(args: Array[String]): Unit = {
    val settings = Settings()
    val system = ActorSystem[NotUsed](Main(settings), "akka-oled", settings.config)

    // Start Akka HTTP Management extension
    AkkaManagement(system).start()
  }
}

