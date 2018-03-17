package org.neopixel

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion

object PingPong {

  case class Envelope(name: String, payload: Any)

  val typeName: String =
    "ping-pong"

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case PingPong.Envelope(name, payload) => (name, payload)
  }

  def extractShardId(shardCount: Int): ShardRegion.ExtractShardId= {
    case PingPong.Envelope(name, _) => name.hashCode % shardCount toString
  }

  def props: Props = Props(new PingPong)
}

class PingPong extends Actor with ActorLogging {

  import PingPong._

  override def receive: Receive = akka.actor.Actor.emptyBehavior

}
