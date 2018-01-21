package org.neopixel

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import neopixel.{rpi_ws281xConstants => wsC}

object ClusterStatusTrackerMain {
  def main(args: Array[String]): Unit = {
    System.loadLibrary("rpi_ws281x")

    val clusterNodeAddress = new InetSocketAddress("localhost", 2500)
    val ledStatusIndicatorAddress = new InetSocketAddress("localhost", 2501)

    val system = ActorSystem("pi-cluster-system")

    val clusterStatusTracker =
      system.actorOf(ClusterStatusTracker.props(clusterNodeAddress, ledStatusIndicatorAddress))
  }
}
