package org.neopixel

import akka.actor.{Actor, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.typesafe.config.Config

import scala.concurrent.duration.{Duration, FiniteDuration, MILLISECONDS => Millis}

object ConfigSettingsExtension extends ExtensionId[ConfigSettingsImpl] with ExtensionIdProvider {
  override def lookup = ConfigSettingsExtension

  override def createExtension(system: ExtendedActorSystem) = new ConfigSettingsImpl(system)
}

class ConfigSettingsImpl(system: ExtendedActorSystem) extends Extension {

  private def validateColor(colorSetting: String)(implicit config: Config, colorMap: Map[String, Long]): Long = {
    val color = config.getString(colorSetting).toUpperCase
    if (colorMap.contains(color)) {
      colorMap(color)
    } else throw new Exception(s"$color: invalid color for $colorSetting")
  }

  implicit val colorMap: Map[String, Long] = availableColorMap.map { case (x, y) => (x.toUpperCase, y)}

  private implicit val config: Config = system.settings.config

  val nodeUpColor: Long =
    validateColor("cluster-status-indicator.cluster-node-colors.cluster-node-up-color")

  val nodeWeaklyUpColor: Long =
    validateColor("cluster-status-indicator.cluster-node-colors.cluster-node-weakly-up-color")

  val nodeDownColor: Long =
    validateColor("cluster-status-indicator.cluster-node-colors.cluster-node-down-color")

  val nodeLeftColor: Long =
    validateColor("cluster-status-indicator.cluster-node-colors.cluster-node-left-color")

  val nodeExitedColor: Long =
    validateColor("cluster-status-indicator.cluster-node-colors.cluster-node-exited-color")

  val nodeUnreachableColor: Long =
    validateColor("cluster-status-indicator.cluster-node-colors.cluster-node-unreachable-color")

  val nodeJoinedColor: Long =
    validateColor("cluster-status-indicator.cluster-node-colors.cluster-node-joined-color")

  val leaderIndicatorColor: Long =
    validateColor("cluster-status-indicator.cluster-leader-indicator-color")

  val heartbeartIndicatorColor: Long =
    validateColor("cluster-status-indicator.cluster-heartbeat-indicator-color")

  val heartbeatIndicatorInterval: FiniteDuration =
    Duration(system.settings.config.getDuration("cluster-status-indicator.cluster-heartbeat-indicator-interval", Millis), Millis)
}

trait SettingsActor {
  this: Actor =>

  val settings: ConfigSettingsImpl = ConfigSettingsExtension(context.system)
}