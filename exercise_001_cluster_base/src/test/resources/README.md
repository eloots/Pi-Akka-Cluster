cluster_LED_status_viewer

# Exercise 1 > Set-up LED cluster status

In this set-up, we show the status of the cluster nodes on an 8-LED strip.

The first 5 LED's (counting from the left) show the status of node-0 through node-4 as seen from the node on which the LED-strip is connected.

The following color codes are used:

Green:      Node is UP
Red:        Node is Down
Cyan:       Node in Leaving
Magenta:    Node is Exiting
White:      Node is Unreachable
Dark Green: Node is Weakly-Up

LED number 6 indicates is Cyan when the node has a leader role
LED number 7 is unused
LED number 8 is the cluster liveliness indicator: when it blinks, we now that the cluster node software is actually running.