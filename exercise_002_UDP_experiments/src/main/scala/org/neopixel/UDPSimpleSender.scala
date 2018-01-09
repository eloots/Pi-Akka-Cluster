package org.neopixel

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.{IO, Udp}
import akka.util.ByteString

object UDPSimpleSender {

  def props(remote: InetSocketAddress): Props = Props(new UDPSimpleSender(remote))
}

class UDPSimpleSender(remote: InetSocketAddress) extends Actor with ActorLogging {

  import context.system
  IO(Udp) ! Udp.SimpleSender

  def receive = {
    case Udp.SimpleSenderReady =>
      context.become(ready(sender()))
  }

  def ready(send: ActorRef): Receive = {
    case msg: String =>
      send ! Udp.Send(ByteString(msg), remote)
  }

}
