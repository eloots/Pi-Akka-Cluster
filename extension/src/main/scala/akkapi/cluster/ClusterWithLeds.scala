package akkapi.cluster

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.ConfigFactory

trait ClusterWithLeds {

  def main(args: Array[String]): Unit = {

    val actorSystemName = s"""pi-${ConfigFactory.load().getString("cluster-node-configuration.cluster-id")}-system"""

    val system = ActorSystem[NotUsed](Behaviors.ignore, actorSystemName)

    LedStatusTracker(system).start()

    // Start Akka HTTP Management extension
    AkkaManagement(system.toClassic).start()
  }
}
