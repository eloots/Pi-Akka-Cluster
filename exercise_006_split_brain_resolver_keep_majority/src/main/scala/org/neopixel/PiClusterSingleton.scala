package org.neopixel

import akka.actor.{Actor, ActorLogging, Props}

object PiClusterSingleton {

  def props(strip: Adafruit_NeoPixel.type): Props = Props(new PiClusterSingleton(strip))
}

class PiClusterSingleton(strip: Adafruit_NeoPixel.type) extends Actor with ActorLogging {

  override def receive: Receive = akka.actor.Actor.emptyBehavior

  override def preStart(): Unit = {
    log.info(s"ClusterSingleton started")
    setPixelColorAndShow(strip, 1, LightBlue)
    super.preStart()
  }

  override def postStop(): Unit = {
    log.info(s"ClusterSingleton stopped")
    setPixelColorAndShow(strip, 1, Black)
    super.postStop()
  }

  private def setPixelColorAndShow(strip: Adafruit_NeoPixel.type ,
                                   ledNumber: Int,
                                   ledColor: Long): Unit = {
    strip.setPixelColor(ledNumber, ledColor)
    strip.show()
  }

}
