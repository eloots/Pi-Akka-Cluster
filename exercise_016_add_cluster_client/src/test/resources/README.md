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

## Steps

1. Notice in the project folder, there's an additional configuration file as well as some changes to the
original file. Let's start with the original file. 
    
    Open up the file `src/main/resources/application.conf`
    
    Check out the additional configuration for both telemetry and 
    the cluster client:
    
    ```
      akka.actors {
        "/user/*" {
          report-by = instance
          traceable = on
          excludes = ["akka.http.*", "akka.stream.*"]
        }
      }
    
      akka.http {
        servers {
          "*:*" {
            paths {
              "*" {
                metrics = on
                traceable = on
              }
            }
          }
        }
    
        clients {
          "*:*" {
            paths {
              "*" {
                metrics = on
                traceable = on
              }
            }
          }
        }
      }
    
      opentracing {
        # Const sampler trace only for demo purposes -> never in production!
        tracer {
          sampler = const-sampler
          const-sampler {
            decision = true
          }
        }
    
        akka.trace-system-messages = yes
    
        zipkin {
          url-connection {
            # FIXME : THE IP NEEDS TO POINT TO THE MACHINE WHERE ZIPKIN IS RUNNING
            endpoint = "http://192.168.0.28:9411/api/v1/spans"
          }
        }
      }
    ```
3. There's also a new file in the resources folder 
`src/main/resources/sudokuclient.conf`
    - This file is contains configuration for the Akka http server that will be running on your local laptop to connect 
    to the pi-cluster. 
    
4. The hostname information in the artery configuration 
    needs to be changed. 
    
    ```
    # TODO the hostname should be changed to the 
    hostname of your laptop
    canonical {
            hostname = "192.168.0.28"
            port = 4000
    }
    ```
5. Notice the old main `src/main/scala/org/neopixel/ClusterStatusTrackerMain` is completely commented out. 
This is because we will be using the file 'AkkaHttpServer' as the main now. 
Look at the Akka Http Server file and make the following changes:

    *TODO in case we have time for students to modify the file*
5. Assemble and run this file on your laptop
   
   - Run `sbt assemble`
   - alternately, you can simply run on your machine with 
   `sbt run`
6. You can try to post a problem to the http server now
and notice the type of response you will see

    Remember you can do this by trying to post to your 
    localhost while it's running by using 
    ```
    curl --header "Content-Type: application/json" --request POST --data '{ "values": [[0,6,0,0,2,0,9,0,7], [0,0,0,3,6,9,0,0,0], [0,0,1,0,4,0,0,0,0], [0,9,8,0,0,5,0,0,0], [0,2,0,6,0,0,3,0,0], [0,0,0,0,0,0,0,0,9], [0,0,2,8,0,0,0,0,1], [0,0,0,0,0,7,0,6,0], [0,5,0,0,0,0,0,0,0]]}' localhost:8080/sudoku`
    ```