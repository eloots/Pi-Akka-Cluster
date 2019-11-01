# Demonstrate Akka Cluster Weakly-Up members

When a node tries to join the cluster while one or more other nodes are already
up but unreachable, the leader will move the new node from `Joining` to `WeaklyUp`
instead of `Up`.

The joining of a cluster in the `WeaklyUp` state can be disabled by setting 
`akka.cluster.allow-weakly-up-members` to `off`

# Instructions

- Build and transfer the new version of the code to the nodes
- Start a cluster on node-0, node-1 and node-2
- Wait until the cluster is in a converged state before proceeding with
  the next step
- Start the application on the remaining nodes (3 & 4) and disconnect one
  of the first three nodes
- Observe and explain what happens
- Next, reconnect the unplugged node and observe what happens. `Be patient`

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