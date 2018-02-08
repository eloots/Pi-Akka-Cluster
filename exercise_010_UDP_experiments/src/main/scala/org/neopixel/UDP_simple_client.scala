package org.neopixel

import java.net._

import akka.actor.ActorSystem

import scala.concurrent.Future

object UDP_simple_client {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("udp-simple-client-system")
    val serverAddress = new InetSocketAddress("localhost", 2500)
    val log = system.log
    import scala.concurrent.ExecutionContext.Implicits.global

    val client = system.actorOf(UDPSimpleSender.props(serverAddress), "simple-client")

    Thread.sleep(500)

    while (true) {
      client ! "A string ...."
      Thread.sleep(500)
    }

    system.terminate().flatMap(t => Future.unit).onComplete { _ => println(s"UDP client terminated")}

  }
}
