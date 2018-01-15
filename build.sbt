
lazy val base = (project in file("."))
  .aggregate(
    common,
    exercise_000_initial_state,
    exercise_001_cluster_base,
    exercise_002_UDP_experiments,
    exercise_003_split_off_LED_status_monitor,
    exercise_004_cluster_state_monitor_improved
 )
  .settings(CommonSettings.commonSettings: _*)


lazy val common = project
  .settings(CommonSettings.commonSettings: _*)

lazy val exercise_000_initial_state = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_001_cluster_base = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_002_UDP_experiments = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_003_split_off_LED_status_monitor = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_004_cluster_state_monitor_improved = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

