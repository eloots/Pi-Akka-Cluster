
lazy val pi_cluster_master = (project in file("."))
  .aggregate(
    common,
    exercise_000_initial_state,
    exercise_002_cluster_base,
    exercise_003_cluster_weakly_up,
    exercise_004_cluster_singleton,
    exercise_005_cluster_the_perils_of_auto_downing,
    exercise_006_cluster_weakly_up_disabled,
    exercise_007_split_brain_resolver_keep_majority,
    exercise_008_split_brain_resolver_static_quorum,
    exercise_009_split_brain_resolver_keep_referee,
    exercise_010_split_brain_resolver_keep_oldest,
    exercise_011_split_brain_resolver_down_all,
    exercise_012_split_brain_resolver_static_quorum_http_mamagement,
    exercise_013_clustered_sudoku_solver,
    exercise_014_add_cluster_client,
    exercise_015_clustered_sudoku_solver_cluster_client_enabled,
    exercise_017_es_opentracing,
    exercise_018_es_classic_console,
    exercise_050_cluster_cluster_singleton_akka_bootstrap_discovery_via_config,
    exercise_051_cluster_singleton_akka_bootstrap_discovery_via_akka_dns,
    exercise_060_cluster_sharding
 ).settings(CommonSettings.commonSettings: _*)

lazy val common = project.settings(CommonSettings.commonSettings: _*)

lazy val exercise_000_initial_state = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_002_cluster_base = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_003_cluster_weakly_up = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_004_cluster_singleton = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_005_cluster_the_perils_of_auto_downing = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_006_cluster_weakly_up_disabled = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_007_split_brain_resolver_keep_majority = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_008_split_brain_resolver_static_quorum = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_009_split_brain_resolver_keep_referee = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_010_split_brain_resolver_keep_oldest = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_011_split_brain_resolver_down_all = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_012_split_brain_resolver_static_quorum_http_mamagement = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_013_clustered_sudoku_solver = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_014_add_cluster_client = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_015_clustered_sudoku_solver_cluster_client_enabled = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_017_es_opentracing = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_018_es_classic_console = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_050_cluster_cluster_singleton_akka_bootstrap_discovery_via_config = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_051_cluster_singleton_akka_bootstrap_discovery_via_akka_dns = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_060_cluster_sharding = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")
