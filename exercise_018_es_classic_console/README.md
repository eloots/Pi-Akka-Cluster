# How to demonstrate Enterprise Suite Classic Console with Raspberry Pi Akka Cluster

## Exercise - Telemetry Demo
In this exercise, you will be able to see your cluster telemetry using Lightbend Telemetry plugin
with a Prometheus-based sandbox

### Demo Setup Pre-requisite steps

1. Ensure you have password-less ssh setup between your laptop and your Raspberry Pi Cluster. 
	2. Steps found [here:](https://github.com/lightbend/Pi-Akka-Cluster/blob/master/Hypriot-OS-Customisation-Instructions.md#configure-password-less-login-1)
2. ssh into each of the Raspberry Pi nodes in the cluster with user `akkapi`
> Hint 1: Use a multi-input terminal to make this easier. For example with a mac you can use iTerm2 [link to iTerm2](https://www.iterm2.com)
 
### Demo Steps
1. Run `sbt universal:packageBin` to create the packaged binaries.
2. Use the `copy` script to copy the binaries to each node in the cluster.
3. start the Lightbend Telemetry sandbox on your laptop: 
    * `cd exercise_018_es_classic_console`
	* `docker-compose up -d`
4. After the docker images have started, log into grafana on your laptop at the address `localhost:3000`
> the username and password for grafana default to `admin\admin`
5. Enable the Prometheus plugin
    * ![prometheus plugin](../docs/images/prometheus-plugin-enable.png)
6. Open the Lightbend Dashboard home
5. Run the example on the cluster by using the `run` command with the appropriate exercise number.
6. View your metrics in the pre-enabled Lightbend Telemetry dashboard