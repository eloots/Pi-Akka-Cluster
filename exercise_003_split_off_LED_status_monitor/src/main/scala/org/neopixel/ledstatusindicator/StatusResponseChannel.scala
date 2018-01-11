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
//      log.debug(s"Received status from ${remote.getHostName}: ${remote.getPort} : ${buf.toList}")
      val ledUpdates =
        buf.toList
          .sliding(4, 4)
          .map(toLedColor)
          .toList
      context.parent ! LEDStatusIndicator.LedUpdate(ledUpdates)

    case Udp.Unbind =>
      socket ! Udp.Unbind

    case Udp.Unbound =>
      context.stop(self)
  }

  protected def toLedColor(colorComponents: List[Byte]): Long = {
    colorComponents.foldLeft(0L) {
      case (tally, component) => tally << 8 | component
    }
  }

}
