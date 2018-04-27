
lazy val pi_cluster_master = (project in file("."))
  .aggregate(
    common,
    exercise_000_initial_state,
    exercise_001_UDP_experiments,
    exercise_002_split_off_LED_status_monitor,
    exercise_003_cluster_state_monitor_improved,
    exercise_004_cluster_base,
    exercise_005_cluster_base_move_to_artery_tcp,
    exercise_006_cluster_weakly_up,
    exercise_007_cluster_cluster_singleton,
    exercise_008_cluster_the_perils_of_auto_downing,
    exercise_009_cluster_weakly_up_disabled,
    exercise_010_split_brain_resolver_keep_majority,
    exercise_011_split_brain_resolver_static_quorum,
    exercise_012_split_brain_resolver_keep_referee,
    exercise_013_split_brain_resolver_keep_oldest,
    exercise_014_split_brain_resolver_static_quorum_http_mamagement
 ).settings(CommonSettings.commonSettings: _*)

lazy val common = project.settings(CommonSettings.commonSettings: _*)

lazy val exercise_000_initial_state = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_001_UDP_experiments = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_002_split_off_LED_status_monitor = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_003_cluster_state_monitor_improved = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_004_cluster_base = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_005_cluster_base_move_to_artery_tcp = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_006_cluster_weakly_up = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_007_cluster_cluster_singleton = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_008_cluster_the_perils_of_auto_downing = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_009_cluster_weakly_up_disabled = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_010_split_brain_resolver_keep_majority = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_011_split_brain_resolver_static_quorum = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_012_split_brain_resolver_keep_referee = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_013_split_brain_resolver_keep_oldest = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_014_split_brain_resolver_static_quorum_http_mamagement = project
  .configure(CommonSettings.configure)
  .dependsOn(common % "test->test;compile->compile")
