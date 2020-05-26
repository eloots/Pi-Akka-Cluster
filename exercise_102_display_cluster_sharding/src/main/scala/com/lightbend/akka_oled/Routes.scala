package com.lightbend.akka_oled

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, get, onSuccess, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.lightbend.akka_oled.ClientEntity.{Get, PostPoints}
import com.lightbend.akka_oled.Main.AddPoints

import scala.concurrent.duration._
object Routes{
  case class NodeStatus(status:String)

}
class Routes(sharding: ClusterSharding)(implicit system: ActorSystem[_]) extends SprayJsonSupport {
  implicit val timeout: Timeout = 8.seconds
  implicit val scheduler = system.scheduler

  lazy val route: Route =
    pathPrefix("user" / "[0-9a-zA-Z]+".r) { username =>
      concat(
        get {
          val entityRef = sharding.entityRefFor(ClientEntity.TypeKey, username)
          onSuccess(entityRef ? Get(username)) {
            value: Int => complete(value.toString + "\n")
          }
        },
        post {
          entity(as[AddPoints]) { transaction =>
            val entityRef = sharding.entityRefFor(ClientEntity.TypeKey, username)
            onSuccess(entityRef ? PostPoints(username, transaction.points)) {
              result => complete(result)
            }
          }
        }
      )
    }

}
