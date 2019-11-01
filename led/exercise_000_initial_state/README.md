# Initial State - Set-up the cluster hardware and verify it work correctly

Let's start by listing the components you'll be using for this:

- 5 x `Raspberry-Pi 3` nodes stacked in a small tower using either transparent or
  black separator plates
- Cables assemblies:
  - 1 x `Green/Blue` paired UTP Ethernet cable set
  - 1 x `Red/Yellow/Orange` paired UTP Ethernet cable set
  - 1 x `White` UTP Ethernet cable
  - 1 x `Black` UTP Ethernet cable
  - 1 x USB to micro-USB cable set with 5 cables
- One 10 port Anker USB Power-hub
- Two 5-port Ethernet switches
- Two USB to 9V converters

Follow these steps to wire up your `Raspberry-Pi` based cluster hardware.

- Plug the micro-USB connectors of the 5-cable USB power cable set to the
  Raspberry-Pi power connectors and plug the USB connectors on the other side
  of the cables into the USB Power-hub
- Connect one of the 5-port Ethernet switches to the lower three Pi-nodes
  using the `Red/Yellow/Orange` paired UTP Ethernet cable set. The order is
  not important
- Connect the second switch to the top two Pi nodes using the `Green/Blue`
  paired UTP Ethernet cable set
- Connect the two switches together with the `White` UTP Ethernet cable
- Connect the power jack on the switches to the Power-hub using the two USB
  to 9V converters
- One of the switches has one free left available. Use the `Black` UTP
  Ethernet cable to connect this port to the Ethernet port on your laptop.
  Depending on your laptop model, you may need to use a dongle/adapter to
  do this

- Make sure that your laptop is configure correctly (ssh, password-less 
  login to the Pi based nodes) and that you have loaded the software project
  in IntelliJ.
- Build this exercise in `sbt` using the `;clean;assembly` command
- Using the `copy` command, copy the fat-jar to the nodes:

```scala
➜  Pi-Akka-Cluster git:(update-exercise-man-e-pages) ✗ CLUSTER_NR=0 ./copy 0
Using exercise_000_initial_state/target/scala-2.12/exercise_000_initial_state-assembly-1.3.0.jar
Copy exercise_000_initial_state/target/scala-2.12/exercise_000_initial_state-assembly-1.3.0.jar to node-0
exercise_000_initial_state-assembly-1.3.0.jar                                                        100%   36MB  10.1MB/s   00:03
Copy exercise_000_initial_state/target/cinnamon-agent.jar to node-0
.
.
.
```

- Connect to the nodes and run the code by entering the `run` command 
  followed by the exercise number, 0 in this case:
  
```scala
HypriotOS/armv7: akkapi@node-1 in ~
$ ./run 0
Running exercise_000_initial_state-assembly-1.3.0.jar on node-1
[INFO] [06/08/2018 11:41:28.228] [CoreAgent] Cinnamon Agent version 2.8.6
HypriotOS/armv7: akkapi@node-1 in ~
$
```