
lazy val pi_cluster_master = (project in file("."))
  .aggregate(
    common,
    exercise_000_initial_state,
    exercise_001_cluster_base,
    exercise_002_cluster_weakly_up,
    exercise_003_cluster_the_perils_of_auto_downing,
    exercise_004_cluster_weakly_up_disabled,
    exercise_005_split_brain_resolver_keep_majority,
    exercise_006_split_brain_resolver_static_quorum,
    exercise_007_split_brain_resolver_keep_referee,
    exercise_008_split_brain_resolver_keep_oldest,
    exercise_009_UDP_experiments,
    exercise_010_split_off_LED_status_monitor,
    exercise_011_cluster_state_monitor_improved,
    exercise_012_split_brain_resolver_static_quorum_http_mamagement
 ).settings(CommonSettings.commonSettings: _*)

lazy val common = project.settings(CommonSettings.commonSettings: _*)

lazy val exercise_000_initial_state = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_001_cluster_base = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_002_cluster_weakly_up = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_003_cluster_the_perils_of_auto_downing = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_004_cluster_weakly_up_disabled = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_005_split_brain_resolver_keep_majority = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_006_split_brain_resolver_static_quorum = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_007_split_brain_resolver_keep_referee = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_008_split_brain_resolver_keep_oldest = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_009_UDP_experiments = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_010_split_off_LED_status_monitor = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_011_cluster_state_monitor_improved = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_012_split_brain_resolver_static_quorum_http_mamagement = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")
       