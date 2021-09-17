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

package akkapi.cluster

import akka.NotUsed
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, Routers}
import akka.actor.typed.{ActorSystem, Behavior, Terminated}
import akka.cluster.typed.{ClusterSingleton, SingletonActor}
import akka.management.scaladsl.AkkaManagement
import akkapi.cluster.sudoku.{SudokuSolverSettings, SudokuSolver, SudokuProblemSender}

object Main {
  def apply(settings: Settings): Behavior[NotUsed] = Behaviors.setup { context =>
    val sudokuSolverSettings = SudokuSolverSettings("sudokusolver.conf")
    // Start CLusterStatusTracker & LedStripVisualiser
    val ledStripDriver = context.spawn(LedStripDriver(settings), "led-strip-driver")
    val ledStripController = context.spawn(LedStripVisualiser(settings, ledStripDriver), "led-strip-controller")
    val clusterStatusTracker =
      context.spawn(
        ClusterStatusTracker(
          settings,
          Some(contextToClusterSingleton(settings))
        ),
        "cluster-status-tracker"
      )
    clusterStatusTracker ! ClusterStatusTracker.SubscribeVisualiser(ledStripController)

    // Start SodukuSolver: we'll run one instance/cluster node
    context.spawn(SudokuSolver(ledStripDriver, sudokuSolverSettings), s"sudoku-solver")
    // We'll use a [cluster-aware] group router
    val sudokuSolverGroup = context.spawn(Routers.group(SudokuSolver.Key).withRoundRobinRouting(), "sudoku-solvers")
    // And run one instance of the Sudoku problem sender in the cluster
    ClusterSingleton(context.system).init(SingletonActor(SudokuProblemSender(sudokuSolverGroup, sudokuSolverSettings), "sudoku-problem-sender"))

    Behaviors.receiveSignal {
      case (_, Terminated(_)) =>
        Behaviors.stopped
    }
  }

  private def contextToClusterSingleton(settings: Settings): ActorContextToSingletonBehavior  =
    (context: ActorContext[ClusterStatusTracker.ClusterEvent]) => PiClusterSingleton(settings, context.self)

  type ActorContextToSingletonBehavior = ActorContext[ClusterStatusTracker.ClusterEvent] => Behavior[PiClusterSingleton.Command]
}

object ClusterStatusTrackerMain {
  def main(args: Array[String]): Unit = {

    val osArch = System.getProperty("os.arch")
    println(s"os.arch = $osArch")

    if (System.getProperty("os.arch") == "aarch64") {
      println(s"Running on a 64-bit architecture")
      System.loadLibrary("rpi_ws281x_64")
    } else {
      println(s"Running on a 32-bit architecture")
      System.loadLibrary("rpi_ws281x")
    }

    val settings = Settings()
    val config = settings.config
    val system = ActorSystem[NotUsed](Main(settings), settings.actorSystemName, config)
    val classicSystem = system.toClassic

    // Start Akka HTTP Management extension
    AkkaManagement(classicSystem).start()
  }
}
