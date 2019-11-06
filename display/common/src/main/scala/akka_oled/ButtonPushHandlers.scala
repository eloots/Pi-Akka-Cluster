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
package akka_oled

import akka.actor.ActorRef
import akka_oled.ButtonPushHandlers.{NEXT_SCREEN, RESET_SCREEN}
import com.pi4j.io.gpio.{GpioController, GpioFactory, GpioPinDigitalInput, PinPullResistance, RaspiPin}
import com.pi4j.io.gpio.event.{GpioPinDigitalStateChangeEvent, GpioPinListenerDigital}

object ButtonPushHandlers{
   case object NEXT_SCREEN
   case object RESET_SCREEN
   case object PREVIOUS_SCREEN
}

trait ButtonPushHandlers {
   val gpio: GpioController = GpioFactory.getInstance
   val DELAY = 300
   var counter = 0
   val lastClick = 0
   val RESET_DELAY = 1000
   def onStop(): Unit = gpio.removeAllListeners()

   private[this] def diff(last: Option[Long]): Long = System.currentTimeMillis() - last.getOrElse(0L)

   def initButtonPush(actor: ActorRef): Unit = {
      val upButton: GpioPinDigitalInput = gpio.provisionDigitalInputPin(RaspiPin.GPIO_16, PinPullResistance.PULL_UP)

      upButton.addListener(new GpioPinListenerDigital() {
         var lastPush: Option[Long] = None

         override def handleGpioPinDigitalStateChangeEvent(event: GpioPinDigitalStateChangeEvent): Unit = {
            counter += 1
            println(counter)
            if (event.getState.isLow && diff(lastPush) > DELAY) { // display pin state on console
               if(diff(lastPush) < RESET_DELAY){
                  actor ! RESET_SCREEN
               }else{
                  actor ! NEXT_SCREEN
               }
               lastPush = Some(System.currentTimeMillis())
            }
         }
      })
   }
}
