# Clustered Sudoku Solver - Add Akka Cluster Client & HTTP server

In this step, we add a HTTP server which will allow us to POST 
sudoku puzzlers to the clustered sudoku solver.

The server should be started on your local machine (so not on the
Raspberry Pi based Akka Cluster). The server is configured to 
listen on port `8080`.

Here's an example of posting a puzzle using `curl`:

`curl --header "Content-Type: application/json" --request POST --data '{ "values": [[0,6,0,0,2,0,9,0,7], [0,0,0,3,6,9,0,0,0], [0,0,1,0,4,0,0,0,0], [0,9,8,0,0,5,0,0,0], [0,2,0,6,0,0,3,0,0], [0,0,0,0,0,0,0,0,9], [0,0,2,8,0,0,0,0,1], [0,0,0,0,0,7,0,6,0], [0,5,0,0,0,0,0,0,0]]}' localhost:8080/sudoku`

Some 74 sudoku puzzler problem are available in the `sudokus` folder in
the "traditional" format as well as in JSON format.

These Sudoku problems can be sent to the cluster using the `postSudoku <N>`
command (with `N` between 1 and 74).

The sudoku solver will be adapted in the next exercise to serve
requests sent via the Akka Cluster Client

## Steps

1. Notice in the project folder, there's an additional configuration 
file as well as some changes to the
original file. Let's start with the original file. 
    
    Open up the file `src/main/resources/application.conf`
    
    Check out the additional configuration for both telemetry and 
    the cluster client:
    
```scala
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
    
2a. Change the IP address of zipkin to match your laptop's IP address. 

3. There's also a new file in the resources folder 
`src/main/resources/sudokuclient.conf`
    - This file is contains configuration for the Akka http server that
      will be running on your local laptop to connect to the pi-cluster.
    - Set `akka.remote.artery.canonical.hostname` to the IP address of
      your laptop's Ethernet interface.
    
4. Notice the old main `src/main/scala/org/neopixel/ClusterStatusTrackerMain`
   is completely commented out. This is because we will be using the file
   'AkkaHttpServer' as the main now. 

5. Run this project on your laptop
   
   - Assemble `sbt exercise_016_add_cluster_client/assembly`
               
   - Run      `java -jar exercise_016_add_cluster_client/target/scala-2.12/exercise_016_add_cluster_client-assembly-1.3.0.jar`
   
6. You can try to post a problem to the http server now
and notice the type of response you will see

    - Remember you can do this by trying to post to your 
      localhost while it's running by using:
    
```scala
curl --header "Content-Type: application/json" --request POST --data '{ "values": [[0,6,0,0,2,0,9,0,7], [0,0,0,3,6,9,0,0,0], [0,0,1,0,4,0,0,0,0], [0,9,8,0,0,5,0,0,0], [0,2,0,6,0,0,3,0,0], [0,0,0,0,0,0,0,0,9], [0,0,2,8,0,0,0,0,1], [0,0,0,0,0,7,0,6,0], [0,5,0,0,0,0,0,0,0]]}' localhost:8080/sudoku`
```

