package org.neopixel

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Timers}

import scala.concurrent.duration._

object SudokuProblemSender {

  case object SendNewSudoku

  def props(sudokuSolver: ActorRef): Props = Props(new SudokuProblemSender(sudokuSolver))

  private val rowUpdates: Seq[SudokuDetailProcessor.RowUpdate] =
    SudokuIO.readSudokuFromFile(new File("sudokus/001.sudoku"))
      .map { case (rowIndex, update) => SudokuDetailProcessor.RowUpdate(rowIndex, update) }

  private val initialUpdate =
    SudokuSolver.InitialRowUpdates(rowUpdates)
}

class SudokuProblemSender(sudokuSolver: ActorRef) extends Actor with ActorLogging with Timers {

  import SudokuProblemSender._

  timers.startPeriodicTimer("problem-send-interval", SendNewSudoku, 500.millis)

  override def receive: Receive = {
    case SendNewSudoku =>
      log.debug("sending new sudoku problem")
      sudokuSolver ! initialUpdate
  }
}
