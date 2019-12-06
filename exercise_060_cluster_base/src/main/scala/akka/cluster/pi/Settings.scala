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

package akka.cluster.pi

import akka.actor.typed.ActorSystem
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import org.neopixel._

import scala.concurrent.duration.{Duration, FiniteDuration, MILLISECONDS => Millis}
import scala.jdk.CollectionConverters._

object Settings {

  def apply(): Settings = {
    val baseConfig = ConfigFactory.load()

    val nodeHostname = baseConfig.getString("cluster-node-configuration.node-hostname")

    implicit val config = baseConfig.withValue("akka.remote.artery.canonical.hostname", ConfigValueFactory.fromAnyRef(nodeHostname))

    new Settings()
  }
}

class Settings(implicit val config: Config) {

  private def validateColor(colorSetting: String)(implicit config: Config, colorMap: Map[String, Long]): Long = {
    val color = config.getString(colorSetting).toUpperCase
    if (colorMap.contains(color)) {
      colorMap(color)
    } else throw new Exception(s"$color: invalid color for $colorSetting")
  }

  implicit val colorMap: Map[String, Long] = availableColorMap.map { case (x, y) => (x.toUpperCase, y)}

  private val clusterNodeConfig = config.getConfig("cluster-node-configuration")

  val actorSystemName = s"pi-${config.getString("cluster-node-configuration.cluster-id")}-system"

  private val clusterId = clusterNodeConfig.getString("cluster-id")

  private val clusterNodeToLedMapping = clusterNodeConfig.getConfig(s"cluster-node-to-led-mapping.$clusterId")

  val HostToLedMapping: Map[String, Int] = (for {
    mapping <- clusterNodeToLedMapping.entrySet().asScala
  } yield (mapping.getKey, clusterNodeToLedMapping.getInt(mapping.getKey))).toMap

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

  val heartbeatIndicatorColor: Long =
    validateColor("cluster-status-indicator.cluster-heartbeat-indicator-color")

  val heartbeatIndicatorConvergenceColor: Long =
    validateColor("cluster-status-indicator.cluster-heartbeat-indicator-convergence-color")

  val heartbeatIndicatorNoConvergenceColor: Long =
    validateColor("cluster-status-indicator.cluster-heartbeat-indicator-no-convergence-color")

  val heartbeatIndicatorInterval: FiniteDuration =
    Duration(config.getDuration("cluster-status-indicator.cluster-heartbeat-indicator-interval", Millis), Millis)

  val weaklyUpIndicatorInterval: FiniteDuration =
    Duration(config.getDuration("cluster-status-indicator.cluster-weakly-up-indicator-interval", Millis), Millis)

  val clusterStateConvergenceInterval: FiniteDuration =
    Duration(config.getDuration("cluster-status-indicator.cluster-heartbeat-indicator-convergence-interval", Millis), Millis)

  object LedStripConfig {

    val ledBrightness: Short =
      config.getInt("cluster-status-indicator.led-brightness").toShort

    val ledCount: Int =
      config.getInt("cluster-status-indicator.led-count")

    val ledPin: Int =
      config.getInt("cluster-status-indicator.led-pin")

    val ledFreqHz: Int =
      config.getInt("cluster-status-indicator.led-freq-hz")

    val ledDma: Int =
      config.getInt("cluster-status-indicator.led-dma")

    val ledInvert: Boolean =
      config.getBoolean("cluster-status-indicator.led-invert")

    val ledChannel: Int =
      config.getInt("cluster-status-indicator.led-channel")
  }
}