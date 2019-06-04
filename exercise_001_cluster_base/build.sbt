import com.typesafe.sbt.packager.docker.{DockerChmodType, DockerPlugin}

mappings in Universal += file("librpi_ws281x.so") -> "lib/librpi_ws281x.so"

enablePlugins(DockerPlugin, JavaServerAppPackaging)

dockerRepository := Some("docker-registry-default.gsa2.lightbend.com/lightbend")
dockerExposedPorts := Seq(8080, 8558, 2552, 9001)
dockerBaseImage := "hypriot/rpi-java"
dockerChmodType := DockerChmodType.UserGroupWriteExecute
dockerEnvVars := Map("LED_STRIP_TYPE" -> "eight-led-reversed-order" , "java.library.path" -> "lib/.")



