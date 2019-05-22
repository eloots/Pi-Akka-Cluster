package org.neopixel

import akka.actor._
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.server.{Directives, Route}
import akka.japi.Util.immutableSeq
import akka.pattern.AskTimeoutException
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.io.StdIn
import scala.util.{Failure, Success}

case class SudokuMessage(values: Seq[Seq[Int]])

case class SudokuSolution(solution: Sudoku)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val sudokuMessageFormat = jsonFormat1(SudokuMessage)
  implicit val sudokuSolutionFormat = jsonFormat1(SudokuSolution)
}

/**
  * Adds a simple HTTP server that handles JSON, converts it into an internal Sudoku format, sends the Sudoku over to
  * the cluster, and returns the result as a HTTP response.
  *
  * The JSON format is a bit different from the file based formats in that it does not contain spaces, which instead are represented as the number 0.
  * Also, there are no delimiters used. The layout is expected to be 9x9.
  *
  * Example JSON format:
  * {"values": [
  *   [0,6,0,0,2,0,9,0,7],
  *   [0,0,0,3,6,9,0,0,0],
  *   [0,0,1,0,4,0,0,0,0],
  *   [0,9,8,0,0,5,0,0,0],
  *   [0,2,0,6,0,0,3,0,0],
  *   [0,0,0,0,0,0,0,0,9],
  *   [0,0,2,8,0,0,0,0,1],
  *   [0,0,0,0,0,7,0,6,0],
  *   [0,5,0,0,0,0,0,0,0]
  * ]}
  *
  * Example curl command:
  * curl --header "Content-Type: application/json" --request POST --data '{ "values": [[0,6,0,0,2,0,9,0,7], [0,0,0,3,6,9,0,0,0], [0,0,1,0,4,0,0,0,0], [0,9,8,0,0,5,0,0,0], [0,2,0,6,0,0,3,0,0], [0,0,0,0,0,0,0,0,9], [0,0,2,8,0,0,0,0,1], [0,0,0,0,0,7,0,6,0], [0,5,0,0,0,0,0,0,0]]}' localhost:8080/sudoku
  *
  */
object AkkaHttpServer extends Directives with JsonSupport {
  def main(args: Array[String]): Unit = {
    implicit val askTimeout: Timeout = 5000.millis

    val conf = ConfigFactory.load("sudokuclient")

    implicit val system = ActorSystem("sudoku-solver-system", conf)
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val clusterClient = initiateClusterClient(system)

    def sudokuRouteWithFormatter[A : ToResponseMarshaller](formatter: SudokuSolver.Result => A): Route =
      post {
        entity(as[SudokuMessage]) { sudokuMessage =>
          val sudokuInitialUpdate =  createSudokuMessage(sudokuMessage)
          onComplete(askClusterClient(clusterClient, sudokuInitialUpdate).mapTo[SudokuSolver.Result]) {
            case Success(solution) => complete(formatter(solution))
            case Failure(ex) => complete("Could not solve Sudoku.")
          }
        }
      }

    val routes =
      path("sudoku") {
        sudokuRouteWithFormatter[String](SudokuIO.sudokuPrinter)
      } ~ pathPrefix("sudoku" / "asJSON") {
        sudokuRouteWithFormatter[Sudoku]{ solution: SudokuSolver.Result => solution.sudoku }
      }

    val bindingFuture = Http().bindAndHandle(routes, "localhost", 8084)
    println(s"Server online at http://localhost:8084/\nPress RETURN to stop...")

    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  def askClusterClient(clusterClient: ActorRef, msg: Any)(implicit system: ActorSystem, askTimeout: Timeout): Future[SudokuSolver.Result] = {
    val resultPromise = Promise[SudokuSolver.Result]()

    case object TimeoutReached
    system.actorOf(Props(new Actor with ActorLogging with Timers {

      clusterClient ! ClusterClient.Send("/user/sudoku-solver", msg, localAffinity = false)

      timers.startSingleTimer("request-timeout", TimeoutReached, askTimeout.duration)

      override def receive: Receive = {
        case result: SudokuSolver.Result =>
          resultPromise.complete(Success(result))
          context.stop(self)

        case TimeoutReached =>
          resultPromise.failure(new AskTimeoutException("request timed out"))
          context.stop(self)
      }
    }))

    resultPromise.future
  }

  def initiateClusterClient(system: ActorSystem): ActorRef = {

    val initialContacts = immutableSeq(system.settings.config.getStringList("contact-points")).map {
      case AddressFromURIString(addr) => RootActorPath(addr) / "system" / "receptionist"
    }.toSet

    system.actorOf(
      ClusterClient.props(
        ClusterClientSettings(system)
          .withInitialContacts(initialContacts)),
      "clusterClient")
  }

  def createSudokuMessage(sudokuMessage: SudokuMessage): SudokuSolver.InitialRowUpdates = {
    val cellsIn = sudokuMessage.values.map { cellsInRow =>
      if (cellsInRow.size != 9) throw new IllegalArgumentException(s"Each Sudoku row must contain 9 numbers, not[$cellsInRow]")
      else {
        val sb = new StringBuilder(9);
        cellsInRow foreach { i =>
          if (i > 0) sb.append(i.toString)
          else sb.append(" ")
        }
        sb.toString
      }
    }.zipWithIndex.toList

    if (cellsIn.size != 9) throw new IllegalArgumentException("The Sudoku must have 9x9 cells.")

    val rowUpdates =
      SudokuIO.convertFromCellsToComplete(cellsIn).map {
        case (rowIndex, update) => SudokuDetailProcessor.RowUpdate(rowIndex, update)
      }

    SudokuSolver.InitialRowUpdates(rowUpdates)
  }
}


