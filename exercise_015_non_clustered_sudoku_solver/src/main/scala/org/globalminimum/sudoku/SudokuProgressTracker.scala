package org.globalminimum.sudoku

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object SudokuProgressTracker {

  case class NewUpdatesInFlight(count: Int)

  def props(rowDetailProcessors: Map[Int, ActorRef]): Props =
    Props(new SudokuProgressTracker(rowDetailProcessors))

}

class SudokuProgressTracker(rowDetailProcessors: Map[Int, ActorRef]) extends Actor with ActorLogging {

  import SudokuProgressTracker._

  override def receive: Receive = trackProgress(updatesInFlight = 0)

  def trackProgress(updatesInFlight: Int) : Receive = {
    case NewUpdatesInFlight(updateCount) =>
      context.become(trackProgress(updatesInFlight + updateCount))

    case SudokuDetailProcessor.SudokuDetailUnchanged if updatesInFlight - 1 == 0 =>
      rowDetailProcessors.foreach { case (_, processor) => processor ! SudokuDetailProcessor.GetSudokuDetailState }
      context.become(collectEndState())

    case SudokuDetailProcessor.SudokuDetailUnchanged =>
      context.become(trackProgress(updatesInFlight - 1))
  }

  def collectEndState(remainingRows: Int = 9, endState: Vector[SudokuDetailProcessor.SudokuDetailState] = Vector.empty[SudokuDetailProcessor.SudokuDetailState]): Receive = {
    case detail @ SudokuDetailProcessor.SudokuDetailState(index, state) if remainingRows == 1 =>
      context.parent ! SudokuSolver.Result((detail +: endState).sortBy { case SudokuDetailProcessor.SudokuDetailState(idx, _) => idx }.map { case SudokuDetailProcessor.SudokuDetailState(_, state) => state})
      context.become(trackProgress(updatesInFlight = 0))

    case detail @ SudokuDetailProcessor.SudokuDetailState(index, state) =>
      context.become(collectEndState(remainingRows = remainingRows - 1, detail +: endState))
  }

}
