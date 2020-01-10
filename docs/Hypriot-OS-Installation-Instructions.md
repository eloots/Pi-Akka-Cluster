## Pi-Hypriot OS installation instructions

> ***NOTE: AFTER INITIAL POWER-UP OF THE NODES, AN INSTALLATION SCRIPT IS RUN WHICH WILL INSTALL THE OPENJDK AND A NUMBER OF PACKAGES. IT IS RECOMMENDED TO PLUG AN LED STRIP ON THE GPIO CONNECTOR AS IT WILL DISPLAY THE PROGRESS OF THE INSTALLATION (ALL LEDS TURN GREEN WHEN THE INSTALLATION IS COMPLETED)***


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

The command to flash the card is (**always check the [Hypriot downloads page](https://blog.hypriot.com/downloads/) to find out what the most recent version is!**):

```
Pi-Akka-Cluster git:(master) ✗ flash -n node-1 -u akka-pi-os.yml \
   https://github.com/hypriot/image-builder-rpi/releases/download/v1.12.0/hypriotos-rpi-v1.12.0.img.zip
```

`-n node-0` specifies the host name for the node. For other nodes, set it to a name of the host this card is destined for (`node-0` through `node-4`)

We're also using the latest version of Hypriot (1.12.0 at the time of writing). For a complete list of available versions, see the [downloads page](https://blog.hypriot.com/downloads/).

Next:

- Insert the card into the micro SD slot on your Raspberry Pi (should work on Raspberry Pi 2 and 3 - Model B and B+).
- Power-up the Pi.
- Be patient... Installation progress is indicated on the LED strip (provided one is plugged into the GPIO connector).
- After about 1.5 minute, you can log-in on the Raspberry Pi. Log-in as user _akkapi_ using ssh (password is also _akkapi_).
- It takes between 4 and 6 minutes to complete the initial installation (depending on the Raspberry Pi model and the speed of the micro SD card).

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

### Configure password-less login & finish the configuration

Follow the instructions in paragraphs `"Configure password-less login"` and `"Finishing the configuration"` in the [Hypriot OS Course Preparation Instructions](Hypriot-OS-Course-Preparation-Instructions.md) document.

### Running base cluster with status LED indicators

Start an `sbt` session and have a look at the available exercises by running the `projects` command:

```
$ sbt
man [e] > Pi-Akka-Cluster > initial state > projects
[info] In file:/Users/ericloots/Trainingen/LightbendTraining/Pi-Akka-Cluster/
[info] 	   common
[info] 	 * exercise_000_initial_state
[info] 	   exercise_002_cluster_base
[info] 	   exercise_003_cluster_weakly_up
[info] 	   exercise_004_cluster_singleton
[info] 	   exercise_005_cluster_the_perils_of_auto_downing
[info] 	   exercise_006_cluster_weakly_up_disabled
[info] 	   exercise_007_split_brain_resolver_keep_majority
[info] 	   exercise_008_split_brain_resolver_static_quorum
[info] 	   exercise_009_split_brain_resolver_keep_referee
[info] 	   exercise_010_split_brain_resolver_keep_oldest
[info] 	   exercise_011_split_brain_resolver_down_all
[info] 	   exercise_012_split_brain_resolver_static_quorum_http_mamagement
[info] 	   exercise_013_clustered_sudoku_solver
[info] 	   exercise_014_add_cluster_client
[info] 	   exercise_015_clustered_sudoku_solver_cluster_client_enabled
[info] 	   exercise_017_es_opentracing
[info] 	   exercise_018_es_classic_console
[info] 	   exercise_050_cluster_cluster_singleton_akka_bootstrap_discovery_via_config
[info] 	   exercise_051_cluster_singleton_akka_bootstrap_discovery_via_akka_dns
[info] 	   pi_cluster_master
```

Next, we build the first exercise (`exercise_000_initial_state`):

```
man [e] > Pi-Akka-Cluster > initial state > ;clean;universal:packageBin
[success] Total time: 0 s, completed Nov 14, 2019 8:21:39 PM
[success] All package validations passed
<... elided>

[success] Total time: 6 s, completed Nov 14, 2019 8:21:46 PM
```

Quit `sbt` and copy the packaged binaries to the boards by running `copy` script. Pass the number of the exercise you want to copy, exercise 0 in this case, as a single argument:

```
$ ./copy 0
Copy exercise_000_initial_state-1.3.0.zip to node-0
exercise_000_initial_state-1.3.0.zip                                     100%   31MB  11.3MB/s   00:02
Unzipping archive
Copy exercise_000_initial_state-1.3.0.zip to node-1
exercise_000_initial_state-1.3.0.zip                                     100%   31MB  11.2MB/s   00:02
Unzipping archive
Copy exercise_000_initial_state-1.3.0.zip to node-2
exercise_000_initial_state-1.3.0.zip                                     100%   31MB  11.1MB/s   00:02
Unzipping archive
Copy exercise_000_initial_state-1.3.0.zip to node-3
exercise_000_initial_state-1.3.0.zip                                     100%   31MB  11.4MB/s   00:02
Unzipping archive
Copy exercise_000_initial_state-1.3.0.zip to node-4
exercise_000_initial_state-1.3.0.zip                                     100%   31MB  11.0MB/s   00:02
Unzipping archive
```

Log in on the different node (use a multi-session terminal like iTerm-2 on MacOS for efficiency) and run the exercise code using the `run` script. The `run` command takes a single argument which is the number of the exercise you want to run. For example, on `node-1`:

```
$ ./run 0
Running exercise_000_initial_state on node-1
```

The LEDs on the LED strip should blink in different colors for about 5 seconds.

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

## Hardware build instructions

- If you want to assemble the LED status strip yourself, detailed instructions can be found [here](LED-status-indicator-assembly-instructions.md).

- Assembling the Pi's to form a physical cluster is relatively straightforward. See diagram [Cluster set-up schema](images/Cluster-set-up-schema.pdf) to see how it all connects together.