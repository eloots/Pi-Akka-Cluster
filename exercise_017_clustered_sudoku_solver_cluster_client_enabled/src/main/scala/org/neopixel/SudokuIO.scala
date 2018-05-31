package org.neopixel

object SudokuIO {

  def printRow( row: ReductionSet): String = {
    def printSubRow( subRowNo: Int): String = {
      val printItems = List(1,2,3) map( x => x + subRowNo * 3)
      (for { elem <- row }
        yield {
          (printItems map (item => if ((elem & printItems.toSet).contains(item)) item.toString else " ")).mkString("")
        }).mkString("| ", " | ", " |")
    }
    (for { subRow <- 0 until 3 } yield printSubRow(subRow)).mkString("\n")
  }

  def printRowShort( row: ReductionSet): String = {
    (for {
      elem <- row
    } yield {
      if (elem.size == 1) elem.head.toString else " "
    }).mkString("|","|","|")

  }

  private def sudokuCellRepresentation(content: CellContent): String = {
    content.toList match {
      case Nil => "x"
      case singleValue +: Nil => singleValue.toString
      case _ => " "
    }
  }

  private def sudokuRowPrinter(threeRows: Vector[ReductionSet]): String = {
    val rowSubBlocks = for {
      row <- threeRows
      rowSubBlock <- row.map(el => sudokuCellRepresentation(el)).sliding(3,3)
      rPres = rowSubBlock.mkString

    } yield rPres
    rowSubBlocks.sliding(3,3).map(_.mkString("", "|", "")).mkString("|", "|\n|", "|\n")
  }

  def sudokuPrinter(result: SudokuSolver.Result): String = {
    result.sudoku
      .sliding(3,3)
      .map(sudokuRowPrinter)
      .mkString("+---+---+---+\n", "+---+---+---+\n", "+---+---+---+")
  }

  /*
   * FileLineTraversable code taken from "Scala in Depth" by Joshua Suereth
   */

  import java.io.{BufferedReader, File, FileReader}

  import scala.language.postfixOps
  class FileLineTraversable(file: File) extends Traversable[String] {
    override def foreach[U](f: String => U): Unit = {
      val input = new BufferedReader(new FileReader(file))
      try {
        var line = input.readLine
        while (line != null) {
          f(line)
          line = input.readLine
        }
      } finally {
        input.close()
      }
    }

    override def toString: String =
      "{Lines of " + file.getAbsolutePath + "}"
  }

  def convertFromCellsToComplete(cellsIn: List[(String, Int)]): Seq[(Int, CellUpdates)] =
    for {
      (rowCells, row) <- cellsIn
      updates = (rowCells.zipWithIndex foldLeft cellUpdatesEmpty) {
        case (cellUpdates, (c, index)) if c != ' ' =>
          (index, Set(c.toString.toInt)) +: cellUpdates
        case (cellUpdates, _) => cellUpdates
      }

    } yield (row, updates)


  def readSudokuFromFile(sudokuInputFile: java.io.File): Seq[(Int, CellUpdates)] = {
    val dataLines = new FileLineTraversable(sudokuInputFile).toList
    val cellsIn =
      dataLines
        .map { inputLine => """\|""".r replaceAllIn(inputLine, "")}     // Remove 3x3 separator character
        .filter (_ != "---+---+---")              // Remove 3x3 line separator
        .map ("""^[1-9 ]{9}$""".r findFirstIn(_)) // Input data should only contain values 1-9 or ' '
        .collect { case Some(x) => x}
        .zipWithIndex

    convertFromCellsToComplete(cellsIn)
  }
}
