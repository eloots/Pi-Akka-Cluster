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
- Two USB to 5V converters
- One TP-link TL-WR802N Travel Router with an ethernet cable and a USB to micro-USB cable.

Follow these steps to wire up your `Raspberry-Pi` based cluster hardware.

- Plug the micro-USB connectors of the 5-cable USB power cable set to the
  Raspberry-Pi power connectors and plug the USB connectors on the other side
  of the cables into the USB Power-hub.
- Connect one of the 5-port Ethernet switches to the lower three Pi-nodes
  using the `Red/Yellow/Orange` paired UTP Ethernet cable set. The order is
  not important.
- Connect the second switch to the top two Pi nodes using the `Green/Blue`
  paired UTP Ethernet cable set.
- Connect the two switches together with the `White` UTP Ethernet cable
- Connect the power jack on the switches to the Power-hub using the two USB
  to 9V converters.
- One of the switches has one free left available. You can use the `Black` UTP
  Ethernet cable to connect this port to the Ethernet port on your laptop.
  Depending on your laptop model, you may need to use a dongle/adapter to
  do this. Alternatively, you can connect to the cluster via Wifi via the 
  TP-Link travel router.
- On the Travel Router:
    - Connect the router to one of the USB-Power-hub ports.
    - Connect the router's Ethernet port to a free port on one of the 
      Ethernet switches.

- Make sure that your laptop is configure correctly (ssh, password-less 
  login to the Pi based nodes) and that you have loaded the software project
  in IntelliJ.
  
- Build this exercise in `sbt` using the `;clean;universal:packageBin` command

- Using the `copy` command, copy the fat-jar to the nodes:

```scala
➜  Pi-Akka-Cluster git:(master) ✗  ./copy 0
Copy exercise_000_initial_state-1.3.0.zip to node-0
exercise_000_initial_state-1.3.0.zip                                     100%   31MB  11.2MB/s   00:02
Unzipping archive
Copy exercise_000_initial_state-1.3.0.zip to node-1
exercise_000_initial_state-1.3.0.zip                                     100%   31MB  11.2MB/s   00:02
Unzipping archive
Copy exercise_000_initial_state-1.3.0.zip to node-2
exercise_000_initial_state-1.3.0.zip                                     100%   31MB  11.2MB/s   00:02
Unzipping archive
Copy exercise_000_initial_state-1.3.0.zip to node-3
exercise_000_initial_state-1.3.0.zip                                     100%   31MB  10.9MB/s   00:02
Unzipping archive
Copy exercise_000_initial_state-1.3.0.zip to node-4
exercise_000_initial_state-1.3.0.zip                                     100%   31MB  11.2MB/s   00:02
Unzipping archive
```

- Connect to the nodes and run the code by entering the `run` command 
  followed by the exercise number, 0 in this case:
  
```scala
$ ./run 0
Running exercise_000_initial_state on node-1
HypriotOS/armv7: akkapi@node-1 in ~
```