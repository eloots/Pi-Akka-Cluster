package org.neopixel.ledstatusindicator

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props, Terminated}
import org.neopixel.Adafruit_NeoPixel

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import java.net.InetSocketAddress

object LEDStatusIndicator {

  case object PollNode
  case class LedUpdate(ledUpdates: List[Long])

  def props(pollingInterval: FiniteDuration,
            clusterNodeAddress: InetSocketAddress,
            ledStatusIndicatorAddress: InetSocketAddress): Props =
    Props(new LEDStatusIndicator(pollingInterval, clusterNodeAddress, ledStatusIndicatorAddress))
}

class LEDStatusIndicator(pollingInterval: FiniteDuration,
                         clusterNodeAddress: InetSocketAddress,
                         ledStatusIndicatorAddress: InetSocketAddress) extends Actor with ActorLogging {
  import NeoPixelSettings._
  import LEDStatusIndicator._

  var timeOutScheduler: Option[Cancellable] = None
  import context.dispatcher

  private val thisHost = context.system.settings.config.getString("akka.remote.netty.tcp.hostname")
  log.debug(s"Starting LED Status indicator on $thisHost")

  var pendingPolls = 0

  override def receive: Receive = uninitialised()

  def uninitialised(): Receive = akka.actor.Actor.emptyBehavior

  def showInitialising(strip: Adafruit_NeoPixel.type, showerStart: ActorRef, requestChannel: ActorRef): Receive = {
    case Terminated(`showerStart`) =>
      timeOutScheduler = Some(context.system.scheduler.schedule(0.milliseconds, pollingInterval, self, PollNode))
      context.become(running(strip, requestChannel))
  }

  def running(pixel: Adafruit_NeoPixel.type, requestChannel: ActorRef): Receive = {
    case PollNode if pendingPolls >= 3 =>
      resetAllLeds(pixel)
      requestChannel ! "sendStatusUpdate"

    case PollNode =>
      pendingPolls += 1
      requestChannel ! "sendStatusUpdate"

    case LedUpdate(ledUpdates) =>
      pendingPolls = 0
      ledUpdates.zipWithIndex.foreach {
        case (color, ledIndex) =>
          pixel.setPixelColor(ledIndex, color)
      }
      pixel.show(log)
  }

  override def preStart(): Unit = {
    val strip = initialiseLedStrip
    val showerStart = createStartUpIndicator(strip)
    val requestChannel = createRequestChannel()
    val responseChannel = createResponseChannel()
    context.become(showInitialising(strip, showerStart, requestChannel))
    super.preStart()
  }

  override def postStop(): Unit = {
    if (timeOutScheduler.isDefined) timeOutScheduler.get.cancel()
    super.postStop()
  }

  protected def createStartUpIndicator(strip: Adafruit_NeoPixel.type): ActorRef = {
    val startUpIndicator = context.actorOf(LEDStatusIndicatorShowStart.props(strip, NeoPixelSettings.LED_COUNT),"start-up-indicator")
    context.watch(startUpIndicator)
  }

  protected def createRequestChannel(): ActorRef = {
    context.actorOf(StatusRequestChannel.props(clusterNodeAddress), "status-request-channel")
  }

  protected def createResponseChannel(): ActorRef = {
    context.actorOf(StatusResponseChannel.props(ledStatusIndicatorAddress), "status-response-channel")
  }

  protected def initialiseLedStrip: Adafruit_NeoPixel.type = {
    val strip = Adafruit_NeoPixel(LED_COUNT, LED_PIN, LED_FREQ_HZ, LED_DMA, LED_INVERT, LED_BRIGHTNESS, LED_CHANNEL, LED_STRIP)
    strip.begin()
    resetAllLeds(strip)
    strip.setBrightness(1)
    strip
  }
}
