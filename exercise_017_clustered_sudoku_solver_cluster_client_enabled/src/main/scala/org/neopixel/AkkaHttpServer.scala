package org.neopixel

import akka.actor._
import akka.pattern.ask
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.japi.Util.immutableSeq
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol

import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.{Failure, Success}

case class SudokuMessage(values: Seq[Seq[Int]])

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val sudokuMessageFormat = jsonFormat1(SudokuMessage)
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
  *   [9,0,0,0,0,4,0,8,0],
  *   [0,0,5,0,7,0,0,0,0],
  *   [0,7,1,0,5,2,0,6,0],
  *   [0,8,0,0,0,0,0,0,3],
  *   [0,5,0,9,6,0,0,0,0],
  *   [0,0,9,7,0,0,4,0,5],
  *   [0,0,0,5,0,0,0,0,2],
  *   [0,0,0,0,0,0,0,0,0],
  *   [0,0,2,0,0,3,0,5,4]
  * ]}
  *
  * Example curl command:
  * curl --header "Content-Type: application/json" --request POST --data '{ "values": [[9,0,0,0,0,4,0,8,0],[0,0,5,0,7,0,0,0,0],[0,7,1,0,5,2,0,6,0],[0,8,0,0,0,0,0,0,3],[0,5,0,9,6,0,0,0,0],[0,0,9,7,0,0,4,0,5],[2],[0,0,0,0,0,0,0,0,0],[0,0,2,0,0,3,0,5,4]]}' localhost:8080/sudoku
  *
  */
object AkkaHttpServer extends Directives with JsonSupport {
  def main(args: Array[String]): Unit = {
    implicit val askTimeout: Timeout = 5.seconds
    implicit val system = ActorSystem("sudoku-solver-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val clusterClient = initiateClusterClient(system)

    val routes =
      path("sudoku") {
        post {
          entity(as[SudokuMessage]) { sudokuMessage =>
            onComplete((clusterClient ? createSudokuMessage(sudokuMessage)).mapTo[SudokuSolver.Result]) {
              case Success(solution) => complete(solution.toString) // FIXME: Improved formatting of response would be nice.
              case Failure(ex) => complete("Could not solve Sudoku.")
            }
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  def initiateClusterClient(system: ActorSystem): ActorRef = {
    val conf = ConfigFactory.load("sudokuclient")

    val initialContacts = immutableSeq(conf.getStringList("contact-points")).map {
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


