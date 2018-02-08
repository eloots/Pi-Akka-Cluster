/**
  * Copyright © 2018 Lightbend, Inc
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

package org.neopixel

import java.net._

import akka.actor.ActorSystem

import scala.concurrent.Future

object UDP_simple_client {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("udp-simple-client-system")
    val serverAddress = new InetSocketAddress("localhost", 2500)
    val log = system.log
    import scala.concurrent.ExecutionContext.Implicits.global

    val client = system.actorOf(UDPSimpleSender.props(serverAddress), "simple-client")

    Thread.sleep(500)

    while (true) {
      client ! "A string ...."
      Thread.sleep(500)
    }

    system.terminate().flatMap(t => Future.unit).onComplete { _ => println(s"UDP client terminated")}

  }
}
