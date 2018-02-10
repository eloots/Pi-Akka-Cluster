/**
  * Copyright © 2018 Lightbend, Inc
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

package org.neopixel.ledstatusindicator

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.{IO, Udp}

object StatusResponseChannel {

  def props(ledStatusIndicatorAddress: InetSocketAddress): Props =
    Props(new StatusResponseChannel(ledStatusIndicatorAddress))
}

class StatusResponseChannel(ledStatusIndicatorAddress: InetSocketAddress) extends Actor with ActorLogging {

  import context.system

  val buf: Array[Byte] = Array.fill[Byte](32)(0.toByte)

  IO(Udp) ! Udp.Bind(self, ledStatusIndicatorAddress)

  override def receive: Receive = {
    case Udp.Bound(local) =>
      log.debug(s"UDPListener bound: ${local.getHostName}:${local.getPort}")
      context.become(ready(sender()))
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data, remote) =>
      data.copyToArray(buf, 0, 32)
      // log.debug(s"Received status from ${remote.getHostName}: ${remote.getPort} : ${buf.toList}")
      val ledUpdates =
        buf
          .sliding(4, 4)
          .map(toLedColor)
          .toList
      context.parent ! LEDStatusIndicator.LedUpdate(ledUpdates)

    case Udp.Unbind =>
      socket ! Udp.Unbind

    case Udp.Unbound =>
      context.stop(self)
  }

  protected def toLedColor(colorComponents: Array[Byte]): Long = {
    import java.nio.ByteBuffer

    val bb = ByteBuffer.wrap(colorComponents.reverse)
    bb.order(java.nio.ByteOrder.nativeOrder())
    val l = bb.getInt
    bb.clear()
    l
  }

}
