package akkapi.cluster.sudoku

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akkapi.cluster.{CborSerializable, LedController, LedStatusTracker}
import org.neopixel.Neopixel

import scala.concurrent.duration._

final case class SudokuField(sudoku: Sudoku)

object SudokuSolver {

  val Key: ServiceKey[Command] = ServiceKey("sudoku-processor")

  // SudokuSolver Protocol
  sealed trait Command
  final case class InitialRowUpdates(rowUpdates: Seq[SudokuDetailProcessor.RowUpdate],
                                     replyTo: ActorRef[SudokuSolver.Response]) extends Command with CborSerializable
  // Wrapped responses
  private final case class SudokuDetailProcessorResponseWrapped(response: SudokuDetailProcessor.Response) extends Command
  private final case class SudokuProgressTrackerResponseWrapped(response: SudokuProgressTracker.Response) extends Command
  // My Responses
  sealed trait Response
  final case class SudokuSolution(sudoku: Sudoku) extends Response with CborSerializable

  import SudokuDetailProcessor.UpdateSender

  def genDetailProcessors[A <: SudokoDetailType : UpdateSender](context: ActorContext[Command]): Map[Int, ActorRef[SudokuDetailProcessor.Command]] = {
    cellIndexesVector.map {
      index =>
        val detailProcessorName = implicitly[UpdateSender[A]].processorName(index)
        val detailProcessor = context.spawn(SudokuDetailProcessor(index), detailProcessorName)
        (index, detailProcessor)
    }.toMap
  }

  def apply(sudokuSolverSettings: SudokuSolverSettings): Behavior[Command] =
    Behaviors.supervise[Command] {
      Behaviors.withStash(capacity = sudokuSolverSettings.SudokuSolver.StashBufferSize) { buffer =>
        Behaviors.setup { context =>
          val ledController = LedStatusTracker(context.system).controller
          new SudokuSolver(context, buffer, ledController).idle()
        }
      }
    }.onFailure[Exception](
      SupervisorStrategy.restartWithBackoff(minBackoff = 5.seconds, maxBackoff = 1.minute, randomFactor = 0.2)
    )
}

