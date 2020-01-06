# Split brain resolver (SBR) with Keep Majority strategy

# Introduction - SBR module set-up

This exercise is the first in a series of exercises on Akka Split Brain
Resolver. As Akka Split Brain resolver is a Lightbend commercial module,
you will have to have access to so-called commercial credentials and you 
will also have to enable using these credentials. Follow the instructions
to do this in the following paragraph.

# Setting up your credentials/enable commercial modules

If you're following a workshop, the commercial credentials will be provided 
to you by the instructor(s). They will be in a file named
`commercial.credentials` with 4 lines like this:

```scala
$ more ~/.lightbend/commercial.credentials
realm = Bintray
host = dl.bintray.com
user = deafdeaf-deaf-deaf-deaf-deafdeafdeaf@lightbend
password = decadeacdecadecadecadecadecadecadecadeca
```

Put the provided credentials file in a folder named `.lightbend` in your
home folder.

Next, enable the use of the commercial modules by running the `ecl` script
in the root folder of the repo:

```scala
$ ./ecl
[tmp 80ae104] Commit all user changes before applying patch
 2 files changed, 5 insertions(+), 1 deletion(-)
Applying: Enable utilisation of commercial licenses
```

Finally, if you have any sbt sessions running, reload the changes by running
the `reload` command from the sbt prompt.

# Introduction

The split brain resolver is added and configured with a `Keep majority` strategy.

With this strategy, the `SBR` will down all nodes in a partition if that
partition doesn't have a majority of the nodes that were part of the
cluster when it was last in a converged state.

`Discuss`

What are the implications with respect to cluster size when using this
strategy?

# Instructions

- Build & transfer the application
- Start up the application on all nodes and wait for the cluster to converge
- Partition the network in two by removing the white cable between the two
  switches
  - Observe what happens and try to explain it
- Disconnect one of the remaining `Up` nodes from the network
  - Observe what happens and try to explain it
- If there still are two nodes in the `Up` state, disconnect one of them
  from the switch it is connected to
  - Observe what happens and try to explain it

- Try your own scenarios - think about what other (network) failures can
  happen
  
- Reconnect everything back together, stop what is still running and
  start the full cluster from scratch and wait for convergence
- Use `Akka HTTP Management` to down nodes one by one, but give the `SBR`
  time to react (or not)
  - Observe what happens and try to explain it

# LED Legend

- LEDs 1 to 5 show the status of each node as seen by a node
    - Green:      Node is Up
    - Red:        Node is Down
    - Cyan:       Node in Leaving
    - Magenta:    Node is Exiting
    - White:      Node is Unreachable
    - Dark Green: Node is WeaklyUp - The LED is blinking

- LED number 6: Cyan when the node has a leader role
- LED number 7: Dark Blue when the node is running the Cluster Singleton actor
- LED number 8: Cluster liveliness indicator: when it blinks, we know
                that the cluster node software is actually running