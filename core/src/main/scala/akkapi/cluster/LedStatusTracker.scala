package akkapi.cluster

import akka.actor.typed.{ActorRef, ActorSystem, Extension, ExtensionId}
import akkapi.cluster.LedStripDriver.Blinker

import scala.concurrent.duration.FiniteDuration

final class LedController(ledStripDriver: ActorRef[LedStripDriver.Command]) {

  def setLedState(ledId: Int, color: Long, blinkInterval: Option[Blinker]): Unit = {
    ledStripDriver ! LedStripDriver.SetLedState(ledId, color, blinkInterval)
  }

  def flashLed(id: String, ledNumber: Int, color: Long, flashDuration: FiniteDuration, overRunColor: Option[Long]): Unit = {
    ledStripDriver ! LedStripDriver.FlashLed(id, ledNumber, color, flashDuration, overRunColor)
  }
}

final class LedStatusTracker(system: ActorSystem[_]) extends Extension {

//  private var started = false
//
//  def start(): Unit =
//    if (!started) {

  val controller = {
  val osArch = System.getProperty("os.arch")
      println(s"os.arch = $osArch")

      if (System.getProperty("os.arch") == "aarch64") {
        println(s"Running on a 64-bit architecture")
        System.loadLibrary("rpi_ws281x_64")
      } else {
        println(s"Running on a 32-bit architecture")
        System.loadLibrary("rpi_ws281x")
      }

    implicit val config = system.settings.config
    val settings = new Settings()

    val ledStripDriver = system.systemActorOf(LedStripDriver(settings), "led-strip-driver")
    val ledStripController = system.systemActorOf(LedStripVisualiser(settings, ledStripDriver), "led-strip-controller")
    val clusterStatusTracker = system.systemActorOf(ClusterStatusTracker(settings), "cluster-status-tracker")
    clusterStatusTracker ! ClusterStatusTracker.SubscribeVisualiser(ledStripController)


//      started = true

      new LedController(ledStripDriver)
    }
}

object LedStatusTracker extends ExtensionId[LedStatusTracker] {

  def createExtension(system: ActorSystem[_]): LedStatusTracker = new LedStatusTracker(system)

  // Java API
  def get(system: ActorSystem[_]): LedStatusTracker = apply(system)
}

