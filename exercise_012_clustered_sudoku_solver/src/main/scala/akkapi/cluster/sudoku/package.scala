package akkapi.cluster

package object sudoku {

  type Seq[+A] = scala.collection.immutable.Seq[A]
  val Seq = scala.collection.immutable.Seq

  private val N = 9
  val CELLPossibleValues: Vector[Int] = (1 to N).toVector
  val cellIndexesVector: Vector[Int] = (0 until N).toVector
  val initialCell: Set[Int] = Set(1 to N: _*)

  type CellContent = Set[Int]
  type ReductionSet = Vector[CellContent]
  type Sudoku = Vector[ReductionSet]

  type CellUpdates = Seq[(Int, Set[Int])]
  val cellUpdatesEmpty = Seq.empty[(Int, Set[Int])]

  import SudokuDetailProcessor.RowUpdate

  implicit class RowUpdatesToSudokuField(val update: Seq[SudokuDetailProcessor.RowUpdate]) extends AnyVal {
    def toSudokuField: SudokuField = {
      val rows =
        update
          .map { case SudokuDetailProcessor.RowUpdate(id, cellUpdates) => (id, cellUpdates)}
        .to(Map).withDefaultValue(cellUpdatesEmpty)
      val sudoku = for {
        (row, cellUpdates) <- Vector.range(0, 9).map(row => (row, rows(row)))
        x = cellUpdates.to(Map).withDefaultValue(Set(0))
        y = Vector.range(0, 9).map(n => x(n))
        } yield y
      SudokuField(sudoku)
    }
  }

  implicit class SudokuFieldOps(val sudokuField: SudokuField) extends AnyVal {
    def transpose: SudokuField = SudokuField(sudokuField.sudoku.transpose)

    def rotateCW: SudokuField = SudokuField(sudokuField.sudoku.reverse.transpose)

    def rotateCCW: SudokuField = SudokuField(sudokuField.sudoku.transpose.reverse)

    def flipVertically: SudokuField = SudokuField(sudokuField.sudoku.reverse)

    def flipHorizontally: SudokuField = sudokuField.rotateCW.flipVertically.rotateCCW

    def rowSwap(row1: Int, row2: Int): SudokuField = {
      SudokuField(
        sudokuField.sudoku.zipWithIndex.map {
        case (_, `row1`) => sudokuField.sudoku(row2)
        case (_, `row2`) => sudokuField.sudoku(row1)
        case (row, _) => row
        }
      )
    }

    def columnSwap(col1: Int, col2: Int): SudokuField = {
      sudokuField.rotateCW.rowSwap(col1, col2).rotateCCW
    }

    def remapNumbers(mapping: Map[Int, Int]): SudokuField = {
      require(mapping.keySet == mapping.values.to(Set))
      val mapping_0 = mapping + (0 -> 0)
      SudokuField(sudokuField.sudoku.map { row =>
        row.map(cell => Set(mapping_0(cell.head)))
      })
    }

    def randomSwapAround: SudokuField = {
      val swapAround = scala.util.Random.shuffle(Vector(1,2,3,4,5,6,7,8,9)).zipWithIndex.map{ case (a, b) => (a, b + 1)}.to(Map) + (0 -> 0)
      SudokuField(sudokuField.sudoku.map { row =>
        row.map(cell => Set(swapAround(cell.head)))
      })
    }

    def toRowUpdates: Seq[RowUpdate] = {
      sudokuField
        .sudoku
        .map(_.zipWithIndex)
        .map(row => row.filterNot(_._1 == Set(0)))
        .zipWithIndex.filter(_._1.nonEmpty)
        .map { case (c, i) =>
          RowUpdate(i, c.map(_.swap))
        }
    }
  }
}
