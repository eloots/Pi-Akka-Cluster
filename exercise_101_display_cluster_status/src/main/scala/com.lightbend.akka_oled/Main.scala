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

import akka.actor.ActorSystem
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.ConfigFactory

object Main {

   def main(args: Array[String]): Unit = {
      val baseConfig = ConfigFactory.load()

      val system = ActorSystem("akka-oled", baseConfig)
      val clusterStatusTracker = system.actorOf(ClusterNodeStatus.props(),ClusterNodeStatus.ACTOR_NAME)

      AkkaManagement(system).start

   }
}