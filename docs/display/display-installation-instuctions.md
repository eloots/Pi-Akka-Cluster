## Pi-Hypriot OS installation instructions for Display Assembly

### Network Config
To be able to reproduce network partitioning and still have access to node we'll have two networks configured for each PI.
* Ethernet network will be used by clustering cross node communications. Default IPs are 192.168.1.10X here.
* WiFi will be used for all other communications: Akka management, Akka persistence, REST endpoints. Default IPs are 192.168.8.10X.

### Laptop config

On your Mac, add the following entries to your _/etc/hosts_ file:
```
192.168.8.100 node-0
192.168.8.101 node-1
192.168.8.102 node-2
192.168.8.103 node-3
```
Also you need to join the same WiFi network that PI Cluster is in.

### Preparing SD cards with linux image

> With support for `cloud-init` being present in the [Hypriot OS](http://blog.hypriot.com) distribution, we switch to using this tool instead starting from a standard installation and then going through a lengthy manual install.

The installation is now relatively simple:

Download and install the Hypriot _**flash**_ tool by following the instructions in the **Installation** section [here](https://github.com/hypriot/flash#installation).

Using the `akka-pi-os-display.yml` file in this repo, a 16GB micro SD card and a flash card reader/writer, you are now set to flash an SD card.
 
You will need to customise several parameters in the `.yml`: 
* ethernet IP-address of the node. Here you need to change `static ip_address=192.168.1.100/24` to node ethernet IP
* WiFi IP-address of the node. Here you need to change `static ip_address=192.168.8.100/24` to node WiFi IP
* add your WiFi ssid and password. Update following piece  
```
network = {
              ssid="GL-MT300N-V2-e9f"
              psk="goodlife"
          }
```

The command to flash the card is (**always check the [Hypriot downloads page](https://blog.hypriot.com/downloads/) to find out what the most recent version is!**):

```
Pi-Akka-Cluster git:(master) ✗ flash -n node-0 -u akka-pi-os-display.yml \
   https://github.com/hypriot/image-builder-rpi/releases/download/v1.12.0/hypriotos-rpi-v1.12.0.img.zip
```

`-n node-0` specifies the host name for the node. For other nodes, set it to a name of the host this card is destined for (`node-0` through `node-4`)

Next you need to insert SD cards to PIs and start your nodes. It takes time for them to finish configuration and start for the first time