class SudokuSolver private (context: ActorContext[SudokuSolver.Command],
                            buffer: StashBuffer[SudokuSolver.Command],
                            ledController: LedController) {
  import CellMappings._
  import SudokuSolver._

  context.system.receptionist ! Receptionist.Register(Key, context.self)

  val detailProcessorResponseMapper: ActorRef[SudokuDetailProcessor.Response] =
    context.messageAdapter(response => SudokuDetailProcessorResponseWrapped(response))
  val progressTrackerResponseMapper: ActorRef[SudokuProgressTracker.Response] =
    context.messageAdapter(response => SudokuProgressTrackerResponseWrapped(response))

  private val rowDetailProcessors    = genDetailProcessors[Row](context)
  private val columnDetailProcessors = genDetailProcessors[Column](context)
  private val blockDetailProcessors  = genDetailProcessors[Block](context)
  private val allDetailProcessors = List(rowDetailProcessors, columnDetailProcessors, blockDetailProcessors)

  private val progressTracker =
    context.spawn(SudokuProgressTracker(rowDetailProcessors, progressTrackerResponseMapper), "sudoku-progress-tracker")

  def idle(): Behavior[Command] = Behaviors.receiveMessagePartial {

    case InitialRowUpdates(rowUpdates, sender) =>
      rowUpdates.foreach {
        case SudokuDetailProcessor.RowUpdate(row, cellUpdates) =>
          rowDetailProcessors(row) ! SudokuDetailProcessor.Update(cellUpdates, detailProcessorResponseMapper)
      }
      progressTracker ! SudokuProgressTracker.NewUpdatesInFlight(rowUpdates.size)
      ledController.setLedState(9, Neopixel.Green, None)
      ledController.flashLed("MessageRcvd", 8, Neopixel.Green, 15.millis, None)
      processRequest(Some(sender), System.currentTimeMillis())

  }

  def processRequest(requestor: Option[ActorRef[Response]], startTime: Long): Behavior[Command] = Behaviors.receiveMessagePartial {
    case SudokuDetailProcessorResponseWrapped(response) => response match {
      case SudokuDetailProcessor.RowUpdate(rowNr, updates) =>
        updates.foreach {
          case (rowCellNr, newCellContent) =>

            val (columnNr, columnCellNr) = rowToColumnCoordinates(rowNr, rowCellNr)
            val columnUpdate = List((columnCellNr, newCellContent))
            columnDetailProcessors(columnNr) ! SudokuDetailProcessor.Update(columnUpdate, detailProcessorResponseMapper)

            val (blockNr, blockCellNr) = rowToBlockCoordinates(rowNr, rowCellNr)
            val blockUpdate = List((blockCellNr, newCellContent))
            blockDetailProcessors(blockNr) ! SudokuDetailProcessor.Update(blockUpdate, detailProcessorResponseMapper)
        }
        progressTracker ! SudokuProgressTracker.NewUpdatesInFlight(2 * updates.size - 1)
        Behaviors.same
      case SudokuDetailProcessor.ColumnUpdate(columnNr, updates) =>
        updates.foreach {
          case (colCellNr, newCellContent) =>

            val (rowNr, rowCellNr) = columnToRowCoordinates(columnNr, colCellNr)
            val rowUpdate = List((rowCellNr, newCellContent))
            rowDetailProcessors(rowNr) ! SudokuDetailProcessor.Update(rowUpdate, detailProcessorResponseMapper)

            val (blockNr, blockCellNr) = columnToBlockCoordinates(columnNr, colCellNr)
            val blockUpdate = List((blockCellNr, newCellContent))
            blockDetailProcessors(blockNr) ! SudokuDetailProcessor.Update(blockUpdate, detailProcessorResponseMapper)
        }
        progressTracker ! SudokuProgressTracker.NewUpdatesInFlight(2 * updates.size - 1)
        Behaviors.same
      case SudokuDetailProcessor.BlockUpdate(blockNr, updates) =>
        updates.foreach {
          case (blockCellNr, newCellContent) =>

            val (rowNr, rowCellNr) = blockToRowCoordinates(blockNr, blockCellNr)
            val rowUpdate = List((rowCellNr, newCellContent))
            rowDetailProcessors(rowNr) ! SudokuDetailProcessor.Update(rowUpdate, detailProcessorResponseMapper)

            val (columnNr, columnCellNr) = blockToColumnCoordinates(blockNr, blockCellNr)
            val columnUpdate = List((columnCellNr, newCellContent))
            columnDetailProcessors(columnNr) ! SudokuDetailProcessor.Update(columnUpdate, detailProcessorResponseMapper)
        }
        progressTracker ! SudokuProgressTracker.NewUpdatesInFlight(2 * updates.size - 1)
        Behaviors.same
      case unchanged@SudokuDetailProcessor.SudokuDetailUnchanged =>
        progressTracker ! SudokuProgressTracker.NewUpdatesInFlight(-1)
        Behaviors.same
    }
    case SudokuProgressTrackerResponseWrapped(result) => result match {
      case SudokuProgressTracker.Result(sudoku) =>
        context.log.info(s"Sudoku processing time: ${System.currentTimeMillis() - startTime} milliseconds")
        requestor.get ! SudokuSolution(sudoku)
        resetAllDetailProcessors()
        ledController.setLedState(9, Neopixel.Black, None)
        buffer.unstashAll(idle())
    }
    case msg: InitialRowUpdates if buffer.isFull =>
      context.log.info(s"DROPPING REQUEST")
      ledController.flashLed("MessageRcvd", 8, Neopixel.Red, 15.millis, None)
      Behaviors.same
    case msg: InitialRowUpdates =>
      ledController.flashLed("MessageRcvd", 8, Neopixel.Blue, 15.millis, None)
      buffer.stash(msg)
      Behaviors.same
  }

  private def resetAllDetailProcessors(): Unit = {
    for {
      processors <- allDetailProcessors
      (_, processor) <- processors
    } processor ! SudokuDetailProcessor.ResetSudokuDetailState
  }
}
