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

package akka.cluster.pi

import akka.NotUsed
import akka.actor.typed.scaladsl.{Behaviors, Routers}
import akka.actor.typed.{ActorSystem, Behavior, Terminated}
import akka.cluster.typed.{ClusterSingleton, SingletonActor}
import akka.management.scaladsl.AkkaManagement

object Main {
  def apply(settings: Settings): Behavior[NotUsed] = Behaviors.setup { context =>
    // Start CLusterStatusTracker & LedStripVisualiser
    val ledStripController = context.spawn(LedStripVisualiser(settings), "led-strip-controller")
    val clusterStatusTracker = context.spawn(ClusterStatusTracker(settings), "cluster-status-tracker")
    clusterStatusTracker ! ClusterStatusTracker.SubscribeVisualiser(ledStripController)

    // Start SodukuSolver: we'll run one instance/cluster node
    context.spawn(SudokuSolver(), s"sudoku-solver")
    // We'll use a [cluster-aware] group router
    val sudokuSolverGroup = context.spawn(Routers.group(SudokuSolver.Key), "sudoku-solvers")
    // And run one instance if the Sudoku problem sender in the cluster
    ClusterSingleton(context.system).init(SingletonActor(SudokuProblemSender(sudokuSolverGroup), "sudoku-problem-sender"))

    Behaviors.receiveSignal {
      case (_, Terminated(_)) =>
        Behaviors.stopped
    }
  }
}

object ClusterStatusTrackerMain {
  def main(args: Array[String]): Unit = {
    System.loadLibrary("rpi_ws281x")

    val settings = Settings()
    val config = settings.config
    val system = ActorSystem[NotUsed](Main(settings), settings.actorSystemName, config)
    val classicSystem = system.classicSystem

    // Start Akka HTTP Management extension
    AkkaManagement(classicSystem).start()
  }
}
