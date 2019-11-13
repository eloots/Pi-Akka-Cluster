# Raspberry Pi - Kubernetes Cluster

## Description

This code is used on a Raspberry-Pi based Akka Cluster to visually demonstrate cluster formation, split-brain occurrence and Split Brain Resolver.

Each node is equipped with an 10-LED RGB strip where different LED's are used to show each node's state (`Joining`, `Up`, `Weakly-up`, `Unreachable`, `Leaving`, `Exiting` and `Down`/`Removed`). In addition, it shows if a node has a so-called _leader_ role and whether a node is running an Akka Cluster Singleton (if one is created on the cluster).

In the last series of exercises, we deploy an actor based application (a Sudoku Solver) on the cluster and add monitoring (`Cinnamon`/`Prometheus`/`Grafana`) and tracing to the set-up (`Open-tracing`/`Zipkin`).

## Maintainers & Changelog

Removed the changelog & list of maintainers. Git history contains this information.

## Instructions

- Make sure you completed the installation of
    - Java 8 SDK.
    - Some terminal ([iTerm-2 on Mac](https://iterm2.com), [Terminator on Linux](https://gnometerminator.blogspot.com/p/introduction.html) style OS-es, Powershell or [ConEmu on Windows](https://conemu.github.io)).
    - curl.
- Clone [this project](https://github.com/lightbend/Pi-Akka-Cluster) on your laptop.

## Preparing your laptop for a _Raspberry-Pi based Akka Cluster_ workshop

Detailed instructions can be found [here](docs/Hypriot-OS-Course-Preparation-Instructions.md).

## Building your own 5-node Raspberry-Pi based Akka Cluster

If you want to build your own physical cluster, you may want to have a look at the BOM [here](docs/images/BOM.md).

## Pi-Hypriot installation instructions

The easiest and _fastest_ way to flash a Hypriot OS instance that is ready to use for the Akka Cluster software can be found [here](docs/Hypriot-OS-Installation-Instructions.md).

## Travel router configuration

In some cases it can be handy to use a so-called travel router which allows you to connect to a Wifi network on location, and still get access to a physical network with a fixed IP network address. This is because, currently, all nodes have statically configured IP addresses.

Details on how to configure such a (TP-Link TL-WR802N) router can be found [here](docs/Configuring-TP-Link-Travel-Router-for-class-room-environment.md).

## Build your own Akka-Cluster on Raspberry Pi

- A bill of material can be found [here](docs/images/BOM.md)
- A custom 10-LED strip that can be directly plugged on a Raspberry Pi board's GPIO connector can now be bought from Lightbend! You can buy them in a pack of 5 [here](https://shop.lightbend.com/products/custom-led-strips-for-raspberry-pi-clusters).
- A schematic of the LED strip can be found [here](docs/images/raspberry_led_platine_v2.pdf)

