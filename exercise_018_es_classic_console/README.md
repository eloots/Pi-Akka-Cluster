# How to demonstrate Enterprise Suite Classic Console with Raspberry Pi Akka Cluster

## Exercise - ES Classic Demo

### Demo Setup Pre-requisite steps

1. Ensure you have passwordless ssh setup between your laptop and your Raspberry Pi Cluster. 
	2. Steps found [here:](https://github.com/lightbend/Pi-Akka-Cluster/blob/master/Hypriot-OS-Customisation-Instructions.md#configure-password-less-login-1)
2. ssh into each of the Rasperry Pi nodes in the cluster with user `akkapi`
> Hint 1: Use a multi-input terminal to make this easier. For example with a mac you can use iTerm2 [link to iTerm2](https://www.iterm2.com)
 
### Demo Steps
1. From the project root assemble the jar using command:
	`sbt exercise_017_es_classic_console/assembly`
2. Copy the assembled jar to the raspberry pi's using the command: 	`CLUSTER_NR=0 ./copy 18`
3. start vizceral docker machine on your laptop using the command: 
	`docker-compose -f exercise_018_es_vizceral/docker-compose.yml up`
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
