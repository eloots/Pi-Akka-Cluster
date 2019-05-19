# Switch remoting transport from default to Artery-TCP

`Akka 2.5.11` introduced an new remoting transport named `Artery TCP`.

`Artery TCP` is built on top of `Akka Streams` and provides an alternative
for the existing TCP based transport.

Apart from giving lower latency and higher throughput, `Artery TCP`
also utilises separate TCP connections for user- and system traffic.
This is important as it prevents user traffic from 'pushing out'
system messages which in turn may lead to pushing out `Akka Cluster`
gossip messages.

It is expected that `Artery TCP` will become the default remoting
transport protocol in a future `Akka` release.

In this example, we take the code from the previous exercise and
switch the transport to `Artery TCP`

# Instructions

- `Artery TCP` is enabled by swapping the remote transport setting
  which uses `Netty TCP` for one that uses the new transport. Look
  at the slide deck for more details

- Note that, in contrast with the default remoting transport, `Artery TCP`
  uses `akka` instead of `akka.tcp` in the URI's protocol field.

  For example, the following URI valid for the default transport:

```scala
    akka.tcp://my-favorite-cluster-system@some-node:2550
```

  will change to the following version when using `Artery TCP`:

```scala
   akka://my-favorite-cluster-system@some-node:2550
```

- Adapt any URIs in the configuration file the reflect this change
  in the protocol field

- Build and transfer the new version of the code to the nodes
- Verify that everything continues to work as in the previous exercise

# LED Legend

- LEDs 1 to 5 show the status of each node as seen by a node
    - Green:      Node is Up
    - Red:        Node is Down
    - Cyan:       Node in Leaving
    - Magenta:    Node is Exiting
    - White:      Node is Unreachable
    - Dark Green: Node is WeaklyUp - The LED is blinking

- LED number 6 indicates is Cyan when the node has a leader role
- LED number 7 is unused
- LED number 8 is the cluster liveliness indicator: when it blinks, we know
that the cluster node software is actually running