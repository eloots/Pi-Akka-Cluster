# Raspberry Pi - Kubernetes Cluster

## Changelog

Start from standard _**Hypriot**_ [Raspberry Pi distro](http://blog.hypriot.com/getting-started-with-docker-on-your-arm-device/) (Version [1.7.1](http://blog.hypriot.com/downloads/))

Follow [instructions](http://blog.hypriot.com/getting-started-with-docker-and-mac-on-the-raspberry-pi/) on how to flash OS image to a micro SD card.

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

Download and build software for driving NeoPixels: [rpi_ws281x](https://github.com/jgarff/rpi_ws281x)

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
$ mkdir java
$ cp python/*.i java
$ cd java
$ swig -java rpi_ws281x.i
$ gcc -Wl,--add-stdcall-alias -c *.c -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux
$ gcc -Wl,--add-stdcall-alias -shared ../*.o rpi_ws281x_wrap.o -o librpi_ws281x.so

$ sudo java -Djava.library.path=. -jar exercise_000_initial_state-assembly-1.3.0.jar com.neopixel.Main 5
```

Once the shared library has been built, it is the only thing to enable access via java/scala to the NeoPixel strip.

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
