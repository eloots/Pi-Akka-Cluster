package org.neopixel

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.{IO, Udp}

object ClusterStatusLEDRequestInChannel {

  def props(clusterNodeAddress: InetSocketAddress): Props =
    Props(new ClusterStatusLEDRequestInChannel(clusterNodeAddress))
}

class ClusterStatusLEDRequestInChannel(clusterNodeAddress: InetSocketAddress) extends Actor with ActorLogging {

  import context.system

  val buf: Array[Byte] = Array.fill[Byte](32)(0)

  IO(Udp) ! Udp.Bind(self, clusterNodeAddress)

  override def receive: Receive = {
    case Udp.Bound(local) =>
      log.debug(s"UDPListener bound: ${local.getHostName}:${local.getPort}")
      context.become(ready(sender()))
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data, remote) =>
      // TODO add minimal validation of incoming request
      context.parent ! ClusterStatusTracker.SendUpdatedStatus

    case Udp.Unbind =>
      socket ! Udp.Unbind

    case Udp.Unbound =>
      context.stop(self)
  }

}