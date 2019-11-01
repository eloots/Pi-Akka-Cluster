# Cluster Singleton

In this exercise, we introduce a so-called `Cluster Singleton`. As the name
suggests, at anyone point in time, one and only one instance of a singleton
is running on a cluster.

In an upcoming exercise, we will see that if we're not careful, we may actually
end-up in a state where more than one instance of the singleton is running.
Obviously, this is faulty and we will see how we can avoid this from happening.

# Instructions

`  Part one`

- Build and copy this exercise to the cluster nodes
- Start the application on each node and observe the `Cluster Singleton`
  running on a node. In fact, the singleton will be always be running on
  the `oldest node`
  
Question: why is the `Cluster Singleton` running on the first seed node?

- Use Akka HTTP Management to make node-1, the first seed node, leave the
  cluster
- Observe what happens, specifically with the `Cluster Singleton`

`  Part two`

- Bring all the nodes down (simply press Ctrl-C in the terminal windows)
- Bring the cluster back-up

Now that we have a 5-node cluster up-and-running, let's run a real-life
failure scenario by bringing the first seed node down by crashing it

- Unplug the power cable of node-1
- The remaining nodes will mark node-1 as unreachable
- What happens to the `Cluster Singleton`?

Question: Of course, we can recover from this problem by restoring the
          power to the Pi and restarting the application. Instead,
          imagine that it is impossible to bring node-1 back up... Maybe
          it has a hardware problem that prevents it from booting.
          We can wait as long as we want, the `Cluster Singleton` won't
          come back...
          So, how, with the tools we have at our disposal, can we 
          recover from this failure?

# LED Legend

- LEDs 1 to 5 show the status of each node as seen by a node
    - Green:      Node is Up
    - Red:        Node is Down
    - Cyan:       Node in Leaving
    - Magenta:    Node is Exiting
    - White:      Node is Unreachable
    - Dark Green: Node is WeaklyUp - The LED is blinking

- LED number 6: Cyan when the node has a leader role
- `LED number 7: Dark Blue when the node is running the Cluster Singleton actor`
- LED number 8: Cluster liveliness indicator: when it blinks, we know
                that the cluster node software is actually running