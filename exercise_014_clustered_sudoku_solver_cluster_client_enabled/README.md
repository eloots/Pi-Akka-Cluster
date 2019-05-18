# Clustered Sudoku Solver - Cluster Client Enabled

In the previous exercise, we created an Akka HTTP server 
that takes sudoku problems and sends them to the pi cluster to solve. 

We only setup the Akka HTTP side and you noticed when you 
posted a problem to the server, there was an error. 

In order to fix this, we must enable to akka cluster to accept messages from cluster clients. 

## Steps

1. Open up the file `src/main/scala/org/neopixel/ClusterStatusTrackerMain.scala` and see if you notice anything new...
    
   Of course you notice something new! 
   1. The actor that previously sent messages to the cluster is no longer there. Instead of relying 
   on that actor, we are going to be receiving the problems from the 
   Akka http application. In this project we only need the sudoku solver running which you will still see there - 

        `val sudokuSolver = system.actorOf(SudokuSolver.props(), "sudoku-solver")`
2. We need to now enable to cluster to accept messages from external clients. We do this by enabling the client receptionist. 

   `ClusterClientReceptionist(system).registerService(sudokuSolver)`
   
   The `ClusterClientReceptionist` provides methods for registration of actors that should be reachable from the client. Messages are wrapped in `ClusterClient.Send`, `ClusterClient.SendToAll` or `ClusterClient.Publish`.
3. Run `sbt assembly` to create the assembly file 
4. Copy the file to each node in the cluster. Use the script `copy` with exercise number 17. (`./copy 17`)
5. Run the example on the cluster by using the `run` command with exercise number 17. (`./run 17`)
6. Now try posting a sudoku problem to your Akka HTTP server. 
7. Observe sudoku solver in action!