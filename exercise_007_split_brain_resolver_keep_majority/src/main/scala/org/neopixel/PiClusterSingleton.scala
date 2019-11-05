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

package org.neopixel

import akka.actor.{Actor, ActorLogging, Props}

object PiClusterSingleton {

  def props(strip: Adafruit_NeoPixel.type,logicalToPhysicalLEDMapping: Int => Int): Props =
    Props(new PiClusterSingleton(strip, logicalToPhysicalLEDMapping))
}

class PiClusterSingleton(strip: Adafruit_NeoPixel.type, logicalToPhysicalLEDMapping: Int => Int) extends Actor with ActorLogging {

  override def receive: Receive = akka.actor.Actor.emptyBehavior

  override def preStart(): Unit = {
    log.info(s"ClusterSingleton started")
    setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(6), LightBlue)
    super.preStart()
  }

  override def postStop(): Unit = {
    log.info(s"ClusterSingleton stopped")
    setPixelColorAndShow(strip, logicalToPhysicalLEDMapping(6), Black)
    super.postStop()
  }

  private def setPixelColorAndShow(strip: Adafruit_NeoPixel.type ,
                                   ledNumber: Int,
                                   ledColor: Long): Unit = {
    strip.setPixelColor(ledNumber, ledColor)
    strip.show()
  }

}
