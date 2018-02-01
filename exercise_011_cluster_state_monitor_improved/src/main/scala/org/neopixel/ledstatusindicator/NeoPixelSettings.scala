package org.neopixel.ledstatusindicator

import org.neopixel.{Adafruit_NeoPixel, color}
import neopixel.{rpi_ws281xConstants => wsC}

object NeoPixelSettings {

  // LED strip configuration:
  val LED_COUNT = 8 // Number of LED pixels.
  val LED_PIN = 18 // GPIO pin connected to the pixels (must support PWM!).
  val LED_FREQ_HZ = 800000 // LED signal frequency in hertz (usually 800khz)
  val LED_DMA = 5 // DMA channel to use for generating signal (try 5)
  val LED_BRIGHTNESS = 10.toShort // Set to 0 for darkest and 255 for brightest
  val LED_INVERT = false // True to invert the signal (when using NPN transistor level shift)
  val LED_CHANNEL = 0
  val LED_STRIP: Int = wsC.WS2811_STRIP_RGB

  val HighestLedIndex = LED_COUNT - 1

  val LED_Black = color(0, 0, 0)
  val LED_Green = color(255, 0, 0)
  val LED_Red = color(0, 255, 0)
  val LED_Blue = color(0, 0, 255)
  val LED_Yellow = color(255, 255, 0)
  val LED_Cyan = color(255, 0, 255)
  val LED_Magenta = color(0, 255, 255)
  val LED_White = color(255, 255, 0)
  val LED_WhiteLow = color(100, 100, 100)
  val LeaderLedNumber = HighestLedIndex - 5

  def resetAllLeds(strip: Adafruit_NeoPixel.type): Unit = {
    for {
      pixel <- 0 until strip.numPixels()
    } strip.setPixelColor(pixel, LED_Black)
    strip.show()
  }

}
