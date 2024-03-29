/**
  * Copyright © Eric Loots 2022 - eric.loots@gmail.com
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
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

import sbt.Keys.libraryDependencies
import sbt._

object Version {
  val akkaVer           = "2.6.19"
  val akkaHttpVer       = "10.2.9"
  val logbackVer        = "1.2.3"
  val scalaVersion      = "2.13.8"
  val akkaManagementVer = "1.1.3"
  val scalaTestVer      = "3.1.2"
}

object Dependencies {
  
  private val akkaDeps = Seq(
    "com.typesafe.akka"             %% "akka-actor-typed",
    "com.typesafe.akka"             %% "akka-serialization-jackson",
    "com.typesafe.akka"             %% "akka-cluster-typed",
    "com.typesafe.akka"             %% "akka-cluster-sharding-typed",
    "com.typesafe.akka"             %% "akka-persistence-typed",
    "com.typesafe.akka"             %% "akka-slf4j",
    "com.typesafe.akka"             %% "akka-stream",
    "com.typesafe.akka"             %% "akka-discovery",
    "com.typesafe.akka"             %% "akka-serialization-jackson",
  ).map (_ % Version.akkaVer)

  private val pi4jDeps = Seq(
    "com.pi4j" % "pi4j-core",
    "com.pi4j" % "pi4j-device",
    "com.pi4j" % "pi4j-gpio-extension"
  ).map (_ % "1.2")

  private val logbackDeps = Seq (
    "ch.qos.logback"                 %  "logback-classic",
  ).map (_ % Version.logbackVer)

  private val akkaHttpDeps = Seq(
    "com.typesafe.akka"             %% "akka-http",
    "com.typesafe.akka"             %% "akka-http-spray-json",
  ).map (_ % Version.akkaHttpVer)

  private val akkaManagementDeps = Seq(
    "com.lightbend.akka.management" %% "akka-management",
    "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap",
    "com.lightbend.akka.management" %% "akka-management-cluster-http",
  ).map (_ % Version.akkaManagementVer)

  private val scalaTestDeps = Seq(
    "org.scalatest"                 %% "scalatest" % Version.scalaTestVer % Test
  )

  private val akkaTestkitDeps = Seq(
    "com.typesafe.akka"             %% "akka-actor-testkit-typed" % Version.akkaVer % Test
  )

  private val persistenceDep = Seq(
    "com.typesafe.akka"            %% "akka-persistence-query" % Version.akkaVer,
    "com.github.dnvriend"          %% "akka-persistence-jdbc" % "3.5.2",
    "mysql"                        %  "mysql-connector-java" % "8.0.18"
  )

  private val commonsDep = Seq(
    "org.apache.commons" % "commons-lang3" % "3.1",
    "commons-io" % "commons-io" % "2.5"
  )

  private val mockDep = Seq(
    "org.mockito" %% "mockito-scala" % "1.11.2" % Test
  )

  val core_dependencies: Seq[ModuleID] =
    akkaDeps ++
      logbackDeps ++
      //commercialModulesDeps ++
      akkaHttpDeps ++
      akkaManagementDeps ++
      scalaTestDeps

  val eroled_dependencies: Seq[ModuleID] =
    mockDep ++
    akkaTestkitDeps ++
    pi4jDeps ++
    persistenceDep ++
    commonsDep
}
