split_off_led_status_indicator

```scala
################################################################
# THIS IS WORK IN PROGRESS - SKIP TO exercise_004_cluster_base #
################################################################
```

# Split off LED Status Indicator

In this stage, the LED status monitor is split-off in a separate process.

As the separated LED status monitor is implemented in Akka, communication
with the Akka Cluster process could be done using Akka Remote. This comes
with some risk though: the LED status indicator may get quarantined, a
situation that might necessitate a restart of the LED status indicator.

Instead, we use to unconnected UDP connections to exchange data.

The LED Status indicator process will poll the Akka Cluster node. If it
doesn't receive any response for a pre-configured number of polls, the Akka
Cluster node is considered to be down, and the status LED's are dimmed.

Otherwise, the LED Status indicator will receive the current state of all
nodes and update the LED's accordingly.

