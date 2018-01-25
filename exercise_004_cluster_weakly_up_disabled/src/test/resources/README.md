cluster_LED_status_viewer_improved

# Exercise 2 > Basic cluster status tracking improved

Indication of weakly-up members using fast blinking LED.

When a node tries to join the cluster while on or more other nodes are already up but unreachable, the leader will not move the new node from JOINING to WEAKLY-UP instead of UP.

The joining of a cluster in the WEAKLY-UP state can be disabled by setting `akka.cluster.allow-weakly-up-members=off`

Green:      Node is UP
Red:        Node is Down
Cyan:       Node in Leaving
Magenta:    Node is Exiting
White:      Node is Unreachable
Dark Green: Node is Weakly-Up - The LED is blinking at a high frequency

LED number 6 indicates is Cyan when the node has a leader role
LED number 7 is unused
LED number 8 is the cluster liveliness indicator: when it blinks, we now that the cluster node software is actually running.