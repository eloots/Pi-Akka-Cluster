package org.neopixel

import neopixel.{rpi_ws281x => ws, rpi_ws281xConstants => wsC}

object Main {
  def main(args: Array[String]): Unit = {
    System.loadLibrary("rpi_ws281x")

    // LED strip configuration:
    val LED_COUNT = 8 // Number of LED pixels.
    val LED_PIN = 18 // GPIO pin connected to the pixels (must support PWM!).
    val LED_FREQ_HZ = 800000 // LED signal frequency in hertz (usually 800khz)
    val LED_DMA = 5 // DMA channel to use for generating signal (try 5)
    val LED_BRIGHTNESS = 10.toShort // Set to 0 for darkest and 255 for brightest
    val LED_INVERT = false // True to invert the signal (when using NPN transistor level shift)
    val LED_CHANNEL = 0
    val LED_STRIP: Int = wsC.SK6812_STRIP_RGBW

    val strip = Adafruit_NeoPixel(LED_COUNT, LED_PIN, LED_FREQ_HZ, LED_DMA, LED_INVERT, LED_BRIGHTNESS, LED_CHANNEL, LED_STRIP)
    strip.begin()

    val mp = 60

    strip.setPixelColor(7, color(mp, 0, 0))
    strip.setPixelColor(6, color(0, mp, 0))
    strip.setPixelColor(5, color(0, 0, mp))
    strip.setPixelColor(4, color(mp, mp, 0))
    strip.setPixelColor(3, color(mp, 0, mp))
    strip.setPixelColor(2, color(0, mp, mp))
    strip.setPixelColorRGB(1, mp, mp, mp, 255)
    strip.setPixelColorRGB(0, mp, mp, mp, 100)
    strip.show()

    //println(s"Led 5: ${strip.getPixelColor(5)}")

    val updateInterval = if (args.length == 2) args(1).toInt else 1

    for { i <- 1 to 100 } {
      for {b <- mp to 1 by -1} {
        strip.setBrightness(b.toShort)
        strip.show()
        Thread.sleep(updateInterval)
      }
      for {b <- 1 to mp} {
        strip.setBrightness(b.toShort)
        strip.show()
        Thread.sleep(updateInterval)
      }
    }

    strip.setBrightness(0.toShort)
    strip.show()
    strip.cleanup()
  }
}
