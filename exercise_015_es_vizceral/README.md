# How to demonstrate Enterprise Suite with Raspberry Pi Akka Cluster

## Exercise - Vizceral Demo

### Demo Setup Pre-requisite steps

1. Ensure you have passwordless ssh setup between your laptop and your Raspberry Pi Cluster. 
  2. Steps found [here:](https://github.com/lightbend/Pi-Akka-Cluster/blob/master/Hypriot-OS-Customisation-Instructions.md#configure-password-less-login-1)
2. ssh into each of the Rasperry Pi nodes in the cluster with user `akkapi`
> Hint 1: Use a multi-input terminal to make this easier. For example with a mac you can use iTerm2 [link to iTerm2](https://www.iterm2.com)
	 
### Demo Steps

1. From the project root assemble the jar using command:
  `sbt exercise_015_es_vizceral/assembly`
2. Copy the assembled jar to the raspberry pi's using the command: 	`CLUSTER_NR=0 ./copy 18`
3. start vizceral docker machine on your laptop using the command: 
  `docker-compose -f exercise_015_es_vizceral/docker-compose.yml up`
4. Since you'll be using the Akka Http client to send sudoku problems to the cluster, you'll need to start that up 
locally on your machine. Run exercise 16 locally by typing:
    `sbt exercise_013_add_cluster_client/run`
    > HINT: The project code may be configured to use port 8080 for the Akka Http server; you'll need to change this to something 
            different so it does not conflict with the default vizceral port. 
5. Start vizceral visualization exercise 18 on the pi cluster [*from within the terminal window logged into the pi node*] by typing:
    `CLUSTER_NR=0 ./run 18`
6. Send a sudoko problem to the cluster by typing in a new terminal [*from within the project root directory on your laptop*]:
    `./postSudoku 22` (you can choose any number between 1 and 77)
    > HINT: In case you had to change the Akka HTTP Port, ensure you also changed the script
    `postSudoku` to use that port. 
    
    > Post a few of these problems so that vizceral will have data inside of kibana
7. Check out your cool vizceral screens by browsing to `http://localhost:8080/`

#### troubleshooting
 
1. In case you are stuck on the loading screen, here you can troubleshoot
    * Open up kibana dashboard to check the metrics
        * Open the Kibana page at `http://localhost:5601`
        * First log in to Kibana. The default `user/password` is `elastic/changeme`: 
        * Before you can start exploring data in Kibana, you must set up an index. The *Index Patterns* page should be 
        the default page after logging in, when there are no indices already defined. You can navigate to this page by 
        selecting `Management` then `Kibana > Index Patterns`.
        * Enter `cinnamon-metrics-YYYY-MM` for the Index pattern, where `YYYY` is the current year and `MM` is the 
        current month.
	