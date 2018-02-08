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
