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

//import com.lightbend.cinnamon.sbt.Cinnamon
//import com.lightbend.sbt.javaagent.JavaAgent.JavaAgentKeys
import sbt.Keys._
import sbt._
import com.typesafe.sbt.packager.archetypes.{JavaAppPackaging, JavaServerAppPackaging}
import com.typesafe.sbt.packager.docker.DockerChmodType.UserGroupWriteExecute
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.{dockerAdditionalPermissions, dockerBaseImage, dockerChmodType, dockerCommands, dockerEnvVars, dockerExposedPorts, dockerRepository}
import com.typesafe.sbt.packager.docker.{Cmd, DockerChmodType, DockerPlugin}
import com.typesafe.sbt.packager.universal.UniversalPlugin, UniversalPlugin.autoImport._

object CommonSettings {
  lazy val commonSettings = Seq(
    organization := "com.lightbend.training",
    version := "1.3.0",
    scalaVersion := Version.scalaVersion,
    Compile  /scalacOptions ++= CompileOptions.compileOptions,
    Compile / javacOptions ++= Seq("--release", "11"),
    Compile / unmanagedSourceDirectories := List((Compile / scalaSource).value, (Compile / javaSource).value),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-v"),
    Test / logBuffered := false,
    Test / unmanagedSourceDirectories := List((Test / scalaSource).value, (Test / javaSource).value),
    Test / parallelExecution := false,
    GlobalScope / parallelExecution := false,
    Test / fork := false,
    packageSrc / publishArtifact := false,
    packageDoc / publishArtifact := false,
    libraryDependencies ++= Dependencies.core_dependencies,
  )

  lazy val configure: Project => Project = (proj: Project) => {
    proj
      .settings(CommonSettings.commonSettings: _*)
      .enablePlugins(DockerPlugin, JavaAppPackaging)
      .settings(
        Universal / mappings ++=
          Seq(
            file("nodeFiles/librpi_ws281x.so") -> "lib/librpi_ws281x.so",
            file("nodeFiles/librpi_ws281x_64.so") -> "lib/librpi_ws281x_64.so",
            file("sudokus/001.sudoku") -> "sudokus/001.sudoku"
          ),
        Universal / javaOptions ++=
          Seq(
            "-Djava.library.path=lib",
            "-Dcluster-node-configuration.cluster-id=cluster-0",
            "-Dcluster-status-indicator.led-strip-type=ten-led-non-reversed-order"
          ),
        //dockerBaseImage := "arm32v7/openjdk",
        dockerBaseImage := "arm32v7/adoptopenjdk",
        dockerCommands ++= Seq( Cmd("USER", "root"),
          Cmd("RUN", "mkdir -p","/dev/mem")  ),
        dockerChmodType := UserGroupWriteExecute,
        dockerRepository := Some("docker-registry-default.gsa2.lightbend.com/lightbend"),
        dockerExposedPorts := Seq(8080, 8558, 2550, 9001),
        dockerAdditionalPermissions ++= Seq((DockerChmodType.UserGroupPlusExecute, "/tmp"))
      )
  }

  lazy val eroled_configure: Project => Project = (proj: Project) => {
    proj
      .settings(CommonSettings.commonSettings: _*)
      .settings(libraryDependencies ++= Dependencies.eroled_dependencies)
      .enablePlugins(DockerPlugin, JavaAppPackaging)
      .settings(
        Universal / mappings ++=
          Seq(
            file("nodeFiles/librpi_ws281x.so") -> "lib/librpi_ws281x.so",
            file("nodeFiles/librpi_ws281x_64.so") -> "lib/librpi_ws281x_64.so",
            file("sudokus/001.sudoku") -> "sudokus/001.sudoku"
          ),
        Universal / javaOptions ++=
          Seq(
            "-Djava.library.path=lib",
            "-Dcluster-node-configuration.cluster-id=cluster-0",
            "-Dcluster-status-indicator.led-strip-type=ten-led-non-reversed-order"
          ),
        //dockerBaseImage := "arm32v7/openjdk",
        dockerBaseImage := "arm32v7/adoptopenjdk",
        dockerCommands ++= Seq( Cmd("USER", "root"),
          Cmd("RUN", "mkdir -p","/dev/mem")  ),
        dockerChmodType := UserGroupWriteExecute,
        dockerRepository := Some("docker-registry-default.gsa2.lightbend.com/lightbend"),
        dockerExposedPorts := Seq(8080, 8558, 2550, 9001),
        dockerAdditionalPermissions ++= Seq((DockerChmodType.UserGroupPlusExecute, "/tmp"))
      )
  }
}
