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

import com.lightbend.cinnamon.sbt.Cinnamon
import com.lightbend.sbt.javaagent.JavaAgent.JavaAgentKeys
import sbt.Keys._
import sbt._
import sbtassembly._
import sbtstudent.AdditionalSettings

object CommonSettings {
  lazy val commonSettings = Seq(
    organization := "com.lightbend.training",
    version := "1.3.0",
    scalaVersion := Version.scalaVer,
    scalacOptions ++= CompileOptions.compileOptions,
    unmanagedSourceDirectories in Compile := List((scalaSource in Compile).value, (javaSource in Compile).value),
    unmanagedSourceDirectories in Test := List((scalaSource in Test).value, (javaSource in Test).value),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-v"),
    logBuffered in Test := false,
    parallelExecution in Test := false,
    parallelExecution in GlobalScope := false,
    parallelExecution in ThisBuild := false,
    fork in Test := false,
    libraryDependencies ++= Dependencies.dependencies,
    credentials += Credentials(Path.userHome / ".lightbend" / "commercial.credentials"),
    resolvers += "com-mvn" at "https://repo.lightbend.com/commercial-releases/",
    resolvers += Resolver.url("com-ivy", url("https://repo.lightbend.com/commercial-releases/"))(Resolver.ivyStylePatterns)
  ) ++
    AdditionalSettings.initialCmdsConsole ++
    AdditionalSettings.initialCmdsTestConsole ++
    AdditionalSettings.cmdAliases

  lazy val configure: Project => Project = (proj: Project) => {
    proj
    .enablePlugins(Cinnamon)
    .settings(CommonSettings.commonSettings: _*)
    .settings(
      libraryDependencies += Cinnamon.library.cinnamonPrometheus,
      libraryDependencies += Cinnamon.library.cinnamonPrometheusHttpServer,
      libraryDependencies += Cinnamon.library.cinnamonAkkaHttp,
      libraryDependencies += Cinnamon.library.cinnamonOpenTracingZipkin,
      AssemblyKeys.assembly := Def.task {
        JavaAgentKeys.resolvedJavaAgents.value.filter(_.agent.name == "Cinnamon").foreach { agent =>
          sbt.IO.copyFile(agent.artifact, target.value / "cinnamon-agent.jar")
        }
        AssemblyKeys.assembly.value
      }.value,
      AssemblyKeys.assemblyMergeStrategy in AssemblyKeys.assembly := {
        case PathList("reference.conf", _ @ _*) => MergeStrategy.discard
        case PathList("META-INF", _ @ _*) => MergeStrategy.discard
        case _ => MergeStrategy.first
      }
    )
  }
}
