package akka.cluster.pi

/**
 * Copyright © 2016-2020 Lightbend, Inc.
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
 * ClusterStatusAndConvergenceTracker is an actor that registers for Akka Cluster
 * Membership, Reachability, and Leadership Events and translates this
 * to 'Visualiser' events.
 *
 * In its current form, the processing of Akka Cluster events
 * and the visualisation of the cluster status are in separate components.
 *
 */

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.ClusterEvent._
import akka.cluster.Member
import akka.cluster.MemberStatus.{Up, WeaklyUp}
import akka.cluster.typed.{Cluster, ClusterSingleton, SingletonActor, Subscribe}
import akkapi.cluster.Settings

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
  final case object PiClusterSingletonRunning extends NodeState
  final case object PiClusterSingletonNotRunning extends NodeState
  final case object ClusterConverged extends NodeState
  final case object ClusterNotConverged extends NodeState

  sealed trait ClusterEvent
  // internal adapted cluster events only
  private final case class ReachabilityChange(reachabilityEvent: ReachabilityEvent) extends ClusterEvent
  private final case class MemberChange(event: MemberEvent) extends ClusterEvent
  private final case class LeaderChange(event: LeaderChanged) extends ClusterEvent
  private final case class CurrentInternalStat(event: CurrentInternalStats) extends ClusterEvent
  final case object PiClusterSingletonOnNode extends  ClusterEvent
  final case object PiClusterSingletonNotOnNode extends  ClusterEvent
  final case class SubscribeVisualiser(subscriber: ActorRef[ClusterStatusTracker.NodeState]) extends ClusterEvent
  final case class UnsubscribeVisualiser(subscriber: ActorRef[ClusterStatusTracker.NodeState]) extends ClusterEvent

  def apply(settings: Settings, optSingleton: Option[ActorContext[ClusterStatusTracker.ClusterEvent] => Behavior[_]]): Behavior[ClusterEvent] =
    Behaviors.setup { context =>
      new ClusterStatusTracker(context, settings, optSingleton)
        .running(
          Map.empty[String, NodeState],
          isLeader = false,
          singletonRunning = false,
          subscribers = Set.empty[ActorRef[NodeState]],
          convergenceReached = false
        )
    }

  type ActorContextToSingletonBehavior = (ActorContext[ClusterStatusTracker.ClusterEvent]) => Behavior[_]
}

