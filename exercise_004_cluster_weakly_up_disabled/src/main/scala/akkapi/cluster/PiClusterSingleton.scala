package akkapi.cluster

/**
  * Copyright © Eric Loots 2022 - eric.loots@gmail.com
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, PostStop}

object PiClusterSingleton {

  sealed trait Command
  final case object Ping extends Command

  def apply(settings: Settings, clusterStatusTracker: ActorRef[ClusterStatusTracker.ClusterEvent]): Behavior[Command] = {
    Behaviors.setup { context =>
      new PiClusterSingleton(context, settings, clusterStatusTracker).run()
    }
  }
}

class PiClusterSingleton private (context: ActorContext[PiClusterSingleton.Command],
                         settings: Settings,
                         clusterStatusTracker: ActorRef[ClusterStatusTracker.ClusterEvent]) {

  import PiClusterSingleton._

  // Cluster singleton has been started on this node
  clusterStatusTracker ! ClusterStatusTracker.PiClusterSingletonOnNode

  def run(): Behavior[Command] = Behaviors.receiveMessage[Command] {
    case Ping =>
      context.log.info(s"PiClusterSingleton was pinged")
      Behaviors.same
  }.receiveSignal {
    case (_, signal) if signal == PostStop =>
      clusterStatusTracker ! ClusterStatusTracker.PiClusterSingletonNotOnNode
      Behaviors.same
  }

}
