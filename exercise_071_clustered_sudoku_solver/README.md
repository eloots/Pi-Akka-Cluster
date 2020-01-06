# Clustered Sudoku Solver

In this step, we add an actor based Sudoku solver. Each node 
will automatically start a solver and another actor will send 
the same sudoku problem in a continuous loop.  

## Steps

1. Check out the new code in the file `ClusterStatusTrackerMain`

You'll see two new actors being created as part of the actor system.

```scala
val sudokuSolver = context.spawn(SudokuSolver(), "sudoku-solver")
context.spawn(SudokuProblemSender(sudokuSolver), "sudoku-problem-sender")
```

2. Run `sbt universal:packageBin` to create the packaged binaries.
3. Use the `copy` script to copy the binaries to each node in the cluster.
4. Run the example on the cluster by using the `run` command with the appropriate exercise number.
5. Observe sudoku solver in action.
