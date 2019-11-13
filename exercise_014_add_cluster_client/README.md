# Clustered Sudoku Solver - Add Akka Cluster Client & HTTP server

In this step, we add a HTTP server which will allow us to POST 
sudoku puzzlers to the clustered sudoku solver.

The server should be started on your local machine (so not on the
Raspberry Pi based Akka Cluster). The server is configured to 
listen on port `8080`.

Here's an example of posting a puzzle using `curl`:

`curl --header "Content-Type: application/json" --request POST --data '{ "values": [[0,6,0,0,2,0,9,0,7], [0,0,0,3,6,9,0,0,0], [0,0,1,0,4,0,0,0,0], [0,9,8,0,0,5,0,0,0], [0,2,0,6,0,0,3,0,0], [0,0,0,0,0,0,0,0,9], [0,0,2,8,0,0,0,0,1], [0,0,0,0,0,7,0,6,0], [0,5,0,0,0,0,0,0,0]]}' localhost:8084/sudoku`

Some 74 sudoku puzzler problems are available in the `sudokus` folder in
the "traditional" format as well as in JSON format.

These Sudoku problems can be sent to the cluster using the `postSudoku <N>`
command (with `N` between 1 and 74).

The sudoku solver will be adapted in the next exercise to serve
requests sent via the Akka Cluster Client

## Steps

1. Notice in the project folder, there's an additional configuration 
   file as well as some changes to the original file. Let's start with
   the original file. 
    
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
        endpoint = "http://192.168.200.100:9411/api/v1/spans"
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
   
    - Run `sbt universal:packageBin` to create the packaged binaries.
    - To start up the HTTP server, do the following steps:
        - cd into the exercise folder directory.
        - cd into the `target/universal` subfolder.
        - unzip the zip file present in the folder.
        - cd into the folder created with the unzip command.
        - start the HTTP server by executing the start-up script in the `bin` subfolder.
    - Following is a recording of the steps described above (note that the exercise number you're seeing may differ from the one shown below):

```scala
➜  Pi-Akka-Cluster git:(master) ✗ pwd
/lbt/Pi-Akka-Cluster

➜  Pi-Akka-Cluster git:(master) ✗ cd exercise_014_add_cluster_client

➜  Pi-Akka-Cluster git:(master) ✗ cd target/universal

➜  Pi-Akka-Cluster git:(master) ✗ ls
exercise_014_add_cluster_client-1.3.0.zip tmp
scripts

➜  Pi-Akka-Cluster git:(master) ✗ unzip
exercise_014_add_cluster_client-1.3.0.zip
Archive:  exercise_014_add_cluster_client-1.3.0.zip
  inflating: exercise_014_add_cluster_client-1.3.0/conf/application.ini
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.lightbend.training.exercise_014_add_cluster_client-1.3.0.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.lightbend.training.common-1.3.0.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/org.scala-lang.modules.scala-parser-combinators_2.13-1.1.2.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.lightbend.akka.management.akka-management-cluster-bootstrap_2.13-1.0.3.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.akka.akka-remote_2.13-2.5.26.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.lightbend.akka.management.akka-management-cluster-http_2.13-1.0.3.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.akka.akka-persistence_2.13-2.5.26.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/org.ow2.asm.asm-5.0.3.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/ch.qos.logback.logback-classic-1.2.3.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/org.slf4j.slf4j-api-1.7.25.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.akka.akka-slf4j_2.13-2.5.26.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/org.ow2.asm.asm-tree-5.0.3.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.github.jnr.jnr-constants-0.9.9.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/io.spray.spray-json_2.13-1.3.5.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/org.ow2.asm.asm-commons-5.0.3.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.akka.akka-stream_2.13-2.5.26.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.github.jnr.jffi-1.2.16.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/org.scala-lang.scala-library-2.13.1.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/org.agrona.agrona-0.9.31.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.lightbend.akka.management.akka-management_2.13-1.0.3.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.akka.akka-distributed-data_2.13-2.5.26.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.akka.akka-cluster_2.13-2.5.26.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.github.jnr.jffi-1.2.16-native.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/org.ow2.asm.asm-analysis-5.0.3.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.akka.akka-http-core_2.13-10.1.10.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.akka.akka-parsing_2.13-10.1.10.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/io.aeron.aeron-client-1.15.1.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.akka.akka-coordination_2.13-2.5.26.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/org.lmdbjava.lmdbjava-0.6.1.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.akka.akka-http_2.13-10.1.10.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/org.reactivestreams.reactive-streams-1.0.2.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.github.jnr.jnr-ffi-2.1.7.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.akka.akka-actor_2.13-2.5.26.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.akka.akka-protobuf_2.13-2.5.26.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/org.scala-lang.modules.scala-java8-compat_2.13-0.9.0.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/org.ow2.asm.asm-util-5.0.3.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/io.netty.netty-3.10.6.Final.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.github.jnr.jnr-x86asm-1.0.2.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.akka.akka-cluster-sharding_2.13-2.5.26.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.config-1.3.3.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.ssl-config-core_2.13-0.3.8.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.akka.akka-http-spray-json_2.13-10.1.10.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/ch.qos.logback.logback-core-1.2.3.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.akka.akka-discovery_2.13-2.5.26.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/io.aeron.aeron-driver-1.15.1.jar
  inflating: exercise_014_add_cluster_client-1.3.0/lib/com.typesafe.akka.akka-cluster-tools_2.13-2.5.26.jar
  inflating: exercise_014_add_cluster_client-1.3.0/bin/exercise_014_add_cluster_client
  inflating: exercise_014_add_cluster_client-1.3.0/bin/exercise_014_add_cluster_client.bat
  inflating: exercise_014_add_cluster_client-1.3.0/lib/librpi_ws281x.so
  inflating: exercise_014_add_cluster_client-1.3.0/sudokus/001.sudoku

➜  Pi-Akka-Cluster git:(master) ✗ cd exercise_014_add_cluster_client-1.3.0

➜  Pi-Akka-Cluster git:(master) ✗ bin/exercise_014_add_cluster_client
Server online at http://localhost:8084/
Press RETURN to stop...
```
   
6. You can try to post a problem to the http server now
and notice the type of response you will see

    - Remember you can do this by trying to post to your 
      localhost while it's running by using:
    
```scala
curl --header "Content-Type: application/json" --request POST --data '{ "values": [[0,6,0,0,2,0,9,0,7], [0,0,0,3,6,9,0,0,0], [0,0,1,0,4,0,0,0,0], [0,9,8,0,0,5,0,0,0], [0,2,0,6,0,0,3,0,0], [0,0,0,0,0,0,0,0,9], [0,0,2,8,0,0,0,0,1], [0,0,0,0,0,7,0,6,0], [0,5,0,0,0,0,0,0,0]]}' localhost:8084/sudoku`
```

