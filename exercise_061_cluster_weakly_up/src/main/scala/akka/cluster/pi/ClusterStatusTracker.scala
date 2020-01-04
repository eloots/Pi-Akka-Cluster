package akka.cluster.pi

/**
 * Copyright © 2016-2019 Lightbend, Inc.
 *
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
 * NO COMMERCIAL SUPPORT OR ANY OTHER FORM OF SUPPORT IS OFFERED ON
 * THIS SOFTWARE BY LIGHTBEND, Inc.
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
  final case object IsNoLeader extends NodeState

  sealed trait ClusterEvent
  // internal adapted cluster events only
  private final case class ReachabilityChange(reachabilityEvent: ReachabilityEvent) extends ClusterEvent
  private final case class MemberChange(event: MemberEvent) extends ClusterEvent
  private final case class LeaderChange(event: LeaderChanged) extends ClusterEvent
  final case class SubscribeVisualiser(subscriber: ActorRef[ClusterStatusTracker.NodeState]) extends ClusterEvent
  final case class UnsubscribeVisualiser(subscriber: ActorRef[ClusterStatusTracker.NodeState]) extends ClusterEvent

  def apply(settings: Settings): Behavior[ClusterEvent] =
    Behaviors.setup { context =>
      new ClusterStatusTracker(context, settings)
        .running(
          Map.empty[String, NodeState],
          isLeader = false,
          subscribers = Set.empty[ActorRef[ClusterStatusTracker.NodeState]]
        )
    }

}

class ClusterStatusTracker private (context: ActorContext[ClusterStatusTracker.ClusterEvent],
                           settings: Settings
                           ) {
  import ClusterStatusTracker._

  private val cluster = Cluster(context.system)

  private val thisHost = settings.config.getString("akka.remote.artery.canonical.hostname")

  private val memberEventAdapter: ActorRef[MemberEvent] = context.messageAdapter(MemberChange)
  Cluster(context.system).subscriptions ! Subscribe(memberEventAdapter, classOf[MemberEvent])

  private val reachabilityAdapter: ActorRef[ReachabilityEvent] = context.messageAdapter(ReachabilityChange)
  Cluster(context.system).subscriptions ! Subscribe(reachabilityAdapter, classOf[ReachabilityEvent])

  private val roleLeadChangedAdapter: ActorRef[LeaderChanged] = context.messageAdapter(LeaderChange)
  Cluster(context.system).subscriptions ! Subscribe(roleLeadChangedAdapter, classOf[LeaderChanged])

  def running(nodesState: Map[String, NodeState],
              isLeader: Boolean,
              subscribers: Set[ActorRef[ClusterStatusTracker.NodeState]]): Behavior[ClusterStatusTracker.ClusterEvent] = Behaviors.receiveMessage {

    case UnsubscribeVisualiser(subscriber) =>
      running(nodesState, isLeader, subscribers - subscriber)

    case SubscribeVisualiser(newSubscriber) =>
      val updatedSubscribers = subscribers + newSubscriber
      for {
        (_, nodeState) <- nodesState
        subscriber <- updatedSubscribers
      } subscriber ! nodeState
      processLeaderChange(nodesState, isLeader, updatedSubscribers)

    case ReachabilityChange(reachabilityChange) =>
      reachabilityChange match {
        case UnreachableMember(member) =>
          processMemberChange(member, NodeUnreachable(mapHostToNodeId(member)), nodesState, isLeader, subscribers)
        case ReachableMember(member) if member.status == Up =>
          processMemberChange(member, NodeUp(mapHostToNodeId(member)), nodesState, isLeader, subscribers)
        case ReachableMember(member) if member.status == WeaklyUp =>
          processMemberChange(member, NodeWeaklyUp(mapHostToNodeId(member)), nodesState, isLeader, subscribers)
      }

    case MemberChange(memberChange) =>
      memberChange match {
        case MemberJoined(member) =>
          processMemberChange(member, NodeJoining(mapHostToNodeId(member)), nodesState, isLeader, subscribers)
        case MemberUp(member) =>
          processMemberChange(member, NodeUp(mapHostToNodeId(member)), nodesState, isLeader, subscribers)
        case MemberLeft(member) =>
          processMemberChange(member, NodeLeaving(mapHostToNodeId(member)), nodesState, isLeader, subscribers)
        case MemberExited(member) =>
          processMemberChange(member, NodeExiting(mapHostToNodeId(member)), nodesState, isLeader, subscribers)
        case MemberRemoved(member, _) =>
          processMemberChange(member, NodeRemoved(mapHostToNodeId(member)), nodesState, isLeader, subscribers)
        case MemberDowned(member) =>
          processMemberChange(member, NodeDown(mapHostToNodeId(member)), nodesState, isLeader, subscribers)
        case MemberWeaklyUp(member) =>
          processMemberChange(member, NodeWeaklyUp(mapHostToNodeId(member)), nodesState, isLeader, subscribers)
      }

    case LeaderChange(LeaderChanged(None)) =>
      processLeaderChange(nodesState, isLeader = false, subscribers)
    case LeaderChange(LeaderChanged(Some(leader))) if leader.host.getOrElse("") == thisHost =>
      processLeaderChange(nodesState, isLeader = true, subscribers)
    case LeaderChange(LeaderChanged(Some(leader))) =>
      processLeaderChange(nodesState, isLeader = false, subscribers)

  }

  def processLeaderChange(nodesState: Map[String, NodeState],
                          isLeader: Boolean,
                          subscribers: Set[ActorRef[ClusterStatusTracker.NodeState]]): Behavior[ClusterStatusTracker.ClusterEvent] = {
    for ( subscriber <- subscribers) subscriber ! (if (isLeader) IsLeader else IsNoLeader)
    running(nodesState, isLeader, subscribers)
  }

  def processMemberChange(member: Member,
                          newState: NodeState,
                          nodesState: Map[String, NodeState],
                          isLeader: Boolean,
                          subscribers: Set[ActorRef[ClusterStatusTracker.NodeState]]): Behavior[ClusterStatusTracker.ClusterEvent] = {
    val nodeName = member.address.host.get
    for ( subscriber <- subscribers) subscriber ! newState
    running(nodesState + (nodeName -> newState), isLeader, subscribers)
  }

  def mapHostToNodeId(member: Member): Int = settings.HostToLedMapping(member.address.host.get)
}
