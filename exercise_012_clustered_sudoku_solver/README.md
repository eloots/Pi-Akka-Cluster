# Clustered Sudoku Solver

In this step, we add an actor based Sudoku solver. Each node 
will automatically start a solver and another actor will send 
the same sudoku problem in a continues loop.  

## Steps

1. Check out the new code in the file `ClusterStatusTrackerMain`
   
   You'll see two new actors being created as part of the actor system.
   
   ```scala
     val sudokuSolver = system.actorOf(SudokuSolver.props(), "sudoku-solver")
   
     val sudokuProblemSender = system.actorOf(SudokuProblemSender.props(sudokuSolver), "sudoku-problem-sender")
    ```
   
2. There's also an additional configuration file as well as some changes to the
original file. Let's start with the original file. 
    
    Open up the file `src/main/resources/application.conf`
    
    Notice the additional configuration for both telemetry and 
    the cluster client. 
    
3. Run `sbt assembly` to create the assembly file 
4. Copy the file to each node in the cluster. Use the script `copy` with exercise number 15. (`./copy 15`)
5. Run the example on the cluster by using the `run` command with exercise number 15. (`./run 15`)
6. Observe sudoku solver in action!