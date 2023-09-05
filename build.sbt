import sbt._

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val `pi_cluster_master` = (project in file("."))
  .aggregate(
    core,
    `exercise_000_initial_state`,
    `exercise_001_cluster_base`,
    `exercise_002_cluster_weakly_up`,
    `exercise_003_cluster_singleton`,
    `exercise_004_cluster_weakly_up_disabled`,
    `exercise_005_cluster_akka_bootstrap_discovery_via_config`,
    `exercise_006_cluster_akka_bootstrap_discovery_via_akka_dns`,
    `exercise_007_cluster_split_brain_resolver_keep_majority`,
    `exercise_008_cluster_split_brain_resolver_static_quorum`,
    `exercise_010_split_brain_resolver_keep_oldest`,
    `exercise_011_split_brain_resolver_down_all`,
    `exercise_012_clustered_sudoku_solver`,
    `exercise_014_add_cluster_client`,
    `exercise_015_clustered_sudoku_solver_cluster_client_enabled`,
    `exercise_017_es_opentracing`,
    `exercise_018_es_classic_console`,
    `exercise_101_display_cluster_status`,
    `exercise_102_display_cluster_sharding`,
    `exercise_103_display_cluster_crdt`
  )
  .settings(ThisBuild / scalaVersion := Version.scalaVersion)
  .settings(CommonSettings.commonSettings: _*)

lazy val core = project
  .settings(CommonSettings.commonSettings: _*)

lazy val core_eroled = project
  .settings(CommonSettings.commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.eroled_dependencies)
  .dependsOn(core % "test->test;compile->compile")

lazy val `exercise_000_initial_state` = project
  .configure(CommonSettings.configure)
  .dependsOn(core % "test->test;compile->compile")

lazy val `exercise_001_cluster_base` = project
  .configure(CommonSettings.configure)
  .dependsOn(core % "test->test;compile->compile")

lazy val `exercise_002_cluster_weakly_up` = project
  .configure(CommonSettings.configure)
  .dependsOn(core % "test->test;compile->compile")

lazy val `exercise_003_cluster_singleton` = project
  .configure(CommonSettings.configure)
  .dependsOn(core % "test->test;compile->compile")

lazy val `exercise_004_cluster_weakly_up_disabled` = project
  .configure(CommonSettings.configure)
  .dependsOn(core % "test->test;compile->compile")

lazy val `exercise_005_cluster_akka_bootstrap_discovery_via_config` = project
  .configure(CommonSettings.configure)
  .dependsOn(core % "test->test;compile->compile")

lazy val `exercise_006_cluster_akka_bootstrap_discovery_via_akka_dns` = project
  .configure(CommonSettings.configure)
  .dependsOn(core % "test->test;compile->compile")

lazy val `exercise_007_cluster_split_brain_resolver_keep_majority` = project
  .configure(CommonSettings.configure)
  .dependsOn(core % "test->test;compile->compile")

lazy val `exercise_008_cluster_split_brain_resolver_static_quorum` = project
  .configure(CommonSettings.configure)
  .dependsOn(core % "test->test;compile->compile")

lazy val `exercise_010_split_brain_resolver_keep_oldest` = project
  .configure(CommonSettings.configure)
  .dependsOn(core % "test->test;compile->compile")

lazy val `exercise_011_split_brain_resolver_down_all` = project
  .configure(CommonSettings.configure)
  .dependsOn(core % "test->test;compile->compile")

lazy val `exercise_012_clustered_sudoku_solver` = project
  .configure(CommonSettings.configure)
  .dependsOn(core % "test->test;compile->compile")

lazy val `exercise_014_add_cluster_client` = project
  .configure(CommonSettings.configure)
  .dependsOn(core % "test->test;compile->compile")

lazy val `exercise_015_clustered_sudoku_solver_cluster_client_enabled` = project
  .configure(CommonSettings.configure)
  .dependsOn(core % "test->test;compile->compile")

lazy val `exercise_017_es_opentracing` = project
  .configure(CommonSettings.configure)
  .dependsOn(core % "test->test;compile->compile")

lazy val `exercise_018_es_classic_console` = project
  .configure(CommonSettings.configure)
  .dependsOn(core % "test->test;compile->compile")

lazy val `exercise_101_display_cluster_status` = project
  .configure(CommonSettings.configure)
  .dependsOn(core_eroled % "test->test;compile->compile")

lazy val `exercise_102_display_cluster_sharding` = project
  .configure(CommonSettings.configure)
  .dependsOn(core_eroled % "test->test;compile->compile")

lazy val `exercise_103_display_cluster_crdt` = project
  .configure(CommonSettings.configure)
  .dependsOn(core_eroled % "test->test;compile->compile")
       
