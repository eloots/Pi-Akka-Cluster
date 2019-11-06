import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.{dockerBaseImage, dockerCommands}
import com.typesafe.sbt.packager.docker.{DockerPlugin, _}
import sbt.Keys._
import sbt._

val akkaVer = "2.5.26"
val scalaVer = "2.13.1"

lazy val commonSettings = Seq(
   organization := "com.lightbend.training",
   version := "1.3.0",
   scalaVersion := scalaVer,
   unmanagedSourceDirectories in Compile := List((scalaSource in Compile).value, (javaSource in Compile).value),
   unmanagedSourceDirectories in Test := List((scalaSource in Test).value, (javaSource in Test).value),
   assemblyExcludedJars in assembly := {
      val cp = (fullClasspath in assembly).value
      cp filter {
         _.data.getName().indexOf("javadoc.jar") > 0
      }
   },
)

lazy val excerciseConfig: Project => Project = (proj: Project) => {
   proj.settings(commonSettings: _*)
      .enablePlugins(DockerPlugin, JavaAppPackaging,AshScriptPlugin)
      .settings(libraryDependencies ++=
         Seq(
            "com.typesafe.akka" %% "akka-cluster" % akkaVer,
            "com.lightbend.akka.management" %% "akka-management" % "1.0.3",
            "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVer,
            "com.github.dnvriend" %% "akka-persistence-jdbc" % "3.5.2",
            "mysql" % "mysql-connector-java" % "8.0.18",
            "com.lightbend.akka.management" %% "akka-management-cluster-http" % "1.0.3",
            "com.typesafe.akka" %% "akka-distributed-data" % "2.5.26"
         ),
         mainClass in assembly := Some("com.lightbend.akka_oled.Main"),
         assemblyMergeStrategy in assembly := {
            case x if x.contains("io.netty.versions.properties") => MergeStrategy.discard
            case x =>
               val oldStrategy = (assemblyMergeStrategy in assembly).value
               oldStrategy(x)
         },
         dockerCommands += ExecCmd("RUN", "apt-get", "install", "wiringpi"),
         packageName in Docker := "oled-akka",
         dockerUpdateLatest := true,
         dockerBaseImage := "hypriot/rpi-java",
      )
}


lazy val common = project.settings(commonSettings: _*).
   settings(libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVer,
      "com.pi4j" % "pi4j-parent" % "1.2" pomOnly(),
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.apache.commons" % "commons-lang3" % "3.1",
      "commons-io" % "commons-io" % "2.5",
      "org.scalatest" %% "scalatest" % "3.0.8" % Test,
      "org.scalamock" %% "scalamock" % "4.4.0" % Test,
      "org.powermock" % "powermock-api-mockito2" % "2.0.2" % Test))

lazy val pi_cluster_display = (project in file("."))
   .aggregate(
      common,
      exercise_001_cluster_status,
      exercise_002_cluster_sharding,
      exercise_003_cluster_crdt
   ).settings(commonSettings: _*)

lazy val exercise_001_cluster_status = project
   .configure(excerciseConfig)
   .dependsOn(common % "test->test;compile->compile")

lazy val exercise_002_cluster_sharding = project
   .configure(excerciseConfig)
   .dependsOn(common % "test->test;compile->compile")

lazy val exercise_003_cluster_crdt = project
   .configure(excerciseConfig)
   .dependsOn(common % "test->test;compile->compile")
  