# Raspberry Pi - Kubernetes Cluster

## Description

This code is used on a Raspberry-Pi based Akka Cluster to visually demonstrate cluster formation, split-brain occurrence and Split Brain Resolver.

Each node is equipped with an 10-LED RGB strip where different LED's are used to show each node's state (`Joining`, `Up`, `Weakly-up`, `Unreachable`, `Leaving`, `Exiting` and `Down`/`Removed`). In addition, it shows if a node has a so-called _leader_ role and whether a node is running an Akka Cluster Singleton (if one is created on the cluster).

> In addition to 10-LED Cluster there is also OLED display based cluster. Instructions for display assembly can be found [here](docs/display/display-version.md) 

In the last series of exercises, we deploy an actor based application (a Sudoku Solver) on the cluster and add monitoring (`Cinnamon`/`Prometheus`/`Grafana`) and tracing to the set-up (`Open-tracing`/`Zipkin`).

## Maintainers & Changelog

Removed the changelog & list of maintainers. Git history contains this information.

## Instructions

- Make sure you completed the installation of
    - Java 11 SDK.
    - Some terminal ([iTerm-2 on Mac](https://iterm2.com), [Terminator on Linux](https://gnometerminator.blogspot.com/p/introduction.html) style OS-es, Powershell or [ConEmu on Windows](https://conemu.github.io)).
    - curl.
- Clone [this project](https://github.com/lightbend/Pi-Akka-Cluster) on your laptop.

## Preparing your laptop for a _Raspberry-Pi based Akka Cluster_ workshop

Detailed instructions can be found [here](docs/Hypriot-OS-Course-Preparation-Instructions.md).

## Building your own 5-node Raspberry-Pi based Akka Cluster

If you want to build your own physical cluster, you may want to have a look at the BOM [here](docs/images/BOM.md).

## Pi-Hypriot installation instructions

The easiest and _fastest_ way to flash a Hypriot OS instance that is ready to use for the Akka Cluster software can be found [here](docs/Hypriot-OS-Installation-Instructions.md).

Display version instructions are a little different and available [here](docs/display/display-installation-instuctions.md) 

## Travel router configuration

In some cases it can be handy to use a so-called travel router which allows you to connect to a Wifi network on location, and still get access to a physical network with a fixed IP network address. This is because, currently, all nodes have statically configured IP addresses.

Details on how to configure such a (TP-Link TL-WR802N) router can be found [here](docs/Configuring-TP-Link-Travel-Router-for-class-room-environment.md).

## Build your own Akka-Cluster on Raspberry Pi

- A bill of material can be found [here](docs/images/BOM.md)
- A custom 10-LED strip that can be directly plugged on a Raspberry Pi board's GPIO connector can now be bought from Lunatech! You can buy them in a pack of 5. For more info, send a mail to info@lunatech.com.
- A schematic of the LED strip can be found [here](docs/images/raspberry_led_platine_v2.pdf)

>Instructions for Display assembly are available [here](docs/display/display-assembly-instructions.md)


## Running 64-bit Ubuntu

Ubuntu has a 64-bit ARM version with built-in cloud-init support. As such, you can
flash 64-bit capable boards (Raspberry Pi 3 or 4) with this operating system. The
`akka-pi-os-dhcp-64.yml` cloud-init file has the required modifications to flash a
card for that version of the OS on ARM, with the most important difference with the
`akka-pi-os-dhcp-32.yml` being a 64-bit binary for the installation progress tracker.

This has been verified on a RPi4. A slightly modified version of the Hypriot flash
utility is required though.

```
flash -n node-1 -u akka-pi-os-dhcp-64.yml \\n   http://cdimage.ubuntu.com/releases/20.04/release/ubuntu-20.04.2-preinstalled-server-arm64+raspi.img.xz
```
