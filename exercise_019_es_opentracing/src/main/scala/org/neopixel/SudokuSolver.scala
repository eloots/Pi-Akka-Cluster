package org.neopixel

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, Props, Stash}
import org.neopixel.SudokuDetailProcessor.UpdateSender

object SudokuSolver {

  case class SetInitialCells(updates: CellUpdates)

  case class InitialRowUpdates(rowUpdates: Seq[SudokuDetailProcessor.RowUpdate])
  case class Result(sudoku: Sudoku)

  def genDetailProcessors[A <: SudokoDetailType : UpdateSender](context: ActorContext): Map[Int, ActorRef] = {

    cellIndexesVector.map {
      index =>
        val detailProcessorName = implicitly[UpdateSender[A]].processorName(index)
        val detailProcessor = context.actorOf(SudokuDetailProcessor.props[A](index), detailProcessorName)
        (index, detailProcessor)
    }.toMap
  }

  def props(): Props = Props(new SudokuSolver)
}

class SudokuSolver extends Actor with ActorLogging with Stash {

  import SudokuSolver._

  private val rowDetailProcessors    = genDetailProcessors[Row](context)
  private val columnDetailProcessors = genDetailProcessors[Column](context)
  private val blockDetailProcessors  = genDetailProcessors[Block](context)
  private val allDetailProcessors = List(rowDetailProcessors, columnDetailProcessors, blockDetailProcessors)

  private val progressTracker = context.actorOf(SudokuProgressTracker.props(rowDetailProcessors), "sudoku-progress-tracker")

  import org.neopixel.CellMappings._

  override def receive: Receive = idle

  def idle: Receive = {

    case initialUpdate @ InitialRowUpdates(rowUpdates) =>
      rowUpdates.foreach {
        case SudokuDetailProcessor.RowUpdate(row, cellUpdates) =>
          rowDetailProcessors(row) ! SudokuDetailProcessor.Update(cellUpdates)
      }
      progressTracker ! SudokuProgressTracker.NewUpdatesInFlight(rowUpdates.size)
      context.become(processRequest(Some(sender())))
      log.info(s"Received sudo problem: $initialUpdate")

  }

  def processRequest(requestor: Option[ActorRef]): Receive = {
    case SudokuDetailProcessor.RowUpdate(rowNr, updates) =>
      updates.foreach {
        case (rowCellNr, newCellContent) =>

          val (columnNr, columnCellNr) = rowToColumnCoordinates(rowNr, rowCellNr)
          val columnUpdate = List((columnCellNr, newCellContent))
          columnDetailProcessors(columnNr) ! SudokuDetailProcessor.Update(columnUpdate)

          val (blockNr, blockCellNr) = rowToBlockCoordinates(rowNr, rowCellNr)
          val blockUpdate = List((blockCellNr, newCellContent))
          blockDetailProcessors(blockNr) ! SudokuDetailProcessor.Update(blockUpdate)
      }
      progressTracker ! SudokuProgressTracker.NewUpdatesInFlight(2 * updates.size - 1)

    case SudokuDetailProcessor.ColumnUpdate(columnNr, updates) =>
      updates.foreach {
        case (colCellNr, newCellContent) =>

          val (rowNr, rowCellNr) = columnToRowCoordinates(columnNr, colCellNr)
          val rowUpdate = List((rowCellNr, newCellContent))
          rowDetailProcessors(rowNr) ! SudokuDetailProcessor.Update(rowUpdate)

          val (blockNr, blockCellNr) = columnToBlockCoordinates(columnNr, colCellNr)
          val blockUpdate = List((blockCellNr, newCellContent))
          blockDetailProcessors(blockNr) ! SudokuDetailProcessor.Update(blockUpdate)
      }
      progressTracker ! SudokuProgressTracker.NewUpdatesInFlight(2 * updates.size - 1)

    case SudokuDetailProcessor.BlockUpdate(blockNr, updates) =>
      updates.foreach {
        case (blockCellNr, newCellContent) =>

          val (rowNr, rowCellNr) = blockToRowCoordinates(blockNr, blockCellNr)
          val rowUpdate = List((rowCellNr, newCellContent))
          rowDetailProcessors(rowNr) ! SudokuDetailProcessor.Update(rowUpdate)

          val (columnNr, columnCellNr) = blockToColumnCoordinates(blockNr, blockCellNr)
          val columnUpdate = List((columnCellNr, newCellContent))
          columnDetailProcessors(columnNr) ! SudokuDetailProcessor.Update(columnUpdate)
      }
      progressTracker ! SudokuProgressTracker.NewUpdatesInFlight(2 * updates.size - 1)

    case unchanged @ SudokuDetailProcessor.SudokuDetailUnchanged =>
      progressTracker ! unchanged

    case result @ Result(sudoku) =>
      requestor.get ! result
      resetAllDetailProcessors()
      unstashAll()
      context.become(idle)
      log.info(
        s"""Result:
           |${result.sudoku.mkString("\n   ", "\n   ", "")}
         """.stripMargin)

    case _ =>
      stash()
  }

  private def resetAllDetailProcessors(): Unit = {
    for {
      processors <- allDetailProcessors
      (_, processor) <- processors
    } processor ! SudokuDetailProcessor.ResetSudokuDetailState
  }
}