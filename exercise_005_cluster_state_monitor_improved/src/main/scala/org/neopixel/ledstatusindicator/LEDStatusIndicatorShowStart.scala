package org.neopixel.ledstatusindicator

import akka.actor.{Actor, ActorLogging, Props}
import org.neopixel.Adafruit_NeoPixel

object LEDStatusIndicatorShowStart {

  import scala.concurrent.duration._
  private val blinkInterval = 10.milliseconds

  case object Blink

  def props(strip: Adafruit_NeoPixel.type, ledCount: Int, nSweeps: Int = 2): Props =
    Props(new LEDStatusIndicatorShowStart(strip, ledCount: Int, nSweeps))
}

class LEDStatusIndicatorShowStart(strip: Adafruit_NeoPixel.type, ledCount: Int, nSweeps: Int, initialDirection: Int = 1) extends Actor with ActorLogging {
  import LEDStatusIndicatorShowStart._
  import NeoPixelSettings._
  import context.dispatcher

  override def receive: Receive = slideUpOrDown(index = 0)

  self ! Blink

  def slideUpOrDown(index: Int): Receive = {

    case Blink if index == ledCount * nSweeps =>
      resetAllLeds(strip)
      strip.show()
      context.stop(self)

    case Blink =>
      val direction = (index / ledCount + initialDirection) % 2
      val ledNumber = if (direction == 0) index % ledCount else 7 - index % ledCount
      // log.debug(s"Sliding index=$index, direction=$direction, ledNumber=$ledNumber")
      resetAllLeds(strip)
      strip.setPixelColor(ledNumber, LED_Red)
      strip.show()
      scheduleBlink()
      context.become(slideUpOrDown(index + 1))
  }

  private def scheduleBlink(): Unit = {
    context.system.scheduler.scheduleOnce(blinkInterval, self, Blink)
  }

}
