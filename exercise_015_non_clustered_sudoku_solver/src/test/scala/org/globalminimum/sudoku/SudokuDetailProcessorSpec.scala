package org.globalminimum.sudoku

import akka.testkit.TestProbe
import SudokuDetailProcessor.{ Update, BlockUpdate, SudokuDetailUnchanged}

class SudokuDetailProcessorSpec extends BaseAkkaSpec with SudokuTestHelpers {

  "Sending no updates to a sudoku detail processor" should {
    "result in sending a SudokuDetailUnchanged messsage" in {

      val detailParentProbe = TestProbe()
      val detailParent = detailParentProbe.ref

      val detailProcessor = system.actorOf(SudokuDetailProcessor.props[Row](id = 0))
      detailProcessor.tell(Update(cellUpdatesEmpty), detailParent)
      detailParentProbe.expectMsg(SudokuDetailUnchanged)
    }
  }

  "Sending an update to a fresh instance of the SudokuDetailProcessor that sets one cell to a single value" should {
    "result in sending an update that reflects this update" in {
      val detailParentProbe = TestProbe()
      val detailParent = detailParentProbe.ref

      val detailProcessor = system.actorOf(SudokuDetailProcessor.props[Row](id = 0))
      detailProcessor.tell(Update(List((4, Set(7)))), detailParent)

      val expectedState1 =
        SudokuDetailProcessor.RowUpdate(0, stringToIndexedUpdate(
          Vector(
            "123456 89",
            "123456 89",
            "123456 89",
            "123456 89",
            "      7  ",
            "123456 89",
            "123456 89",
            "123456 89",
            "123456 89"
          ))
        )

      detailParentProbe.expectMsg(expectedState1)
    }
  }

  "Sending a series of subsequent Updates to a SudokuDetailProcessor" should {
    "result in sending updates and ultimately return no changes" in {
      val detailParentProbe = TestProbe()
      val detailParent = detailParentProbe.ref

      val detailProcessor = system.actorOf(SudokuDetailProcessor.props[Block](id = 2))

      val update1 =
        Update(stringToReductionSet(Vector(
            "12345678 ",
            "1        ", // 1: Isolated & complete
            "   4     ", // 4: Isolated & complete
            "12 45678 ",
            "      78 ", // (7,8): Isolated & complete
            "       89",
            "      78 ", // (7,8): Isolated & complete
            "     6789",
            " 23   78 "
          )).zipWithIndex.map { _.swap}
        )

      detailProcessor.tell(update1, detailParent)

      val reducedUpdate1 = BlockUpdate(2, stringToReductionSet(Vector(
          " 23 56   ",
          "1        ",
          "   4     ",
          " 2  56   ",
          "      78 ",
          "        9",
          "      78 ",
          "     6  9",
          " 23      "
        )).zipWithIndex.map(_.swap)
      )

      detailParentProbe.expectMsg(reducedUpdate1)

      detailProcessor.tell(Update(cellUpdatesEmpty), detailParent)

      val reducedUpdate2 =
        BlockUpdate(2, stringToIndexedUpdate(
            Vector(
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "     6   ",
            ""
          ))
        )

      detailParentProbe.expectMsg(reducedUpdate2)

      detailProcessor.tell(Update(cellUpdatesEmpty), detailParent)

      val reducedUpdate3 =
        BlockUpdate(2, stringToIndexedUpdate(
          Vector(
            " 23 5    ",
            "",
            "",
            " 2  5    ",
            "",
            "",
            "",
            "",
            ""
          ))
        )

      detailParentProbe.expectMsg(reducedUpdate3)

      detailProcessor.tell(Update(cellUpdatesEmpty), detailParent)

      detailParentProbe.expectMsg(SudokuDetailUnchanged)

    }
  }

}
