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

package org.neopixel

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.{IO, UdpConnected}
import akka.util.ByteString

object UDPconnected {
  def props(address: InetSocketAddress): Props = Props(new UDPconnected(address))
}

class UDPconnected(remote: InetSocketAddress) extends Actor with ActorLogging {

  import context.system
  IO(UdpConnected) ! UdpConnected.Connect(self, remote)

  override def receive: Receive = {
    case UdpConnected.Connected =>
      log.debug(s"Client became connected")
      context.become(ready(sender()))
  }

  def ready(connection: ActorRef): Receive = {
    case UdpConnected.Received(data) =>
      log.debug(s"UDPConnected received back: ${data.utf8String}")
    // process data, send it on, etc.

    case msg: String =>
      connection ! UdpConnected.Send(ByteString(msg))

    case UdpConnected.Disconnect =>
      log.debug(s"UDPConnected received Disconnect")
      connection ! UdpConnected.Disconnect

    case UdpConnected.Disconnected => context.stop(self)
      log.debug(s"UDPConnected received Disconnected")
  }
}
