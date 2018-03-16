# Raspberry Pi - Kubernetes Cluster

## Description

This code is used on a Raspberry-Pi based Akka Cluster to demonstrate cluster formation, split-brain occurrence and Split Brain Resolver in a visual manner.

Each node is equipped with an 8-LED RGB strip where different LED's are used to show each node's state (UP, WEAKLY-UP, DOWN, JOINING, UNREACHABLE, LEAVING, EXITING, REMOVED). In addition, it shows if a node has a so-called _leader_ role and whether a node is running an Akka Cluster Singleton (if one is created on the cluster).

## Maintainer(s)

Eric Loots    - eric.loots@lightbend.com

Kikia Carter  - kikia.carter@lightbend.com

Duncan Devore - duncan.devore@lightbend.com

## Changelog

2018/03/14: Generalise code to allow for multiple clusters on a single network

## Build instructions

Start from standard _**Hypriot**_ [Raspberry Pi distro](http://blog.hypriot.com/getting-started-with-docker-on-your-arm-device/) (Version [1.7.1](http://blog.hypriot.com/downloads/))

Follow [instructions](http://blog.hypriot.com/getting-started-with-docker-and-mac-on-the-raspberry-pi/) on how to flash OS image to a micro SD card.

> __Important__: Note the remark in the instructions that the first boot takes some time as it resizes the file system (allow for up to 3 minutes).

> __Tip:__ In case you want to take a backup of your SD card (after customising it), use the lowest capacity you can get (e.g. 16GB or 32GB) _and_ a fast card. This will reduce the time to restore a backup to a (new) card.

After the initial log-in, run the following command to set the 

```
$ sudo locale-gen UTF-8
Generating locales (this might take a while)...
  en_US.UTF-8... done
Generation complete.
```


#### Update network configuration

Original config:

```
$ more /etc/network/interfaces.d/eth0
allow-hotplug eth0
iface eth0 inet dhcp
```

Change this to:

```
$ more /etc/network/interfaces.d/eth0
allow-hotplug eth0
iface eth0 inet static
    address 192.168.0.101
    netmask 255.255.255.0
    broadcast 10.0.0.255
    gateway 192.168.0.1
```

#### Update the network configuration to not update `/etc/hosts`: comment-out the line containing `update_etc_hosts` in file `/etc/cloud/cloud.cfg`:

```
# The modules that run in the 'init' stage
cloud_init_modules:
 - migrator
 - bootcmd
 - write-files
 - resizefs
 - set_hostname
 - update_hostname
# - update_etc_hosts
 - ca-certs
 - rsyslog
 - users-groups
 - ssh
```

- Set the hostname:

```
$ more /etc/hostname
kubernetes
```

#### Update /etc/hosts to add ip-addresses of other nodes

```
$ more /etc/hosts
127.0.1.1 black-pearl
127.0.0.1 localhost

192.168.0.101 kubernetes
192.168.0.102 node-1
192.168.0.103 node-2
192.168.0.104 node-3
192.168.0.105 node-4

# The following lines are desirable for IPv6 capable hosts
::1 ip6-localhost ip6-loopback
fe00::0 ip6-localnet
ff00::0 ip6-mcastprefix
ff02::1 ip6-allnodes
ff02::2 ip6-allrouters
ff02::3 ip6-allhosts
```

#### Repeat process for nodes 1 through 4

#### Notes on differences between this setup and the one in the book

- I decided to use local IP-addresses in a reserved range on my local LAN (192.168.0.X with X >= 100)
- I didn't add IP-forwarding on the kubernetes node, so the instructions to edit `/etc/sysctl.conf` and tuning of `/etc/rc.local` weren't executed.

#### Install Kubernetes

Slightly adapted instructions I used:

Add encryption keys for the packages:

```
$ curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
```

```
$ sudo vi /etc/apt/sources.list.d/kubernetes.list
$ more /etc/apt/sources.list.d/kubernetes.list
deb http://apt.kubernetes.io/ kubernetes-xenial main
$ sudo apt-get update
$ sudo apt-get upgrade
$ sudo apt-get install -y kubelet kubeadm kubectl kubernetes-cni
```

Execute the above step on all nodes.

### Add a new user for regular logins

Out of the gate, the system has a user named _pirate_ with sudo rights. It is advised to change the default password of this account and keep this account as-is for recovery purposes.

For normal use, a new user named _akkapi_ is created on the system, it is given sudo rights, added to the appropriate groups, an adapted bash profile and password-less ssh login is enabled from a given account on a remote system.

First we create the new user:

```
$ sudo adduser akkapi
Adding user `akkapi' ...
Adding new group `akkapi' (1001) ...
Adding new user `akkapi' (1001) with group `akkapi' ...
Creating home directory `/home/akkapi' ...
Copying files from `/etc/skel' ...
Enter new UNIX password:
Retype new UNIX password:
passwd: password updated successfully
Changing the user information for akkapi
Enter the new value, or press ENTER for the default
	Full Name []: Akka Pi
	Room Number []:
	Work Phone []:
	Home Phone []:
	Other []:
Is the information correct? [Y/n] y
HypriotOS/armv7: pirate@node-0 in ~
```

Next, we add user _akkapi_ to groups _pirate_, _video_ and _docker_:

```
$ sudo adduser akkapi pirate
Adding user `akkapi' to group `pirate' ...
Adding user akkapi to group pirate
Done.
HypriotOS/armv7: pirate@node-0 in ~

$ sudo adduser akkapi video
Adding user `akkapi' to group `video' ...
Adding user akkapi to group video
Done.
HypriotOS/armv7: pirate@node-0 in ~

$ sudo adduser akkapi docker
Adding user `akkapi' to group `docker' ...
Adding user akkapi to group docker
Done.
```

We now switch to the _akkapi_ user to change a couple of things:

```
$ su - akkapi
Password:
HypriotOS/armv7: akkapi@node-0 in ~

$ groups
akkapi video docker pirate

$ cat > .profile.tmux <<EOF
tnr() {
   tmux new -s run
}

tr() {
   tmux attach -t run
}

tnl() {
   tmux new -s log
}

tl() {
   tmux attach -t log
}

tkillall() {
   for session in `tmux ls | sed -e 's/:.*//'`;do
     tmux kill-session -t $session
   done
}
EOF
```

Next, edit the _.profile_ file to add `. .profile.tmux` at the end of the file. The end of that file now looks like:

```
$ tail -4 .profile

mesg n

. .profile.tmux
HypriotOS/armv7: akkapi@node-0 in ~
```

Next, we set-up password-less login. We assume that we want to log in to account _akkapi_ on a pi from an account (_userxxx_) on your laptop named _A_.

First, we need a pair of authentication keys on the laptop. If these keys have been generated already we can skip the generation of a new pair of authentication keys. If this isn't the case, generate them:

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

Now, proceed by create a `.ssh` folder on the _akkapi_'s home folder on the Pi and copy the public key (`id_rsa.pub`) to a file named `authorized_keys` in the `.ssh` folder. You will have to supply the password for the _akkapi_ account for the two commands launched.

```
 [userxxx@Eric-Loots-MBP] $ ssh akkapi@node-0 mkdir .ssh
akkapi@node-0's password:

 [userxxx@Eric-Loots-MBP] $ cat .ssh/id_rsa.pub | ssh akkapi@node-0 'cat >> .ssh/authorized_keys'
akkapi@node-0's password:
```

With this, you should now be able to log into the _akkapi_ on _node-0_ from the _userxxx_ account on laptop _A_ without having to enter a password.

As a final modification, we give account _akkapi_ sudo rights. First, log-in with the _pirate_ account on the pi and add two lines to file `/etc/sudoers.d/90-cloud-init-users`:

```
 [ericloots@Eric-Loots-MBP] $ ssh pirate@node-0
HypriotOS/armv7: pirate@node-0 in ~

$ sudo vi /etc/sudoers.d/90-cloud-init-users
HypriotOS/armv7: pirate@node-0 in ~

$ sudo cat /etc/sudoers.d/90-cloud-init-users
# Created by cloud-init v. 0.7.6 on Thu, 03 Jul 2014 18:46:34 +0000

# User rules for pirate
pirate ALL=(ALL) NOPASSWD:ALL
# User rules for akkapi
akkapi ALL=(ALL) NOPASSWD:ALL
HypriotOS/armv7: pirate@node-0 in ~
```

> Note: as file `/etc/sudoers.d/90-cloud-init-users` has read-only file protections set, you must force-write the file (enter the `w!` instead of `w` in vi).

### Intermediate stage backup

_Probably a good time to make a backup of what we have so far._

## Driving a NeoPixel 8-LED strip

Install build-tools (gcc, ...) & _swig_:

```
sudo apt install build-essential
sudo apt-get install swig
```

__Download and build software__ (see instructions in README.md in the repo) for driving NeoPixels: [rpi_ws281x](https://github.com/jgarff/rpi_ws281x)

Install JDK 8:

```
$ sudo apt-get install oracle-java8-jdk
$ sudo apt-get update
```

Set `JAVA_HOME`

```
$ sudo update-alternatives --config java
There is only one alternative in link group java (providing /usr/bin/java): /usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/jre/bin/java
Nothing to configure.
```

This tells us that `JAVA_HOME` is `/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt`

```
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
-rwxr-xr-x 1 pirate pirate 91480 Jan 20 13:42 librpi_ws281x.so
```

Running a sample application:

```
$ sudo java -Djava.library.path=. -jar exercise_000_initial_state-assembly-1.3.0.jar com.neopixel.Main 5
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
[info] 	   exercise_004_cluster_base
[info] 	   exercise_005_cluster_weakly_up
[info] 	   exercise_006_cluster_cluster_singleton
[info] 	   exercise_007_cluster_the_perils_of_auto_downing
[info] 	   exercise_008_cluster_weakly_up_disabled
[info] 	   exercise_009_split_brain_resolver_keep_majority
[info] 	   exercise_010_split_brain_resolver_static_quorum
[info] 	   exercise_011_split_brain_resolver_keep_referee
[info] 	   exercise_012_split_brain_resolver_keep_oldest
[info] 	 * exercise_013_split_brain_resolver_static_quorum_http_mamagement
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