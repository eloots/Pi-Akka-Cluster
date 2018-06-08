UDP_experiments

```scala
################################################################
# THIS IS WORK IN PROGRESS - SKIP TO exercise_004_cluster_base #
################################################################
```

# Experiments with UDP

In a later stage, the driving of the LED status strip is moved into a
separate process.

The reason for doing so is that, when the cluster node's actor system dies,
is killed or is simply suspended, the LEDs continue to display the state of
the last update. This is confusing to say the least. Ideally, in such cases
all LEDs should be turned off after some amount of time.

Code segments taken from (https://doc.akka.io/docs/akka/current/io-udp.html)

In this step, we use some simple applications that communicate via UDP using
package `akka.io.Udp`.

The (low-level) UDP API allows for communication via UDP between two endpoints
either in `connected` or `unconnected` mode. `Connected` mode strictly restricts
communication between two connected endpoints and no other and provides the
possibility to send responses.

Testing reveals that the unconnected method is the way to go as we don't have
to deal with reconnects when the server side is restarted.

In order to test the two, always run the server (object `UDP_server`) and one
of the clients:

- object `UDP_simple_client` for the `unconnected` version
- object `UDP_client` for the `connected` version

Observations:

- The `unconnected` version will work regardless of the order in which
  server/client are started. Restarting server or client will result in a
  transmission of datagrams being re-established
- The `unconnected` version will stop receiving datagrams in case the server
  is restarted
