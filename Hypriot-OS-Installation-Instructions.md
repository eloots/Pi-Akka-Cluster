## Pi-Hypriot OS installation instructions

> ***NOTE: AFTER INITIAL POWER-UP OF THE NODES, AN INSTALLATION SCRIPT IS RUN WHICH WILL INSTALL THE OPENJDK AND A NUMBER OF PACKAGES. IT IS RECOMMENDED TO PLUGH AN LED STRIP ON THE GPIO CONNECTOR AS IT WILL DISPLAY THE PROGRESS OF THE INSTALLATION (ALL LEDS TURN GREEN WHEN THE INSTALLATION IS COMPLETED)***


On your Mac, add the following entries to your _/etc/hosts_ file:

```
# Cluster #0
192.168.200.10 node-0
192.168.200.11 node-1
192.168.200.12 node-2
192.168.200.13 node-3
192.168.200.14 node-4
```

> With support for `cloud-init` being present in the [Hypriot OS](http://blog.hypriot.com) distribution, we switch to using this tool instead starting from a standard installation and then going through a lengthy manual install.

The installation is now relatively simple:

Download and install the Hypriot _**flash**_ tool by following the instructions in the **Installation** section [here](https://github.com/hypriot/flash#installation).

Using the `akka-pi-os.yml` file in this repo, a 16GB micro SD card and a flash card reader/writer, you are now set to flash an SD card. You will probably customise at least one parameter in the `.yml`: the IP-address of the node (current value is _192.168.200.10_).

> NOTE: Edit `akka-pi-os.yml` to change line: `static ip_address=192.168.200.10/24` for each node.

The command to flash the card is:

```
Pi-Akka-Cluster git:(master) ✗ flash -n node-0 -u akka-pi-os.yml https://github.com/hypriot/image-builder-rpi/releases/download/v1.10.0/hypriotos-rpi-v1.10.0.img.zip
```

`-n node-0` specifies the host name for the node. For other nodes, set it to a name of the host this card is destined for (`node-0` through `node-4`)

We're also using the latest version of Hypriot (1.10.0 at the time of writing). For a complete list of available versions, see the [downloads page](https://blog.hypriot.com/downloads/).

Next:

- Insert the card into the micro SD slot on your Raspberry Pi (should work on Raspberry Pi 2 and 3 - Model B and B+).
- Power-up the Pi.
- Be patient... Installation progress is indicated on the LED strip (provided one is plugged into the GPIO connector).
- After about 1.5 minute, you can log-in on the Raspberry Pi. Log-in as user _akkapi_ using ssh (password is also _akkapi_).
- It takes between 4 and 6 minutes to complete the initial installation (depending on the speed of the micro SD card).

> Note: During the first boot, a number of things happen:
>
> - Resizing of file system to utilise the full capacity of the flashed micro SD card.
> - Install a number of packages (see `.yaml` file for details.
> - Install Java 8 (OpenJDK).
> 
> Check `/var/log/cloud-init-output.log` for progress.
> 
> The process is finished when the following line is logged:

```
Cloud-init v. 0.7.9 finished at Sun, 12 May 2019 14:06:46 +0000. Datasource 
DataSourceNoCloud [seed=/var/lib/cloud/seed/nocloud-net][dsmode=net].  
Up 277.31 seconds
```

#### Repeat process for nodes 1 through 4

The installation of the OS as described in the previous section should be repeated for the remaining nodes. Make sure to pass the correct hostname (`node-1` ... `node-4`) to the `flash` script and to set the corresponding IP address in the `akka-pi-os.yml` file.

### Configure password-less login

Next, we set-up password-less login. We assume that we want to log in to account _akkapi_ on a pi from an account (_userxxx_) on your laptop. In order to set-up password-less login, you need a so-called public/private key pair. If you're unfamiliar with this concept, or don't have such a key pair, first read the paragraph titled _Generating a public/private key pair_ in the Addendum.

Now, proceed by create a `.ssh` folder on the _akkapi_'s home folder on the Pi and copy the public key (`id_rsa.pub`) to a file named `authorized_keys` in the `.ssh` folder. You will have to supply the password for the _akkapi_ account for the two commands launched. On your laptop:

```
[Pi-Akka-Cluster git:(master) ✗ for node in 0 1 2 3 4;do ssh akkapi@node-${node} mkdir .ssh; done
akkapi@node-0's password:
```

Copy the contents of your *public key* to the `authorized_keys` file:

```
Pi-Akka-Cluster git:(master) ✗ for node in 0 1 2 3 4;do cat ~/.ssh/id_rsa.pub | ssh akkapi@node-${node} 'cat >> .ssh/authorized_keys'; done
akkapi@node-0's password:
```

With this, you should now be able to log into the _akkapi_ on all nodes from the _userxxx_ account on your laptop without having to enter a password.

### Run `updateScripts` command

A number of scripts have to be installed on each node. Running `updateScripts` on your laptop your laptop will perform this task:

```
Pi-Akka-Cluster git:(master) ✗ ./updatePiScripts
Copy  to node-0
run                                                                                   100% 1037   273.9KB/s   00:00
librpi_ws281x.so                                                                      100%   92KB   2.4MB/s   00:00
setLedType
Copy  to node-1
run                                                                                   100% 1037   273.9KB/s   00:00
librpi_ws281x.so                                                                      100%   92KB   2.4MB/s   00:00
setLedType
Copy  to node-2
run                                                                                   100% 1037   273.9KB/s   00:00
librpi_ws281x.so                                                                      100%   92KB   2.4MB/s   00:00
setLedType
Copy  to node-3
run                                                                                   100% 1037   273.9KB/s   00:00
librpi_ws281x.so                                                                      100%   92KB   2.4MB/s   00:00
setLedType
Copy  to node-4
run                                                                                   100% 1037   273.9KB/s   00:00
librpi_ws281x.so                                                                      100%   92KB   2.4MB/s   00:00
setLedType
```

> Note: the `updateScripts` command can take a single argument: a node number (0-4). Passing a node number as argument will update the script only on the node with that number.

### Finish the installation

In this final step, we customise the login settings for the `akkapi` account on each node by running the following command from your laptop:

```
Pi-Akka-Cluster git:(master) ✗ for node in 0 1 2 3 4;do ssh akkapi@node-${node} 'finish-install';done
```

### [Optional] Install Kubernetes

Run the `install-kubernetes` command on each node.

## Addendum

### Driving a NeoPixel 8-LED strip

This section explains how to build the LED driver shared library on a Pi.

On a Raspberry-Pi, install build-tools (gcc, ...), _swig_ and _scons_:

```
sudo apt install build-essential
sudo apt-get install swig
sudo apt-get install scons
```

__Download and build software__ (see instructions in README.md in the repo) for driving NeoPixels: [rpi_ws281x](https://github.com/jgarff/rpi_ws281x)

Install JDK 8 (if not already installed):

```
$ sudo apt-get install oracle-java8-jdk
$ sudo apt-get update
```

Set `JAVA_HOME`

```
$ sudo update-alternatives --config java
There is 1 choice for the alternative java (providing /usr/bin/java).

  Selection    Path                                     Priority   Status
------------------------------------------------------------
  0            /usr/lib/jvm/java-8-oracle/jre/bin/java   1081      auto mode
* 1            /usr/lib/jvm/java-8-oracle/jre/bin/java   1081      manual mode
```

This tells us that `JAVA_HOME` is `/usr/lib/jvm/java-8-oracle`

```
$ export JAVA_HOME=/usr/lib/jvm/java-8-oracle
$ git clone git@github.com:jgarff/rpi_ws281x.git
$ cd rpi_ws281x
$ rm -rf *.o
$ scons
$ mkdir java
$ cp python/*.i java
$ cd java
$ swig -package neopixel -java rpi_ws281x.i
$ gcc -c *.c -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux
$ gcc -shared -o librpi_ws281x.so ../*.o rpi_ws281x_wrap.o
$ ls -l librpi_ws281x.so
-rwxr-xr-x 1 akkapi akkapi 94444 Aug 15 13:18 librpi_ws281x.so
```

Running sample application `exercise_000_initial_state`:

```
$ ./run 0
```

## Running base cluster with status LED indicators

> Note: for convenience, set-up password-less ssh so that you can log-in to the PIs without having to enter a password

On your laptop, run sbt and build an uber-jar if not done already:

```
$ sbt
  <... elided>

man [e] > mini-cluster > split_brain_resolver_static_quorum > projects
[info] In file:/Users/ericloots/Trainingen/LightbendTraining/Pi-Akka-Cluster/
[info] 	   common
[info] 	   exercise_000_initial_state
[info] 	   exercise_001_UDP_experiments
[info] 	   exercise_002_split_off_LED_status_monitor
[info] 	   exercise_003_cluster_state_monitor_improved
[info] 	 * exercise_004_cluster_base
[info] 	   exercise_005_cluster_weakly_up
[info] 	   exercise_006_cluster_cluster_singleton
[info] 	   exercise_007_cluster_the_perils_of_auto_downing
[info] 	   exercise_008_cluster_weakly_up_disabled
[info] 	   exercise_009_split_brain_resolver_keep_majority
[info] 	   exercise_010_split_brain_resolver_static_quorum
[info] 	   exercise_011_split_brain_resolver_keep_referee
[info] 	   exercise_012_split_brain_resolver_keep_oldest
[info] 	   exercise_013_split_brain_resolver_static_quorum_http_mamagement
[info] 	   pi_cluster_master
man [e] > mini-cluster > split_brain_resolver_static_quorum > project exercise_004_cluster_base
[info] Set current project to exercise_004_cluster_base (in build file:/Users/ericloots/Trainingen/LightbendTraining/Pi-Akka-Cluster/)
man [e] > mini-cluster > cluster_base > ;clean;assembly

<... elided>

[info] Packaging /Users/ericloots/Trainingen/LightbendTraining/Pi-Akka-Cluster/exercise_004_cluster_base/target/scala-2.12/exercise_004_cluster_base-assembly-1.3.0.jar ...
[info] Done packaging.
[success] Total time: 14 s, completed Mar 14, 2018 12:01:29 PM
man [e] > mini-cluster > cluster_base >
```

Copy the new jar file to the boards by running `copy` script. Pass the number of the exercise you want to copy, exercise 4 in this case, as a single argument:

```
$ ./copy 4
./copy 4
Using exercise_004_cluster_base/target/scala-2.12/exercise_004_cluster_base-assembly-1.3.0.jar
Copy exercise_004_cluster_base/target/scala-2.12/exercise_004_cluster_base-assembly-1.3.0.jar to node-0
exercise_004_cluster_base-assembly-1.3.0.jar                                                                                            100%   31MB  11.2MB/s   00:02
Copy exercise_004_cluster_base/target/scala-2.12/exercise_004_cluster_base-assembly-1.3.0.jar to node-1
exercise_004_cluster_base-assembly-1.3.0.jar                                                                                            100%   31MB  10.5MB/s   00:02
Copy exercise_004_cluster_base/target/scala-2.12/exercise_004_cluster_base-assembly-1.3.0.jar to node-2
exercise_004_cluster_base-assembly-1.3.0.jar                                                                                            100%   31MB  11.2MB/s   00:02
Copy exercise_004_cluster_base/target/scala-2.12/exercise_004_cluster_base-assembly-1.3.0.jar to node-3
exercise_004_cluster_base-assembly-1.3.0.jar                                                                                            100%   31MB  10.5MB/s   00:02
Copy exercise_004_cluster_base/target/scala-2.12/exercise_004_cluster_base-assembly-1.3.0.jar to node-4
exercise_004_cluster_base-assembly-1.3.0.jar
```

Log in on the different node (use a multi-session terminal like iTerm-2 on MacOS for efficiency) and run the exercise code using the `run` script (copy this script from the repo to all nodes). The `run` command takes a single argument which is the number of the exercise you want to run.


> Note: The LED strip is powered from the 5V pin on the Raspberry Pi board. However, the GPIO pin that drives the data input pin on the LED strip is a 3.3V logical signal. This causes intermittent flashes on the LED's. This problem can be solved easily by putting a diode (such as an 1N4001) between the 5V pin on the Pi and the power connection on the LED strip.

> Note: Install `tmux` on the cluster nodes (`apt-get install tmux`) and always (interactive sessions) in a `tmux` session. This prevents processes (eg. nodes) getting killed when the connectivity of a node and your computer is broken (for example, as part of some test scenario like a network partition).

## Hardware build instructions

- If you want to assemble the LED status strip yourself, detailed instructions can be found [here](LED-status-indicator-assembly-instructions.md)

- Assembling the Pi's to form a physical cluster is relatively straightforward. See diagram [Cluster set-up schema](images/Cluster-set-up-schema.pdf) to see how it all connects together

## Generating a public/private key pair

We assume that we want to log in to account _akkapi_ on a pi from an account (_userxxx_) on your laptop.

First, we need a pair of authentication keys ***on the laptop***. If these keys have been generated already we can skip the generation of a new pair of authentication keys. If this isn't the case, generate them.

Logged-in on your laptop account, generate a key pair:

```
$ ssh-keygen -t rsa
Generating public/private rsa key pair.
Enter file in which to save the key (/home/userxxx/.ssh/id_rsa): 
Created directory '/home/userxxx/.ssh'.
Enter passphrase (empty for no passphrase): 
Enter same passphrase again: 
Your identification has been saved in /home/userxxx/.ssh/id_rsa.
Your public key has been saved in /home/userxxx/.ssh/id_rsa.pub.
The key fingerprint is:
3e:4f:05:79:3a:9f:96:7c:3b:ad:e9:58:37:bc:37:e4 userxxx@A
``` 

Use an empty passphrase (just hit _Enter_)

As can be seen from the output generated by _ssh-keygen_, a pair of files (`id_rsa` and `id_rsa.pub`) are created in the (hidden) folder `.ssh` in the users home folder.

Let's have a look:

```
$ ls -l ~/.ssh
total 56
-rw-------  1 ericloots  staff   3326 Dec 21  2015 id_rsa
-rw-r--r--  1 ericloots  staff    746 Dec 21  2015 id_rsa.pub
```