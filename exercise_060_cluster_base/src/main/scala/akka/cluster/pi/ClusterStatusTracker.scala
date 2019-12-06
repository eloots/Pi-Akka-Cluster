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

import akka.actor.typed.scaladsl.{ActorContext, Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.ClusterEvent._
import akka.cluster.Member
import akka.cluster.MemberStatus.{Up, WeaklyUp}
import akka.cluster.typed.{Cluster, Subscribe}

import scala.concurrent.duration._

object ClusterStatusTracker {

  sealed trait Event
  // internal adapted cluster events only
  private final case class ReachabilityChange(reachabilityEvent: ReachabilityEvent) extends Event
  private final case class MemberChange(event: MemberEvent) extends Event
  private final case class LeaderChange(event: LeaderChanged) extends Event
  private case object Tick extends Event

  def apply(settings: Settings): Behavior[Event] =
    Behaviors.setup { context =>
      Behaviors.withTimers[Event] { timers =>
        new ClusterStatusTracker(context, settings, timers).running
      }
    }

}

class ClusterStatusTracker(context: ActorContext[ClusterStatusTracker.Event],
                           settings: Settings,
                           timers: TimerScheduler[ClusterStatusTracker.Event]
                           ) {
  import ClusterStatusTracker._

  private val cluster = Cluster(context.system)

  private val thisHost = settings.config.getString("akka.remote.artery.canonical.hostname")

  private val ledStrip = context.spawn(LedStripController(settings), "led-strip-controller")

  timers.startTimerAtFixedRate(Tick, 1.seconds)

  private val memberEventAdapter: ActorRef[MemberEvent] = context.messageAdapter(MemberChange)
  Cluster(context.system).subscriptions ! Subscribe(memberEventAdapter, classOf[MemberEvent])

  private val reachabilityAdapter: ActorRef[ReachabilityEvent] = context.messageAdapter(ReachabilityChange)
  Cluster(context.system).subscriptions ! Subscribe(reachabilityAdapter, classOf[ReachabilityEvent])

  private val roleLeadChangedAdapter: ActorRef[LeaderChanged] = context.messageAdapter(LeaderChange)
  Cluster(context.system).subscriptions ! Subscribe(roleLeadChangedAdapter, classOf[LeaderChanged])

  def running: Behavior[ClusterStatusTracker.Event] = Behaviors.receiveMessage { message =>
    message match {
      case ReachabilityChange(reachabilityEvent) =>
        reachabilityEvent match {
          case UnreachableMember(member) =>
            ledStrip ! LedStripController.Unreachable(mapHostToLedId(member))
          case ReachableMember(member) if member.status == Up =>
            ledStrip ! LedStripController.Up(mapHostToLedId(member))
          case ReachableMember(member) if member.status == WeaklyUp =>
            ledStrip ! LedStripController.WeaklyUp(mapHostToLedId(member))
        }
      case MemberChange(memberChange) =>
        memberChange match {
          case MemberJoined(member) =>
            ledStrip ! LedStripController.Joining(mapHostToLedId(member))
          case MemberUp(member) =>
            ledStrip ! LedStripController.Up(mapHostToLedId(member))
          case MemberLeft(member) =>
            ledStrip ! LedStripController.Leaving(mapHostToLedId(member))
          case MemberExited(member) =>
            ledStrip ! LedStripController.Exiting(mapHostToLedId(member))
          case MemberRemoved(member, previousStatus) =>
            ledStrip ! LedStripController.Removed(mapHostToLedId(member))
          case MemberDowned(member) =>
            ledStrip ! LedStripController.Down(mapHostToLedId(member))
          case MemberWeaklyUp(member) =>
            ledStrip ! LedStripController.WeaklyUp(mapHostToLedId(member))
        }
      case LeaderChange(LeaderChanged(None)) =>
        ledStrip ! LedStripController.IsNoLeader
      case LeaderChange(LeaderChanged(Some(leader))) if leader.host.getOrElse("") == thisHost =>
        ledStrip ! LedStripController.IsLeader
      case LeaderChange(LeaderChanged(Some(leader))) =>
        ledStrip ! LedStripController.IsNoLeader
      case Tick =>

    }
    Behaviors.same
  }

  def mapHostToLedId(member: Member): Int =
    settings.HostToLedMapping(member.address.host.get)
}
