# The perils of auto-downing

`Don't use auto-downing` as it will lead to the formation of multiple clusters in case of, for example, a network partition.

Formation of multiple clusters will in most cases lead to data corruption

# Instructions

- Build the fat-jar for this exercise and transfer it using the `copy` command
- Start up the application on all nodes and wait until the cluster is in a 
  converged state (all nodes are `Up`)
  
- Partition the network by unplugging the `White` cable that connects the two
  networks. Now that two top nodes "live" in one network partition while the
  three bottom nodes live in another network partition
  
- Observe and try to explain what happens

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

