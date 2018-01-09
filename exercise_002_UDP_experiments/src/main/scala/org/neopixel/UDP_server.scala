package org.neopixel

import akka.actor.ActorSystem


object UDP_server {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("udp-server-system")

    val server = system.actorOf(UDPListener.props, "server")
  }

}
