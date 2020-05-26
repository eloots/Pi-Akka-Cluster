package com.lightbend.akka_oled

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, get, onSuccess, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.lightbend.akka_oled.DistributedDataTracker.{Get, UpdateStatus}
import com.lightbend.akka_oled.Main.NodeStatus

import scala.concurrent.duration._

class Routes(tracker: ActorRef[DistributedDataTracker.Command])(implicit system: ActorSystem[_]) extends SprayJsonSupport {
  implicit val timeout: Timeout = 8.seconds

  val route: Route =
    pathPrefix("status" / "[0-9a-zA-Z]+".r) {
      node =>
        concat(
          get {
            onSuccess(tracker.ask[String](Get(node, _))) {
              value => complete(value + "\n")
            }
          },
          post {
            entity(as[NodeStatus]) { status =>
              tracker ! UpdateStatus(node, status.status)
              complete("Ok\n")
            }
          }
        )
    }


}
