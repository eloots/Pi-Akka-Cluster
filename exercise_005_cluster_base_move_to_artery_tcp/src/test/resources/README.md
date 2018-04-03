cluster_base

# Switch remoting transport from default to Artery-TCP

Akka 2.5.11 introduced an new remoting transport named Artery TCP.

Artery TCP is built on top of Akka Streams and provides an alternative
for the existing TCP based transport.

Apart from giving lower latency and higher throughput, Artery TCP
also utilises separate TCP connections for user- and system traffic.
This is important as it prevents user traffic from 'pushing out'
system messages which in turn may lead to pushing out Akka Cluster
gossip messages.

It is expected that Artery-TCP will become the default remoting
transport protocol in a future Akka release.

In this example, we take the code from the previous exercise and
switch the transport to Artery-TCP
