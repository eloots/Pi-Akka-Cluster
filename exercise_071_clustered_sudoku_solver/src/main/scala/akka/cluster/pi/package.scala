package akka.cluster

package object pi {

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
}
