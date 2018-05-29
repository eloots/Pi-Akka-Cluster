clustered_sudoku_solver_add_http_client

# Clustered Sudoku Solver - Add Akka Cluster Client & HTTP server

In this step, we add a HTTP server which will allow us to POST
sudoko puzzlers to the clustered sudoku solver.

The server should be started on your local machine (so not on the
Raspberry Pi based Akka Cluster). The server is configured to 
listen on port `8080`.

Here's an example of posting a puzzle using `curl`:

`curl --header "Content-Type: application/json" --request POST --data '{ "values": [[0,6,0,0,2,0,9,0,7], [0,0,0,3,6,9,0,0,0], [0,0,1,0,4,0,0,0,0], [0,9,8,0,0,5,0,0,0], [0,2,0,6,0,0,3,0,0], [0,0,0,0,0,0,0,0,9], [0,0,2,8,0,0,0,0,1], [0,0,0,0,0,7,0,6,0], [0,5,0,0,0,0,0,0,0]]}' localhost:8080/sudoku`

The sudoku solver will be adapted in the next exercise to serve
requests sent via the Akka Cluster Client

