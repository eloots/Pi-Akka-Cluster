package org.globalminimum.sudoku

import java.io.File

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

object SudokuMain {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("sudoku-solver-system")

    val sudokuSolver = system.actorOf(SudokuSolver.props())

    implicit val askTimeout: Timeout = 5.seconds
    import scala.concurrent.ExecutionContext.Implicits.global

    val x = for {
      sudokuProblem <- args.toList
    } yield {

      println(s"Running solver for $sudokuProblem")
      val rowUpdates: Seq[SudokuDetailProcessor.RowUpdate] =
        SudokuIO.readSudokuFromFile(new File(sudokuProblem))
          .map { case (rowIndex, update) => SudokuDetailProcessor.RowUpdate(rowIndex, update) }

      val result = (sudokuSolver ? SudokuSolver.InitialRowUpdates(rowUpdates)).mapTo[SudokuSolver.Result]

      result
        .flatMap { x => println(s"Result ~~> ${x.sudoku.mkString("\n   ", "\n   ", "")}"); Future(println("Done !")) }
    }

    Future.sequence(x).onComplete(_ => system.terminate())
  }

}
