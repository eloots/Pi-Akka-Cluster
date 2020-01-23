# Clustered Sudoku Solver

In this step, we add an actor based Sudoku solver. A solver is started on each
node and an Akka Group Router is added to distribute the processing of problem
across all available solvers. The Router is configured using a round-robin
routing strategy (we override the default one, which is random routing strategy
 - feel free to experiment with that one).

An Akka Group Router is cluster-aware: the router will automatically start
routing messages to new routees (Sudoku solvers in this case), or stop routing
to routees when these can no longer be accessed (because, for example, of
network issues) or are stopped.

For demonstration purposes, we Sudoku problem generator is added to the set-up,
and this generator is running as an Akka Cluster Singleton. In practical use
cases, the problems will most probably be coming from external sources (like a
Sudoku Solver Client). Running the problem generator allows us to show how the
system behaves in certain scenarios (like performing a coordinated shutdown of
the node on which the singleton is running, or when it is stop by a Split Brain
Resolver in case of a partitioned cluster).

## Steps

- Check out the new code in the file `ClusterStatusTrackerMain`

You'll see that 2 additional actors are created: a `SudokuSolver` and a
`SudokuProblemGenerator`. Also, a group router is created.

2. Run `sbt universal:packageBin` to create the packaged binaries.
3. Use the `copy` script to copy the binaries to each node in the cluster.
4. Run the example on the cluster by using the `run` command with the
   appropriate exercise number.
5. The problem sender send problems at a fixed rate which can be changed
   with the `sudoku-solver.problem-sender.send-interval` settings in the
   `sudokusolver.conf` configuration file.
6. Observe sudoku solver in action.

You will see that the two remaining LEDs (on the 10-LED strip) are put to use:

- LED #9 blinks for ±15ms each time the Sudoku solver receives a new Sudoku problem. It can blink using one of three colors, each with a specific meaning:
    - Green when sudoku problem can be handled immediately.
    - Blue when problem has to be stashed because solver is busy.
    - Red when problem is dropped due to stash buffer full.
- LED #10 is lit (color is always Green) when the Suduko solver on the node is actively solving a Sudoku. The rate at which Sudoku problems are sent is such that a single node can't keep up with the pace. So, if there's only a single node in the cluster, eventually the stash buffer (200 items) will fill up and requests will be dropped.

Many more interesting tests can be done:

`NOTE:` Depending on the type of nodes in your cluster (Raspberry 3B, 3B+, 4B),
        the speed at which sudoku problems are solved can differ a lot (2 to 3x).

- Start up all nodes and wait until a steady state is reached (LEDs 9 & 10
  should blink Green in cadence). Next, partition the network and observe
  what happens.
- Restore the network and observe what happens.
- Change the rate at which sudoku problems are sent. Setting it to a lower
  rate allows you to better observe certain events.
- You can also configure a Split Brain Resolver on this exercise and repeat
  the exercise. You should be able to explain what you observe.