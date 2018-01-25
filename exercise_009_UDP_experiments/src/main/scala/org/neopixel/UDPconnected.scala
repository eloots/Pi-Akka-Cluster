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
