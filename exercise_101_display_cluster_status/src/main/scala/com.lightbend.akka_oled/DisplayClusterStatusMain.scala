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
import akka.actor.typed.{ActorSystem, Behavior, Terminated}
import akka.management.scaladsl.AkkaManagement
import akkapi.cluster.{ClusterStatusTracker, OledClusterVisualizer, OledDriver, Settings}


object Main {
  def apply(settings: Settings): Behavior[NotUsed] = Behaviors.setup { context =>
    val oledDriver = context.spawn(OledDriver(settings), "oled-driver")
    val clusterView = context.spawn(OledClusterVisualizer(0, settings, oledDriver), "oled-cluster-view")
    oledDriver ! OledDriver.RegisterView("Cluster State", 0)
    val clusterStatusTracker = context.spawn(ClusterStatusTracker(settings, None), "cluster-status-tracker")
    clusterStatusTracker ! ClusterStatusTracker.SubscribeVisualiser(clusterView)
    Behaviors.receiveSignal {
      case (_, Terminated(_)) =>
        Behaviors.stopped
    }
  }
}

object DisplayClusterStatusMain {
  def main(args: Array[String]): Unit = {
    val settings = Settings()
    val config = settings.config
    val system = ActorSystem[NotUsed](Main(settings), "akka-oled", config)

    // Start Akka HTTP Management extension
    AkkaManagement(system).start()
  }
}


