import sbt._

object Version {
  val akkaVer         = "2.5.9"
  val logbackVer      = "1.2.3"
  val scalaVer        = "2.12.4"
  val scalaParsersVer = "1.0.4"
}

object Dependencies {
  val dependencies = Seq(
    "com.typesafe.akka"         %% "akka-actor"                 % Version.akkaVer,
    "com.typesafe.akka"         %% "akka-slf4j"                 % Version.akkaVer,
    "com.typesafe.akka"         %% "akka-cluster-tools"         % Version.akkaVer,
    "com.typesafe.akka"         %% "akka-cluster"               % Version.akkaVer,
    "com.typesafe.akka"         %% "akka-cluster-sharding"      % Version.akkaVer,
    "com.typesafe.akka"         %% "akka-distributed-data"      % Version.akkaVer,
    "com.typesafe.akka"         %% "akka-persistence"           % Version.akkaVer,
    "com.typesafe.akka"         %% "akka-slf4j"                 % Version.akkaVer,
    "ch.qos.logback"            %  "logback-classic"            % Version.logbackVer,
    "ch.qos.logback"            %  "logback-classic"            % Version.logbackVer,
    "com.typesafe.akka"         %% "akka-testkit"               % Version.akkaVer            % Test
  )
}
