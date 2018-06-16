base_clustered_sudoku_solver

# Clustered Sudoku Solver

In this step, we add an actor based Sudoku solver. Each node 
will automatically start a solver and another actor will send 
the same sudoku problem in a continues loop. The sender actor will send 

## Steps 
1. Check out the new code in the file `ClusterStatusTrackerMain`
   
   You'll see two new actors being created as part of the actor system.
   
2. There's also an additional configuration file as well as some changes to the
original file. Let's start with the original file. 
    
    Open up the file `src/main/resources/application.conf`
    
    Notice the additional configuration for both telemetry and 
    the cluster client