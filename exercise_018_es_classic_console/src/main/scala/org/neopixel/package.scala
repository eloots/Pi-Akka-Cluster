/**
  * Copyright © 2016-2019 Lightbend, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * NO COMMERCIAL SUPPORT OR ANY OTHER FORM OF SUPPORT IS OFFERED ON
  * THIS SOFTWARE BY LIGHTBEND, Inc.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package org

import _root_.neopixel.{rpi_ws281x => ws}
import _root_.neopixel.ws2811_t
import _root_.neopixel.{rpi_ws281xConstants => wsC}
import _root_.neopixel.{rpi_ws281xJNI => wsJ}
import _root_.neopixel.ws2811_channel_t
import _root_.neopixel.{ws2811_return_t => wsRet}

import scala.collection.mutable.ListBuffer

package object neopixel {

  sealed trait SudokoDetailType
  case class Row(id: Int) extends SudokoDetailType
  case class Column(id: Int) extends SudokoDetailType
  case class Block(id: Int) extends SudokoDetailType

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

  object ReductionRules {

    def reductionRuleOne(reductionSet: ReductionSet): ReductionSet = {
      val inputCellsGrouped = reductionSet filter {_.size <= 7} groupBy identity
      val completeInputCellGroups = inputCellsGrouped filter {
        case (set, setOccurrences) => set.size == setOccurrences.length
      }
      val completeAndIsolatedValueSets = completeInputCellGroups.keys.toList
      (completeAndIsolatedValueSets foldLeft reductionSet) {
        case (cells, caivSet) => cells map {
          cell => if (cell != caivSet) cell &~ caivSet else cell
        }
      }
    }

    def reductionRuleTwo(reductionSet: ReductionSet): ReductionSet = {
      val valueOccurrences = CELLPossibleValues map { value =>
        (cellIndexesVector zip reductionSet foldLeft Vector.empty[Int]) {
          case (acc, (index, cell)) =>
            if (cell contains value) index +: acc else acc
        }
      }

      val cellIndexesToValues =
        (CELLPossibleValues zip valueOccurrences)
          .groupBy { case (value, occurrence) => occurrence}
          .filter  { case (loc, occ) => loc.length == occ.length && loc.length <= 6 }

      val cellIndexListToReducedValue = cellIndexesToValues map {
        case (index, seq) => (index, (seq map { case (value, _) => value }).toSet)
      }

      val cellIndexToReducedValue = cellIndexListToReducedValue flatMap {
        case (cellIndexList, reducedValue) =>
          cellIndexList map { cellIndex => cellIndex -> reducedValue }
      }

      (reductionSet.zipWithIndex foldRight Vector.empty[CellContent]) {
        case ((cellValue, cellIndex), acc) =>
          cellIndexToReducedValue.getOrElse(cellIndex, cellValue) +: acc
      }
    }
  }

  val Black = color(0, 0, 0)
  val Green = color(255, 0, 0)
  val MediumGreen = color(95, 0, 0)
  val DarkGreen = color(60, 0, 0)
  val Orange = color(255, 165, 0)
  val DarkOrange = color(255, 140, 0)
  val Red = color(0, 255, 0)
  val DarkRed = color(0, 100, 0)
  val Blue = color(0, 0, 255)
  val LightBlue = color(40, 40, 255)
  val DarkBlue = color(0, 0, 100)
  val Yellow = color(255, 255, 0)
  val Cyan = color(255, 0, 255)
  val Magenta = color(0, 255, 255)
  val White = color(255, 255, 255)
  val WhiteLow = color(100, 100, 100)

  val availableColorMap: Map[String, Long] =
    Map(
      "Black" -> Black,
      "Green" -> Green,
      "MediumGreen" -> MediumGreen,
      "DarkGreen" -> DarkGreen,
      "Orange" -> Orange,
      "DarkOrange" -> DarkOrange,
      "Red" -> Red,
      "DarkRed" -> DarkRed,
      "Blue" -> Blue,
      "DarkBlue" -> DarkBlue,
      "Yellow" -> Yellow,
      "Cyan" -> Cyan,
      "Magenta" -> Magenta,
      "White" -> White,
      "WhiteLow" -> WhiteLow
    )

  def color(red: Int, green: Int, blue: Int, white: Int = 0): Long =
    (white << 24) | (red << 16)| (green << 8) | blue

  case class LED_Data (channel: ws2811_channel_t, size: Int) {
//    val ledData: Array[Long] = Array.fill[Long](size)(0L)
    val ledData = ListBuffer.fill[Long](size)(0L)

    def getitem(pos: Int): Long = ws.ws2811_led_get(channel, pos)

    def setitem(pos: Int, value: Long) = {
      ledData(pos) = value
      ws.ws2811_led_set(channel, pos, value)
    }
  }

  object Adafruit_NeoPixel {

    var leds: ws2811_t = _
    var led_data: LED_Data = _
    var thisChannel: ws2811_channel_t = _

    def apply(num: Int, pin: Int, freq_hz: Long = 800000, dma: Int = 5, invert: Boolean = false,
              brightness: Short = 255, channel: Int = 0, strip_type: Int = wsC.WS2811_STRIP_RGB): Adafruit_NeoPixel.type = {

      // Initialize the channels to zero
      leds = new ws2811_t()
      for { channum <- 0 to 1 } {
        val chan = ws.ws2811_channel_get(leds, channum)
        chan.setCount(0)
        chan.setGpionum(0)
        chan.setInvert(0)
        chan.setBrightness(0)
      }

      // Initialize the channel in use
      thisChannel = ws.ws2811_channel_get(leds, channel)
      thisChannel.setCount(num)
      thisChannel.setGpionum(pin)
      thisChannel.setInvert(if (invert) 1 else 0)
      thisChannel.setBrightness(brightness)

      // Initialize the controller
      leds.setFreq(freq_hz)
      leds.setDmanum(dma)

      led_data = LED_Data(thisChannel, num)

      this
    }

    def begin(): Unit = {
      val resp = ws.ws2811_init(leds)
      if (resp != wsRet.WS2811_SUCCESS) {
        val message = ws.ws2811_get_return_t_str(resp)
        println(s"ws2811_init failed with code $resp ($message)")
        System.exit(-1)
      }
    }

    def show(): Unit = {
      val resp = ws.ws2811_render(leds)
      if (resp != wsRet.WS2811_SUCCESS) {
        val message = ws.ws2811_get_return_t_str(resp)
        println(s"ws2811_render failed with code $resp ($message)")
      }
    }

    def setPixelColor(n: Int, color: Long): Unit = {
      led_data.setitem(n, color)
    }

    def setPixelColorRGB(n: Int, red: Int, green: Int, blue: Int, white: Int): Unit = {
      led_data.setitem(n, color(red, green, blue, white))
    }

    def setBrightness(brightness: Short): Unit = {
      thisChannel.setBrightness(brightness)
    }

    def numPixels(): Int = thisChannel.getCount

    def getPixelColor(n: Int): Long = led_data.ledData(n)

    def cleanup(): Unit =
      if (leds != null) {
        ws.ws2811_fini(leds)
      }
  }

}
