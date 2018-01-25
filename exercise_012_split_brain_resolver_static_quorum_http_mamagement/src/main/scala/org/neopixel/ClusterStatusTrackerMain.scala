package org.neopixel

import akka.actor.ActorSystem
import akka.management.AkkaManagement

object ClusterStatusTrackerMain {
  def main(args: Array[String]): Unit = {
    System.loadLibrary("rpi_ws281x")

    val system = ActorSystem("pi-cluster-system")
    val clusterStatusTracker = system.actorOf(ClusterStatusTracker.props(), "cluster-status-tracker")
    AkkaManagement(system).start
  }
}
