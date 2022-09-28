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

/**
 * ClusterStatusTracker is an actor that registers for Akka Cluster
 * Membership, Reachability, and Leadership Events and translates this
 * to 'Visualiser' events.
 *
 * In its current form, the processing of Akka Cluster events
 * and the visualisation of the cluster status are in separate components.
 *
 * Next step is to make the `ClusterStatusTracker` track the state of the
 * cluster and to allow more than one, normally of different type, visualisers
 * to register.
 */

import akka.actor.Address
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.ClusterEvent._
import akka.cluster.Member
import akka.cluster.MemberStatus.{Up, WeaklyUp}
import akka.cluster.typed.{Cluster, ClusterSingleton, SingletonActor, Subscribe}

object ClusterStatusTracker {

  sealed trait NodeState
  final case class NodeJoining(nodeId: Int) extends NodeState
  final case class NodeUp(nodeId: Int) extends NodeState
  final case class NodeLeaving(nodeId: Int) extends NodeState
  final case class NodeExiting(nodeId: Int) extends NodeState
  final case class NodeRemoved(nodeId: Int) extends NodeState
  final case class NodeDown(nodeId: Int) extends NodeState
  final case class NodeUnreachable(nodeId: Int) extends NodeState
  final case class NodeWeaklyUp(nodeId: Int) extends NodeState
  final case object IsLeader extends NodeState
  final case class IsNoLeader(nodeId: Option[Int]) extends NodeState
  final case object PiClusterSingletonRunning extends NodeState
  final case object PiClusterSingletonNotRunning extends NodeState

  sealed trait ClusterEvent
  // internal adapted cluster events only
  private final case class ReachabilityChange(reachabilityEvent: ReachabilityEvent) extends ClusterEvent
  private final case class MemberChange(event: MemberEvent) extends ClusterEvent
  private final case class LeaderChange(event: LeaderChanged) extends ClusterEvent
  final case object PiClusterSingletonOnNode extends ClusterEvent
  final case object PiClusterSingletonNotOnNode extends ClusterEvent
  final case class SubscribeVisualiser(subscriber: ActorRef[ClusterStatusTracker.NodeState]) extends ClusterEvent
  final case class UnsubscribeVisualiser(subscriber: ActorRef[ClusterStatusTracker.NodeState]) extends ClusterEvent

  case class Status(thisHost: String,
                    nodesState: Map[String, NodeState],
                    singletonRunning: Boolean,
                    leader: Option[Address],
                    subscribers: Set[ActorRef[ClusterStatusTracker.NodeState]]) {
    def isLeader(): Boolean = leader.exists(_.host.get == thisHost)
  }

  def apply(settings: Settings, optSingleton: Option[ActorContext[ClusterStatusTracker.ClusterEvent] => Behavior[_]]): Behavior[ClusterEvent] =
    Behaviors.setup { context =>
      val thisHost = settings.config.getString("akka.remote.artery.canonical.hostname")

      new ClusterStatusTracker(context, settings, optSingleton)
        .running(
          Status(thisHost,
            Map.empty[String, NodeState],
            singletonRunning = false,
            leader = None,
            subscribers = Set.empty[ActorRef[ClusterStatusTracker.NodeState]]
          )
        )
    }

  type ActorContextToSingletonBehavior = (ActorContext[ClusterStatusTracker.ClusterEvent]) => Behavior[_]
}

