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

package org.neopixel

import akka.actor.ActorSystem
import akka.cluster.client.ClusterClientReceptionist
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import akka.management.scaladsl.AkkaManagement
import neopixel.{rpi_ws281xConstants => wsC}

object ClusterStatusTrackerMain {
//  def main(args: Array[String]): Unit = {
//    System.loadLibrary("rpi_ws281x")
//
//    val baseConfig = ConfigFactory.load()
//
//    val nodeHostname = baseConfig.getString("cluster-node-configuration.node-hostname")
//
//    val config = baseConfig.withValue("akka.remote.artery.canonical.hostname", ConfigValueFactory.fromAnyRef(nodeHostname))
//
//    val actorSystemName = s"pi-${config.getString("cluster-node-configuration.cluster-id")}-system"
//
//    val system = ActorSystem(actorSystemName, config)
//
//    val settings = ConfigSettingsExtension(system)
//
//    import settings.LedStripConfig._
//
//    val strip = Adafruit_NeoPixel(ledCount, ledPin, ledFreqHz, ledDma, ledInvert, ledBrightness, ledChannel, wsC.WS2811_STRIP_RGB)
//
//    val clusterStatusTracker = system.actorOf(ClusterStatusTracker.props(strip), "cluster-status-tracker")
//
//    AkkaManagement(system).start
//
//    val sudokuSolver = system.actorOf(SudokuSolver.props(), "sudoku-solver")
//
//  }
}
