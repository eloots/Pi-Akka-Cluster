# Clustered Sudoku Solver

In this step, we add an actor based Sudoku solver. Each node 
will automatically start a solver and another actor will send 
the same sudoku problem in a continuous loop.  

## Steps

1. Check out the new code in the file `ClusterStatusTrackerMain`
   
   You'll see two new actors being created as part of the actor system.
   
   ```scala
     val sudokuSolver = system.actorOf(SudokuSolver.props(), "sudoku-solver")
   
     val sudokuProblemSender = system.actorOf(SudokuProblemSender.props(sudokuSolver), "sudoku-problem-sender")
    ```
   
2. There's also an additional configuration file as well as some changes to the
original file. Have a look at the file `src/main/resources/application.conf`
    
    Notice the additional configuration for both telemetry and 
    the cluster client.
    
3. Run `sbt universal:packageBin` to create the packaged binaries.
4. Use the `copy` script to copy the binaries to each node in the cluster.
5. Run the example on the cluster by using the `run` command with the appropriate exercise number.
6. Observe sudoku solver in action.