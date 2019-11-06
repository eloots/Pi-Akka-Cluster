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

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, get, onSuccess, pathPrefix, post}
import akka.management.scaladsl.AkkaManagement
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import com.lightbend.akka_oled.ClusterCRDTStatus.{Get, UpdateStatus}
import spray.json.DefaultJsonProtocol

import scala.concurrent.duration._
import scala.concurrent.Future

object Main extends SprayJsonSupport with DefaultJsonProtocol{
   case class NodeStatus(status:String)
   def main(args: Array[String]): Unit = {
      val baseConfig = ConfigFactory.load()
      implicit val transactionFormat = jsonFormat1(NodeStatus)
      implicit val system = ActorSystem("akka-oled", baseConfig)
      val clusterStatusTracker: ActorRef = system.actorOf(ClusterCRDTStatus.props(),ClusterCRDTStatus.ACTOR_NAME)

      implicit val timeout: Timeout = 3.seconds
      implicit val materializer = ActorMaterializer()
      implicit val executionContext = system.dispatcher

      val route =
         pathPrefix("status" / """[0-9a-zA-Z]+""".r) { node =>
            concat(
               get {
                  val total:Future[Option[String]] = clusterStatusTracker.ask(Get(node)).mapTo[Option[String]]
                  onSuccess(total) {
                     a: Option[String] => complete(a.getOrElse("N/A") + "\n")
                  }
               },
               post {
                  entity(as[NodeStatus]) { status =>
                     clusterStatusTracker ! UpdateStatus(node, status.status)
                     complete("Ok\n")
                  }
               }
            )
         }


      val serverSource = Http().bindAndHandle(route,
         interface = baseConfig.getString("akka.http_host"), port = 8080)

      AkkaManagement(system).start

   }
}