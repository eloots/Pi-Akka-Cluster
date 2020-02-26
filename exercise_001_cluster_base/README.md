# Explore base Akka Cluster formation

In this exercise, we will explore the formation of an Akka Cluster with up-to
5 nodes.

# Instructions

- Build the fat-jar for this exercise and transfer it to all the nodes using
  the `copy` command
- Run different scenarios and observe what happens
- Here's a sample scenario:
    - Start-up nodes individually
        - Start up `node-0` and wait until the heart-beat LED starts blinking
        - Wait for some time - what happens (or doesn't happen)
        - Start up `node-1`
        - Observe...
        - Start up the remaining nodes  
    - Hit `Ctrl-C` in `node-1`'s terminal
    - Observe...
    - Hit `Ctrl-C` in `node-0`'s terminal
    - Observe...
    - Hit `Ctrl-C` in remaining node terminal sessions  

# LED Legend

- LEDs 1 to 5 show the status of each node as seen by a node
    - Green:      Node is Up
    - Red:        Node is Down
    - Cyan:       Node in Leaving
    - Magenta:    Node is Exiting
    - White:      Node is Unreachable
    - Dark Green: Node is WeaklyUp - The LED is blinking

- LED number 6: Cyan when the node has a leader role
- LED number 7: Not used in this exercise
- LED number 8: Cluster liveliness indicator: when it blinks, we know
                that the cluster node software is actually running
                It blinks in red when the membership state of the cluster has not yet converged
                It flashes 3 times in green when the membership state just converged
                It blinks in white when the cluster is operating normally

Note that in this exercise, the cluster gossip interval (and other parameters
related to it) are slowed down on purpose in order to be able to get more time
to see what is happening - normally, things move along faster. This is 
configured in `src/main/application.conf`.
