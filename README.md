# Raspberry Pi - Kubernetes Cluster

## Description

This code is used on a Raspberry-Pi based Akka Cluster to demonstrate cluster formation, split-brain occurrence and Split Brain Resolver in a visual manner.

Each node is equipped with an 8-LED RGB strip where different LED's are used to show each node's state (UP, WEAKLY-UP, DOWN, JOINING, UNREACHABLE, LEAVING, EXITING, REMOVED). In addition, it shows if a node has a so-called _leader_ role and whether a node is running an Akka Cluster Singleton (if one is created on the cluster).

## Maintainer(s)

Eric Loots - eric.loots@lightbend.com

## Changelog

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

### Intermediate stage backup

Probably a good time to make a backup of what we have so far.

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
$ sbt assembly
```

Copy the new jar file to the boards by running the following script:

```
$ cat copy_001
#!/bin/bash

for i in 0 1 2 3 4;do
  scp exercise_001_cluster_base/target/scala-2.12/exercise_001_cluster_base-assembly-1.3.0.jar pirate@node-${i}:/home/pirate
done
```

Start an ssh session on one of the boards and start-up the application.

On node-0:

```
sudo java -Djava.library.path=. -Dakka.remote.netty.tcp.port=2550 -Dakka.remote.netty.tcp.hostname=192.168.0.101 -jar exercise_001_cluster_base-assembly-1.3.0.jar com.neopixel.Main
```

On node-1:

```
sudo java -Djava.library.path=. -Dakka.remote.netty.tcp.port=2550 -Dakka.remote.netty.tcp.hostname=192.168.0.102 -jar exercise_001_cluster_base-assembly-1.3.0.jar com.neopixel.Main
```
etc...

> Note: The LED strip is powered from the 5V pin on the Raspberry Pi board. However, the GPIO pin that drives the data input pin on the LED strip is a 3.3V logical signal. This causes intermittent flashes on the LED's. This problem can be solved easily by putting a diode (such as an 1N4001) between the 5V pin on the Pi and the power connection on the LED strip.

> Note: Install `tmux` on the cluster nodes (`apt-get install tmux`) and always (interactive sessions) in a `tmux` session. This prevents processes (eg. nodes) getting killed when the connectivity of a node and your computer is broken (for example, as part of some test scenario like a network partition).