import com.typesafe.sbt.packager.docker.{Cmd}
import sbtassembly.AssemblyKeys.assembly
import com.typesafe.sbt.packager.docker.DockerChmodType
import com.typesafe.sbt.packager.docker.DockerPlugin

enablePlugins(DockerPlugin, JavaAppPackaging)

dockerRepository := Some("docker-registry-default.gsa2.lightbend.com/lightbend")
dockerExposedPorts := Seq(8080, 8558, 2552, 9001)
dockerBaseImage := "hypriot/rpi-java"
dockerChmodType := DockerChmodType.UserGroupWriteExecute

val artifactPath = ""//assembly.value.getPath
val cinnamonFilePath = ""//file(baseDirectory.value + "/target/cinnamon-agent.jar").getPath
val ledDriverFilePath = ""//file(baseDirectory.value + "librpi_ws281x.so").getPath

val entryPoint = s"java -javaagent:$cinnamonFilePath " +
  s" -Djava.library.path=. " +
  s" -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 " +
  s" -Dcom.sun.management.jmxremote.local.only=false" +
  s" -Dcom.sun.management.jmxremote.authenticate=false " +
  s" -Dcom.sun.management.jmxremote.ssl=false" //+
  //s" -Dcluster-node-configuration.node-hostname=${``hostname``}"

dockerCommands ++= Seq(
  Cmd("ENV", "LED_STRIP_TYPE", "eight-led-reversed-order"),
  Cmd("ADD", "artifactPath"),
  Cmd("ADD", "cinnamonFilePath"),
  Cmd("ENTRYPOINT", "entryPoint")
)
