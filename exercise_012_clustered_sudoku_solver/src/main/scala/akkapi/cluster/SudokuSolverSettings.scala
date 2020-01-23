package akkapi.cluster

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration.{Duration, FiniteDuration, MILLISECONDS => Millis}
import scala.concurrent.duration.{Duration, FiniteDuration}

object SudokuSolverSettings {

  def apply(configFile: String): SudokuSolverSettings =
    new SudokuSolverSettings(ConfigFactory.load(configFile))
}

class SudokuSolverSettings(config: Config) {
  object SudokuSolver {
    val stashBufferSize: Int = config.getInt("sudoku-solver.solver-stash-buffer-size")
  }

  object ProblemSender {

    val sendInterval: FiniteDuration =
      Duration(config.getDuration("sudoku-solver.problem-sender.send-interval", Millis), Millis)
  }
}