split_brain_resolver_keep_majority

# Split brain resolver (SBR) with Keep Majority strategy

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