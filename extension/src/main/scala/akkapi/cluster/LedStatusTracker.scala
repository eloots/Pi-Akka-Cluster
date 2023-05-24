package akkapi.cluster

import akka.actor.typed.{ActorSystem, Extension, ExtensionId}

class LedStatusTracker(system: ActorSystem[_]) extends Extension {

  private var started = false

  def start(): Unit =
    if (!started) {
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
      val clusterStatusTracker = system.systemActorOf(ClusterStatusTracker(settings, None), "cluster-status-tracker")
      clusterStatusTracker ! ClusterStatusTracker.SubscribeVisualiser(ledStripController)

      started = true
    }
}

object LedStatusTracker extends ExtensionId[LedStatusTracker] {

  def createExtension(system: ActorSystem[_]): LedStatusTracker = new LedStatusTracker(system)

  // Java API
  def get(system: ActorSystem[_]): LedStatusTracker = apply(system)
}
