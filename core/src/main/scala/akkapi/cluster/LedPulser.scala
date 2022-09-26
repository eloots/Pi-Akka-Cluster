package akkapi.cluster

import akka.actor.typed.scaladsl.{ActorContext, Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}
import org.neopixel.Neopixel

import scala.concurrent.duration.FiniteDuration

object LedPulser {
  sealed trait Command
  final case class PulseLed(ledNumber: Int,
                            color: Long,
                            flashDuration: FiniteDuration,
                            overRunColor: Option[Long]) extends Command
  private final case class StopPulse(ledNumber: Int) extends Command

  def apply(settings: Settings, ledStripDriver: ActorRef[LedStripDriver.Command]): Behavior[Command] = Behaviors.setup { context =>
    Behaviors.withTimers { timers =>
      new LedPulser(settings, context, timers, ledStripDriver).run(Neopixel.Black)
    }
  }
}

class LedPulser(settings: Settings,
                context: ActorContext[LedPulser.Command],
                timers: TimerScheduler[LedPulser.Command],
                ledStripDriver: ActorRef[LedStripDriver.Command]) {
  import LedPulser._

  def run(currentColor: Long): Behavior[Command] = Behaviors.receiveMessagePartial {
    case PulseLed(ledNumber, color, flashDuration, overRunColor) if color != currentColor =>
      timers.startTimerWithFixedDelay(StopPulse(ledNumber), flashDuration)
      ledStripDriver ! LedStripDriver.SetLedState(ledNumber, color, None)
      run(color)
    case PulseLed(ledNumber, color, flashDuration, overRunColor) =>
      // If the new color is the same as the current color, it implies that
      // the timer is still running. Obviously, no need to update the color
      // on the LED. Running `startTimerWithFixedDelay` will cancel the current
      // timer and start a "fresh" one
      timers.startTimerWithFixedDelay(StopPulse(ledNumber), flashDuration)
      run(color)
    case StopPulse(ledNumber) =>
      ledStripDriver ! LedStripDriver.SetLedState(ledNumber, Neopixel.Black, None)
      run(Neopixel.Black)
  }
}
