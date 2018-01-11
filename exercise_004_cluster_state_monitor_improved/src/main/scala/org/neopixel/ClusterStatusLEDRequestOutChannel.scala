package org.neopixel

import java.net.InetSocketAddress
import java.nio.ByteBuffer

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.{IO, Udp}
import akka.util.ByteString

object ClusterStatusLEDRequestOutChannel {

  def props(remote: InetSocketAddress): Props = Props(new ClusterStatusLEDRequestOutChannel(remote))
}

class ClusterStatusLEDRequestOutChannel(remote: InetSocketAddress) extends Actor with ActorLogging {

  import context.system
  IO(Udp) ! Udp.SimpleSender

  override def receive: Receive = {
    case Udp.SimpleSenderReady =>
      context.become(ready(sender()))
  }

  def ready(send: ActorRef): Receive = {
    case ClusterStatusTracker.NodeUpdate(updates) =>
      val response = ByteString.fromArray(updates.flatMap(longToBytes).toArray)
//      log.debug(s"ClusterStatusLEDRequestOutChannel: $updates ~~> response: $response")
      send ! Udp.Send(response, remote)

    case msg: String =>
      send ! Udp.Send(ByteString(msg), remote)
  }

  def longToBytes(l: Long): List[Byte] = {

    val mask: Long = 0x00000000000000FF
    val b0 = l & mask
    val b1 = (l >> 8) & mask
    val b2 = (l >> 16) & mask
    val b3 = (l >> 24) & mask
    val r = List(b0.toByte, b1.toByte, b2.toByte, b3.toByte).reverse
//    log.debug(s"~~> In: $l out: $r")
    r
  }

}