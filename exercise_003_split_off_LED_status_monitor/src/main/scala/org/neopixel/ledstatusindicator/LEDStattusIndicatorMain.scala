package org.neopixel.ledstatusindicator

import java.net.InetSocketAddress

import akka.actor.ActorSystem

object Main {
  def main(args: Array[String]): Unit = {
    System.loadLibrary("rpi_ws281x")

    import scala.concurrent.duration._

    val system = ActorSystem("led-status-indicator-system")
    val clusterNodeAddress = new InetSocketAddress("localhost", 2500)
    val ledStatusIndicatorAddress = new InetSocketAddress("localhost", 2501)

    val lEDStatusIndicator =
      system.actorOf(LEDStatusIndicator.props(100.milliseconds, clusterNodeAddress, ledStatusIndicatorAddress), "led-status-indicator")
  }
}