class ClusterStatusTracker private(context: ActorContext[ClusterStatusTracker.ClusterEvent],
                                   settings: Settings,
                                   optSingleton: Option[ClusterStatusTracker.ActorContextToSingletonBehavior]
                                  ) {

  import ClusterStatusTracker._

  private val memberEventAdapter: ActorRef[MemberEvent] = context.messageAdapter(MemberChange)
  Cluster(context.system).subscriptions ! Subscribe(memberEventAdapter, classOf[MemberEvent])

  private val reachabilityAdapter: ActorRef[ReachabilityEvent] = context.messageAdapter(ReachabilityChange)
  Cluster(context.system).subscriptions ! Subscribe(reachabilityAdapter, classOf[ReachabilityEvent])

  private val roleLeadChangedAdapter: ActorRef[LeaderChanged] = context.messageAdapter(LeaderChange)
  Cluster(context.system).subscriptions ! Subscribe(roleLeadChangedAdapter, classOf[LeaderChanged])

  createPiClusterSingleton()

  def running(status: Status): Behavior[ClusterStatusTracker.ClusterEvent] = Behaviors.receiveMessage {
    case PiClusterSingletonOnNode =>
      processSingletonState(status.copy(singletonRunning = true))
    case PiClusterSingletonNotOnNode =>
      processSingletonState(status.copy(singletonRunning = false))

    case UnsubscribeVisualiser(subscriber) =>
      running(status.copy(subscribers = status.subscribers - subscriber))

    case SubscribeVisualiser(newSubscriber) =>
      val updatedSubscribers = status.subscribers + newSubscriber
      for {
        (_, nodeState) <- status.nodesState
        subscriber <- updatedSubscribers
      } subscriber ! nodeState
      val newStatus = status.copy(subscribers = updatedSubscribers)
      processLeaderChange(newStatus)
      processSingletonState(newStatus)

    case ReachabilityChange(reachabilityChange) =>
      reachabilityChange match {
        case UnreachableMember(member) =>
          processMemberChange(member, NodeUnreachable(mapHostToNodeId(member)), status)
        case ReachableMember(member) if member.status == Up =>
          processMemberChange(member, NodeUp(mapHostToNodeId(member)), status)
        case ReachableMember(member) if member.status == WeaklyUp =>
          processMemberChange(member, NodeWeaklyUp(mapHostToNodeId(member)), status)
      }

    case MemberChange(memberChange) =>
      memberChange match {
        case MemberJoined(member) =>
          processMemberChange(member, NodeJoining(mapHostToNodeId(member)), status)
        case MemberUp(member) =>
          processMemberChange(member, NodeUp(mapHostToNodeId(member)), status)
        case MemberLeft(member) =>
          processMemberChange(member, NodeLeaving(mapHostToNodeId(member)), status)
        case MemberExited(member) =>
          processMemberChange(member, NodeExiting(mapHostToNodeId(member)), status)
        case MemberRemoved(member, _) =>
          processMemberChange(member, NodeRemoved(mapHostToNodeId(member)), status)
        case MemberDowned(member) =>
          processMemberChange(member, NodeDown(mapHostToNodeId(member)), status)
        case MemberWeaklyUp(member) =>
          processMemberChange(member, NodeWeaklyUp(mapHostToNodeId(member)), status)
      }

    case LeaderChange(LeaderChanged(leaderOption)) =>
      processLeaderChange(status.copy(leader = leaderOption))
  }

  def processLeaderChange(status: Status): Behavior[ClusterStatusTracker.ClusterEvent] = {
    val nodeId = status.leader.map { l => settings.HostToLedMapping(l.host.get) }
    for (subscriber <- status.subscribers) subscriber ! (if (status.isLeader) IsLeader else IsNoLeader(nodeId))
    running(status)
  }

  def processMemberChange(member: Member,
                          newState: NodeState,
                          status: Status): Behavior[ClusterStatusTracker.ClusterEvent] = {
    val nodeName = member.address.host.get
    for (subscriber <- status.subscribers) subscriber ! newState
    running(status.copy(nodesState = status.nodesState + (nodeName -> newState)))
  }

  def processSingletonState(status: Status): Behavior[ClusterStatusTracker.ClusterEvent] = {
    for (subscriber <- status.subscribers)
      if (status.singletonRunning) subscriber ! PiClusterSingletonRunning else subscriber ! PiClusterSingletonNotRunning
    running(status)
  }

  def mapHostToNodeId(member: Member): Int = {
    settings.HostToLedMapping(member.address.host.get)
  }

  def createPiClusterSingleton(): Unit = {
    optSingleton.foreach(singleton =>
      ClusterSingleton(context.system).init(SingletonActor(singleton(context), "pi-cluster-singleton"))
    )

  }
}
