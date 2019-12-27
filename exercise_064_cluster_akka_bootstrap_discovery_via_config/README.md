# Cluster Bootstrap / Discovery via configuration

This exercise demonstrates using Akka Management Cluster Bootstrap
to do discovery rather than using the seed node method. 

You will notice the configuration has changed in the `application.conf` file. 

There's a section that is used to describe the bootstrap setup which 
requires the service name, the discovery method. 

```
management.cluster.bootstrap.contact-point-discovery {
   service-name = "local-cluster"
   discovery-method = config
 }
```
For this exercise, we will be using the discovery via configuration method.

> note: There are other discovery methods, and you can even combine them if you'd like. 

For more information on akka management cluster bootstrap, 
please visit [Akka management cluster bootstrap documentation](https://developer.lightbend.com/docs/akka-management/current/bootstrap/index.html])

The configuration works similarly to the seed node setup with some advancements. 

Instead of adding seed nodes as in previous exercises, you define a service [or set of services] and add the contact points as needed for the selected discovery method.

Review the configuration of the services:

```
akka {
    discovery {
        config.services = {
          local-cluster = {
            endpoints = [
              {
                host = node-0
                port = 8558
              },
              {
                host = node-1
                port = 8558
              },
              {
                host = node-2
                port = 8558
              }
            ]
          }
        }
    }
}
```

In case you wanted to configure a number of different service, you'd add the 
list as follows. 
> note: this would allow your application to discover another 
akka cluster service if you had one setup, or even some external service. 

```
discovery {
    config.services = {
      local-cluster = {
        endpoints = [
          {
            host = node-0
            port = 8558
          }, ... 
        ]
      }, 
      service2 = {
          endpoints = []
        }
     
    }
}

```

Try the following steps:

1) Build the exercise by running `sbt exercise_050_.../assemble`

2) Copy the jar onto the Pi cluster nodes by running `./copy 50`

3) Start the application on `node-0` and `node-1`

- did the cluster form?

4) Start the application on `node-2`

- did you notice a difference between in the cluster formation now?

5) Start the remaining nodes, `node-3` and `node-4`

- how did these nodes know which cluster to join?
    
There are other configuration keys available for the cluster bootstrap settings. 
```
[service-namespace, protocol, exponential-backoff-max, resolve-timeout, effective-name, 
stable-margin, interval, required-contact-point-nr, port-name, exponential-backoff-random-factor]
```
> For more details on the other settings, see API documentation

  