package org.neopixel

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Timers}
import akka.cluster.client.ClusterClient

import scala.concurrent.duration._

object SudokuClientGenerator {

  case object SendNewSudoku

  def props(clusterClient: ActorRef): Props = Props(new SudokuClientGenerator(clusterClient))

  private val rowUpdates: Seq[SudokuDetailProcessor.RowUpdate] =
    SudokuIO.readSudokuFromFile(new File("sudokus/dm171203-03.sudoku"))
      .map { case (rowIndex, update) => SudokuDetailProcessor.RowUpdate(rowIndex, update) }

  private val initialUpdate =
    SudokuSolver.InitialRowUpdates(rowUpdates)
}

class SudokuClientGenerator(clusterClient: ActorRef) extends Actor with ActorLogging with Timers {

  import SudokuClientGenerator._

  timers.startPeriodicTimer("problem-interval", SendNewSudoku, 500.millis)

  override def receive: Receive = {
    case SendNewSudoku =>
      log.info("sending new sudoku problem")
      clusterClient ! ClusterClient.Send("/user/sudoku-solver", initialUpdate, localAffinity = false)
  }
}
