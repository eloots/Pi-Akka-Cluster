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

import sbt._

object Version {
  val akkaVer           = "2.5.26"
  val akkaHttpVer       = "10.1.10"
  val logbackVer        = "1.2.3"
  val scalaVer          = "2.13.1"
  val akkaSBRVer        = "1.1.12"
  val akkaManagementVer = "1.0.3"
  val scalaTestVer      = "3.0.8"
}

object Dependencies {
  
  private val akkaDeps = Seq(
    "com.typesafe.akka"             %% "akka-actor",
    "com.typesafe.akka"             %% "akka-slf4j",
    "com.typesafe.akka"             %% "akka-cluster-tools",
    "com.typesafe.akka"             %% "akka-cluster",
    "com.typesafe.akka"             %% "akka-cluster-sharding",
    "com.typesafe.akka"             %% "akka-distributed-data",
    "com.typesafe.akka"             %% "akka-persistence",
    "com.typesafe.akka"             %% "akka-slf4j",
    "com.typesafe.akka"             %% "akka-stream",
    "com.typesafe.akka"             %% "akka-discovery"
  ).map (_ % Version.akkaVer)
  
  private val logbackDeps = Seq (
    "ch.qos.logback"                 %  "logback-classic",
    "ch.qos.logback"                 %  "logback-classic",
  ).map (_ % Version.logbackVer)
  
//  private val commercialModulesDeps = Seq(
//    "com.lightbend.akka"            %% "akka-split-brain-resolver",
//  ).map (_ % Version.akkaSBRVer)
  
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
    "com.typesafe.akka"             %% "akka-testkit" % Version.akkaVer % Test
  )

  val dependencies: Seq[ModuleID] =
    akkaDeps ++
    logbackDeps ++
    //commercialModulesDeps ++
    akkaHttpDeps ++
    akkaManagementDeps ++ 
    scalaTestDeps ++ 
    akkaTestkitDeps
}
