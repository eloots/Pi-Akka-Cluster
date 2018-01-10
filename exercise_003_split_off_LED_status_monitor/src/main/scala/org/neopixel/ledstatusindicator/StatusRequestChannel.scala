package org.neopixel.ledstatusindicator

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.{IO, Udp}
import akka.util.ByteString


object StatusRequestChannel {

  def props(remote: InetSocketAddress): Props = Props(new StatusRequestChannel(remote))
}

class StatusRequestChannel(remote: InetSocketAddress) extends Actor with ActorLogging {

  import context.system
  IO(Udp) ! Udp.SimpleSender

  override def receive: Receive = {
    case Udp.SimpleSenderReady =>
      context.become(ready(sender()))
  }

  def ready(send: ActorRef): Receive = {
    case msg: String =>
      send ! Udp.Send(ByteString(msg), remote)
  }

}