class ClusterStatusTracker private(context: ActorContext[ClusterStatusTracker.ClusterEvent],
                                   settings: Settings,
                                   optSingleton: Option[ClusterStatusTracker.ActorContextToSingletonBehavior]
                                   ) {
  import ClusterStatusTracker._

  private val thisHost = settings.config.getString("akka.remote.artery.canonical.hostname")

  private val memberEventAdapter: ActorRef[MemberEvent] = context.messageAdapter(MemberChange)
  Cluster(context.system).subscriptions ! Subscribe(memberEventAdapter, classOf[MemberEvent])

  private val reachabilityAdapter: ActorRef[ReachabilityEvent] = context.messageAdapter(ReachabilityChange)
  Cluster(context.system).subscriptions ! Subscribe(reachabilityAdapter, classOf[ReachabilityEvent])

  private val roleLeadChangedAdapter: ActorRef[LeaderChanged] = context.messageAdapter(LeaderChange)
  Cluster(context.system).subscriptions ! Subscribe(roleLeadChangedAdapter, classOf[LeaderChanged])

  private val currentInternalStatsAdapter: ActorRef[CurrentInternalStats] = context.messageAdapter(CurrentInternalStat)
  Cluster(context.system).subscriptions ! Subscribe(currentInternalStatsAdapter, classOf[CurrentInternalStats])

  createPiClusterSingleton()

  def running(nodesState: Map[String, NodeState],
              isLeader: Boolean,
              singletonRunning: Boolean,
              subscribers: Set[ActorRef[ClusterStatusTracker.NodeState]],
              convergenceReached: Boolean): Behavior[ClusterStatusTracker.ClusterEvent] = Behaviors.receiveMessage {
    case PiClusterSingletonOnNode =>
      processSingletonState(nodesState, isLeader, singletonRunning = true, subscribers, convergenceReached)
    case PiClusterSingletonNotOnNode =>
      processSingletonState(nodesState, isLeader, singletonRunning = false, subscribers, convergenceReached)

    case UnsubscribeVisualiser(subscriber) =>
      running(nodesState, isLeader, singletonRunning, subscribers - subscriber, convergenceReached)

    case SubscribeVisualiser(newSubscriber) =>
      val updatedSubscribers = subscribers + newSubscriber
      for {
        (_, nodeState) <- nodesState
        subscriber <- updatedSubscribers
      } subscriber ! nodeState
      processLeaderChange(nodesState, isLeader, singletonRunning, updatedSubscribers, convergenceReached)
      processSingletonState(nodesState, isLeader, singletonRunning, updatedSubscribers, convergenceReached)

    case ReachabilityChange(reachabilityChange) =>
      reachabilityChange match {
        case UnreachableMember(member) =>
          processMemberChange(member, NodeUnreachable(mapHostToNodeId(member)), nodesState, isLeader, singletonRunning, subscribers, convergenceReached)
        case ReachableMember(member) if member.status == Up =>
          processMemberChange(member, NodeUp(mapHostToNodeId(member)), nodesState, isLeader, singletonRunning, subscribers, convergenceReached)
        case ReachableMember(member) if member.status == WeaklyUp =>
          processMemberChange(member, NodeWeaklyUp(mapHostToNodeId(member)), nodesState, isLeader, singletonRunning, subscribers, convergenceReached)
      }

    case MemberChange(memberChange) =>
      memberChange match {
        case MemberJoined(member) =>
          processMemberChange(member, NodeJoining(mapHostToNodeId(member)), nodesState, isLeader, singletonRunning, subscribers, convergenceReached)
        case MemberUp(member) =>
          processMemberChange(member, NodeUp(mapHostToNodeId(member)), nodesState, isLeader, singletonRunning, subscribers, convergenceReached)
        case MemberLeft(member) =>
          processMemberChange(member, NodeLeaving(mapHostToNodeId(member)), nodesState, isLeader, singletonRunning, subscribers, convergenceReached)
        case MemberExited(member) =>
          processMemberChange(member, NodeExiting(mapHostToNodeId(member)), nodesState, isLeader, singletonRunning, subscribers, convergenceReached)
        case MemberRemoved(member, _) =>
          processMemberChange(member, NodeRemoved(mapHostToNodeId(member)), nodesState, isLeader, singletonRunning, subscribers, convergenceReached)
        case MemberDowned(member) =>
          processMemberChange(member, NodeDown(mapHostToNodeId(member)), nodesState, isLeader, singletonRunning, subscribers, convergenceReached)
        case MemberWeaklyUp(member) =>
          processMemberChange(member, NodeWeaklyUp(mapHostToNodeId(member)), nodesState, isLeader, singletonRunning, subscribers, convergenceReached)
      }
    case CurrentInternalStat(event) =>
      event match {
        case CurrentInternalStats(_, vclockStats) =>
          val hasNowConverged = vclockStats.seenLatest == Cluster(context.system).state.members.size
          (convergenceReached, hasNowConverged) match {
            case (false, true) =>
              subscribers.foreach(subscriber => subscriber ! ClusterConverged)
            //timers.cancel("heartbeat-timer")
            //startConvergenceTimer()
            case (true, false) =>
              subscribers.foreach(subscriber => subscriber ! ClusterNotConverged)
            case _ =>
          }
          running(nodesState, isLeader, singletonRunning, subscribers, hasNowConverged)
      }

    case LeaderChange(LeaderChanged(None)) =>
      processLeaderChange(nodesState, isLeader = false, singletonRunning, subscribers, convergenceReached)
    case LeaderChange(LeaderChanged(Some(leader))) if leader.host.getOrElse("") == thisHost =>
      processLeaderChange(nodesState, isLeader = true, singletonRunning, subscribers, convergenceReached)
    case LeaderChange(LeaderChanged(Some(leader))) =>
      processLeaderChange(nodesState, isLeader = false, singletonRunning, subscribers, convergenceReached)

  }

  def processLeaderChange(nodesState: Map[String, NodeState],
                          isLeader: Boolean,
                          singletonRunning: Boolean,
                          subscribers: Set[ActorRef[ClusterStatusTracker.NodeState]],
                          convergenceReached: Boolean): Behavior[ClusterStatusTracker.ClusterEvent] = {
    for ( subscriber <- subscribers) subscriber ! (if (isLeader) IsLeader else IsNoLeader)
    running(nodesState, isLeader, singletonRunning, subscribers, convergenceReached)
  }

  def processMemberChange(member: Member,
                          newState: NodeState,
                          nodesState: Map[String, NodeState],
                          isLeader: Boolean,
                          singletonRunning: Boolean,
                          subscribers: Set[ActorRef[ClusterStatusTracker.NodeState]],
                          convergenceReached: Boolean): Behavior[ClusterStatusTracker.ClusterEvent] = {
    val nodeName = member.address.host.get
    for ( subscriber <- subscribers) subscriber ! newState
    running(nodesState + (nodeName -> newState), isLeader, singletonRunning, subscribers, convergenceReached)
  }

  def processSingletonState(nodesState: Map[String, NodeState],
                            isLeader: Boolean,
                            singletonRunning: Boolean,
                            subscribers: Set[ActorRef[ClusterStatusTracker.NodeState]],
                            convergenceReached: Boolean): Behavior[ClusterStatusTracker.ClusterEvent] = {
    for (subscriber <- subscribers) if (singletonRunning) subscriber ! PiClusterSingletonRunning else subscriber ! PiClusterSingletonNotRunning
    running(nodesState, isLeader, singletonRunning = singletonRunning, subscribers, convergenceReached)
  }

  def mapHostToNodeId(member: Member): Int = settings.HostToLedMapping(member.address.host.get)

  def createPiClusterSingleton(): Unit = {
    optSingleton.foreach(singleton =>
      ClusterSingleton(context.system).init(SingletonActor(singleton(context), "pi-cluster-singleton"))
    )

  }
}
