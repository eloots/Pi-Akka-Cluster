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
import akka.actor.typed.{ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.{Behaviors, Routers}
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.cluster.typed.{ClusterSingleton, SingletonActor}
import akka.management.scaladsl.AkkaManagement
import akkapi.cluster.sudoku.{SudokuProblemSender, SudokuSolver, SudokuSolverSettings}
import com.typesafe.config.ConfigFactory

object ClusterStatusTrackerMain {

  def main(args: Array[String]): Unit = {

    val actorSystemName = s"""pi-${ConfigFactory.load().getString("cluster-node-configuration.cluster-id")}-system"""

    val system = ActorSystem[NotUsed](Main(), actorSystemName)

    // Start Akka HTTP Management extension
    AkkaManagement(system.toClassic).start()
  }
}

object Main {
  def apply(): Behavior[NotUsed] = Behaviors.setup { context =>
    val sudokuSolverSettings = SudokuSolverSettings("sudokusolver.conf")

    // Start SudokuSolver: we'll run one instance/cluster node
    context.spawn(SudokuSolver(sudokuSolverSettings), s"sudoku-solver")

    // We'll use a [cluster-aware] group router
    val sudokuSolverGroup = context.spawn(Routers.group(SudokuSolver.Key).withRoundRobinRouting(), "sudoku-solvers")

    // And run one instance of the Sudoku problem sender in the cluster
    ClusterSingleton(context.system).init(SingletonActor(SudokuProblemSender(sudokuSolverGroup, sudokuSolverSettings), "sudoku-problem-sender"))

    Behaviors.receiveSignal {
      case (_, Terminated(_)) =>
        Behaviors.stopped
    }
  }
}
