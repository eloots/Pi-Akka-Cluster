# Disable Weakly-up feature

In this set-up, we disable the so-called `WeaklyUp` feature.

If the `WeaklyUp` feature is enabled (this is the default), new members 
who try to join an existing cluster, will move from the `Joining` state 
to the `WeaklyUp` state. `WeaklyUp` nodes can participate in offering
cluster services, but will not be counted in any decisions of determining
quorum (as performed by the Split Brain Resolver).

When, as in this example, the `WeaklyUp` feature is disabled, nodes will
stay in the `Joining` state until the cluster leader detects cluster state
convergence and moves these nodes to the (fully) `Up` state.

# Instructions

- Build & transfer the application
- Repeat the same steps as in the exercise about `WeaklyUp` members.

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