
lazy val pi_cluster_master = (project in file("."))
  .aggregate(
    common,
    exercise_000_initial_state,
    exercise_001_cluster_base,
    exercise_002_cluster_weakly_up,
    exercise_003_cluster_weakly_up_disabled,
    exercise_004_split_brain_resolver_keep_majority,
    exercise_005_split_brain_resolver_static_quorum,
    exercise_006_split_brain_resolver_keep_referee,
    exercise_007_split_brain_resolver_keep_oldest,
    exercise_008_UDP_experiments,
    exercise_009_split_off_LED_status_monitor,
    exercise_010_cluster_state_monitor_improved
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

lazy val exercise_003_cluster_weakly_up_disabled = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_004_split_brain_resolver_keep_majority = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_005_split_brain_resolver_static_quorum = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_006_split_brain_resolver_keep_referee = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_007_split_brain_resolver_keep_oldest = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_008_UDP_experiments = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_009_split_off_LED_status_monitor = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_010_cluster_state_monitor_improved = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")
       