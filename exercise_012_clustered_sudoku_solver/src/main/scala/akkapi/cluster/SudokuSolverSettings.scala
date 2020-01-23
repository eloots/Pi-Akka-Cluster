package akkapi.cluster

import com.typesafe.config.ConfigFactory
import scala.concurrent.duration.{Duration, FiniteDuration, MILLISECONDS => Millis}

import scala.concurrent.duration.{Duration, FiniteDuration}

object SudokuSolverSettings {
  private val config = ConfigFactory.load("sudokusolver")

  object ProblemSender {

    val sendInterval: FiniteDuration =
        Duration(config.getDuration("sudoku-solver.problem-sender.send-interval", Millis), Millis)
  }
}
