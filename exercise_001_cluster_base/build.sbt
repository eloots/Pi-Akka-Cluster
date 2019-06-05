import com.typesafe.sbt.packager.docker.DockerChmodType.UserGroupWriteExecute
import com.typesafe.sbt.packager.docker.{Cmd, DockerChmodType, DockerPlugin}

enablePlugins(DockerPlugin, JavaServerAppPackaging)

mappings in Universal += file("librpi_ws281x.so") -> "lib/librpi_ws281x.so"
dockerBaseImage := "hypriot/rpi-java"
dockerCommands ++= Seq( Cmd("USER", "root"),
  Cmd("RUN", "mkdir -p","/dev/mem")  )

dockerChmodType := UserGroupWriteExecute

dockerRepository := Some("docker-registry-default.gsa2.lightbend.com/lightbend")
dockerExposedPorts := Seq(8080, 8558, 2550, 9001)

dockerChmodType := UserGroupWriteExecute
dockerAdditionalPermissions ++= Seq((DockerChmodType.UserGroupPlusExecute, "/tmp"))

dockerEnvVars := Map("LED_STRIP_TYPE" -> "eight-led-reversed-order" , "java.library.path" -> "lib")


