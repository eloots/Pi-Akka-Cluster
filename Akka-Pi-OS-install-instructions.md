## Pi-node set-up from a fully configured OS-image

- On your Mac, add the following entries to your /etc/hosts file:

```
# Cluster #0
192.168.0.101 node-0
192.168.0.102 node-1
192.168.0.103 node-2
192.168.0.104 node-3
192.168.0.105 node-4
```

- Download zip OS image
- Uncompress it
- Follow [these instructions](https://blog.hypriot.com/getting-started-with-docker-and-mac-on-the-raspberry-pi/) on how to flash this image to an SD card
- Insert card into node-4 (the top node in your stack of Pi's) and power-on
- Login to the Pi - it's configured as node-0

```
ssh akkapi@node-0
```

- The _akkapi_ account's password is _akkapi_
- Change the hostname to `node-4` by editing the `/etc/hostname` file

```
sudo vi /etc/hostname
```

- Change the IP address of the node to the one listed in /etc/hosts (192.168.0.105 for `node-4`) by editing the `/etc/network/interfaces.d/eth0` file

```
sudo vi /etc/network/interfaces.d/eth0
```

- Reboot the node by issuing the `sudo reboot` command
- Verify that after the reboot you can log-in to node-4

- Repeat the process for the remaining nodes (3 to 1, node-0 doesn't need the above modifications of course.

- Configure password-less login on your nodes by following the instructions on [this page](https://github.com/lightbend/Pi-Akka-Cluster/blob/master/Hypriot-OS-Customisation-Instructions.md). Search for the section that starts with the line: _"Next, we set-up password-less login."_.

- Clone the [Pi-Akka-Cluster](https://github.com/lightbend/Pi-Akka-Cluster) repository
- After this is done, _cd_ into the cloned repo and run the following commands:

```
CLUSTER_NR=0 ./updatePiScripts
CLUSTER_NR=0 ./clrJars
```

- You're now ready to test and use your cluster. Fire up _sbt_ and type `man e` and follow the instructions.