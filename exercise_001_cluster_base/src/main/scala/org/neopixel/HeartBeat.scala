/**
  * Copyright © 2018 Lightbend, Inc
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

package org.neopixel

import akka.actor.{ActorLogging, FSM, Props}

import scala.concurrent.duration.FiniteDuration

//object HeartBeat {
//
//  case object Pulse
//  case object TurnHeartbeatIndicatorOn
//  case object TurnHeartbeatIndicatorOff
//
//  sealed trait State
//
//  object State {
//    case object IndicatorOn extends State
//    case object IndicatorOff extends State
//  }
//
//  case class Data(count: Int)
//
//  def props(pulseInterval: FiniteDuration,
//            onCount: Int,
//            offCount: Int): Props =
//    Props(new HeartBeat(pulseInterval, onCount, offCount))
//}
//
//class HeartBeat(pulseInterval: FiniteDuration,
//                onCount: Int,
//                offCount: Int) extends FSM[HeartBeat.State, HeartBeat.Data] with ActorLogging {
//  import HeartBeat._
//
//  startWith(State.IndicatorOff, Data(count = 0))
//
//  setTimer("pulse", Pulse, pulseInterval, repeat = true)
//
//  when(State.IndicatorOff) {
//    case Event(Pulse, Data(`offCount`)) =>
//      context.parent ! TurnHeartbeatIndicatorOn
//      goto(State.IndicatorOn) using Data(0)
//
//    case Event(Pulse, Data(count)) =>
//      stay using Data(count + 1)
//  }
//
//  when(State.IndicatorOn) {
//    case Event(Pulse, Data(`onCount`)) =>
//      context.parent ! TurnHeartbeatIndicatorOff
//      goto(State.IndicatorOff) using Data(0)
//
//    case Event(Pulse, Data(count)) =>
//      stay using Data(count + 1)
//  }
//
//  initialize()
//
//}
