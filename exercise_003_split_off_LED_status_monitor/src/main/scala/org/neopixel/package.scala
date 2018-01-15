package org

import _root_.neopixel.{rpi_ws281x => ws}
import _root_.neopixel.ws2811_t
import _root_.neopixel.{rpi_ws281xConstants => wsC}
import _root_.neopixel.{rpi_ws281xJNI => wsJ}
import _root_.neopixel.ws2811_channel_t
import _root_.neopixel.{ws2811_return_t => wsRet}
import akka.event.LoggingAdapter

package object neopixel {

  def color(red: Int, green: Int, blue: Int, white: Int = 0): Long =
    (white << 24) | (red << 16)| (green << 8) | blue

  case class LED_Data (channel: ws2811_channel_t, size: Int) {
    val ledData: Array[Long] = Array.fill[Long](size)(0L)

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

    def show(log: LoggingAdapter): Unit = {
      val resp = ws.ws2811_render(leds)
      if (resp != wsRet.WS2811_SUCCESS) {
        val message = ws.ws2811_get_return_t_str(resp)
        log.debug(s"ws2811_render failed with code $resp ($message)")
      }
      log.debug(s"Neopixel.show called")
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
