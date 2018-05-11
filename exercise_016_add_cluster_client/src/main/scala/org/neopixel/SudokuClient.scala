package org.neopixel

import akka.actor.{ActorPath, ActorSystem, AddressFromURIString, RootActorPath}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.japi.Util.immutableSeq
import com.typesafe.config.ConfigFactory

object SudokuClient {

  def main(args: Array[String]): Unit = {

    val conf = ConfigFactory.load("sudokuclient")

    val system = ActorSystem("sudoku-client", conf)

    val initialContacts = immutableSeq(conf.getStringList("contact-points")).map {
      case AddressFromURIString(addr) => RootActorPath(addr) / "system" / "receptionist"
    }.toSet

    val clusterClient = system.actorOf(
      ClusterClient.props(
        ClusterClientSettings(system)
          .withInitialContacts(initialContacts)),
      "clusterClient")

    system.actorOf(SudokuClientGenerator.props(clusterClient))

  }

}
