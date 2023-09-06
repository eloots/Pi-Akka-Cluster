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
package akkapi.cluster

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.ConfigFactory

object ClusterStatusTrackerMain {

  def main(args: Array[String]): Unit = {

    val actorSystemName = s"""pi-${ConfigFactory.load().getString("cluster-node-configuration.cluster-id")}-system"""

    val system = ActorSystem[NotUsed](Behaviors.ignore, actorSystemName)

    LedStatusTracker(system).start()

    // Start Akka HTTP Management extension
    AkkaManagement(system.toClassic).start()
  }
}
