package org.neopixel

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.{IO, Udp}

object UDPListener {

  def props: Props = Props(new UDPListener)
}

class UDPListener extends Actor with ActorLogging {

  import context.system

  val buf: Array[Byte] = Array.fill[Byte](32)(-128)

  IO(Udp) ! Udp.Bind(self, new InetSocketAddress("localhost", 2500))

  override def receive: Receive = {
    case Udp.Bound(local) =>
      log.debug(s"UDPListener bound: ${local.getHostName}:${local.getPort}")
      context.become(ready(sender()))
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data, remote) =>
      data.copyToArray(buf, 0, 32)
      log.debug(s"Received from ${remote.getHostName}: ${remote.getPort} : ${buf.toList}")

    case Udp.Unbind =>
      socket ! Udp.Unbind

    case Udp.Unbound =>
      context.stop(self)
  }

}
