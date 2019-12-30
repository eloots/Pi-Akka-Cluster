package akka.cluster.pi

import java.io.File

import akka.actor.typed.scaladsl.{ActorContext, Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}

import scala.concurrent.duration._

object SudokuProblemSender {

  sealed trait Command
  case object SendNewSudoku extends Command
  // Wrapped responses
  private final case class SolutionWrapper(result: SudokuSolver.Response) extends Command

  private val rowUpdates: Seq[SudokuDetailProcessor.RowUpdate] =
    SudokuIO.readSudokuFromFile(new File("sudokus/001.sudoku"))
      .map { case (rowIndex, update) => SudokuDetailProcessor.RowUpdate(rowIndex, update) }

  def apply(sudokuSolver: ActorRef[SudokuSolver.Command]): Behavior[Command] =
    Behaviors.setup { context =>
      Behaviors.withTimers { timers =>
        new SudokuProblemSender(sudokuSolver, context, timers).sending()
      }
    }
}

class SudokuProblemSender(sudokuSolver: ActorRef[SudokuSolver.Command],
                          context: ActorContext[SudokuProblemSender.Command],
                          timers: TimerScheduler[SudokuProblemSender.Command]) {
  import SudokuProblemSender._

  private val solutionWrapper: ActorRef[SudokuSolver.Response] =
    context.messageAdapter(response => SolutionWrapper(response))

  private val initialUpdate =
    SudokuSolver.InitialRowUpdates(rowUpdates, solutionWrapper)

  timers.startTimerAtFixedRate(SendNewSudoku, 230.millis)

  def sending(): Behavior[Command] = Behaviors.receiveMessagePartial {
    case SendNewSudoku =>
      context.log.debug("sending new sudoku problem")
      sudokuSolver ! initialUpdate
      Behaviors.same
    case SolutionWrapper(solution: SudokuSolver.SudokuSolution) =>
      context.log.info(s"${SudokuIO.sudokuPrinter(solution)}")
      Behaviors.same
  }
}

