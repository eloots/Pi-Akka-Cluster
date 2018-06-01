cluster_base

# Set-up LED cluster status

In this set-up, we show the status of the cluster nodes on an 8-LED strip.

- The first 5 LED's (counting from the left) show the status of node-0
  through node-4 as seen from the node on which the LED-strip is connected.

  The following color codes are used:

    - Green:      Node is UP
    - Red:        Node is Down
    - Cyan:       Node in Leaving
    - Magenta:    Node is Exiting
    - White:      Node is Unreachable
    - Dark Green: Node is Weakly-Up

- LED number 6 indicates is Cyan when the node has a leader role
- LED number 7 is unused (but it will be in the next exercise)
- LED number 8 is the cluster liveliness indicator: when it blinks, we know
  that the cluster node software is actually running on that node
  
# Background

`Akka Cluster` provides an API that allows one to subscribe to so-called
`cluster events`. For example, when a new node is being moved from its initial
state, where it isn't yet part of the cluster, to a `Joining` state, all nodes
subscribed to the cluster events will receive a `MemberJoined` event that
contains the identity of the joining member. The `ClusterStatusTracker`
module reflects these events into updates of the LED status indicator.


In doing so, the LED status indicator reflects the state of all cluster nodes
as seen from the node to which the LED status indicator is connected.