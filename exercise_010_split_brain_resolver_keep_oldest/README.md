# Split brain resolver with Keep Oldest strategy

> NOTE: If you skipped the first exercise in the Split Brain Resolver exercise
> series (`Keep majority` strategy), read the paragraphs titled
> `Introduction - SBR module set-up` and
> `Setting up your credentials/enable commercial modules` in that first exercise
> before continuing with this one

The split brain resolver is added and configured with a `Keep Oldest` strategy.

# Instructions

- Build & transfer the application
- Start up the application on all nodes and wait for the cluster to converge
- Partition the network in two by removing the white cable between the two
  switches
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