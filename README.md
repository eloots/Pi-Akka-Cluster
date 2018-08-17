# Raspberry Pi - Kubernetes Cluster

## Description

This code is used on a Raspberry-Pi based Akka Cluster to demonstrate cluster formation, split-brain occurrence and Split Brain Resolver in a visual manner.

Each node is equipped with an 8-LED RGB strip where different LED's are used to show each node's state (`Joining`, `Up`, `Weakly-up`, `Unreachable`, `Leaving`, `Exiting` and `Down`/`Removed`). In addition, it shows if a node has a so-called _leader_ role and whether a node is running an Akka Cluster Singleton (if one is created on the cluster).

In the last series of exercises, we deploy an actor based application (a Sudoku Solver) on the cluster and add monitoring (`Cinnamon`/`Prometheus`/`Grafana`) and tracing to the set-up (`Open-tracing`/`Zipkin`).

## Maintainers

| Name               | Email address                           |
|-------------------:|:----------------------------------------|
| Eric Loots         | eric.loots@lightbend.com                |
| Kikia Carter       | kikia.carter@lightbend.com              |
| Henrik Engström    | henrik.engstrom@lightbend.com           |
| Duncan Devore      | duncan.devore@lightbend.com             |

## Changelog

| Date      | Change                                |
|:---------:|:--------------------------------------|
|2018/08/15 | Updated docs for _cloud-init_ based install + updated LED driver library for Raspberry Pi 3 Model B+|
|2018/08/06 | Add Bill of Material |
|2018/06/08 | Add detailed exercise instructions |
|2018/06/08 | Move Hypriot OS customisation instructions to a separate file |
|2018/03/14 | Generalise code to allow for multiple clusters on a single network |

## Instructions

- Make sure you completed the installation of
    - Java 8 SDK
    - Some terminal (iTerm-2 on Mac, Terminator on Linux style OS-es, Powershell or ConEmu on Windows
    - curl
- Clone the project on your laptop

## Building your own 5-node Raspberry-Pi based Akka Cluster

If you want to build your own physical cluster, you may want to have a look at the BOM [here](images/BOM.md).


## Download & flash a fully customised Hypriot OS

> _**The recommended way to flash a customised OS image is documented in the next paragraph.**_

Instructions on where to get a fully configured OS can be found [here](Akka-Pi-OS-install-instructions.md).

## Pi-Hypriot customisation instructions

The easiest and _fastest_ way to flash a Hypriot OS instance that is ready to use for the Akka Cluster software can be found [here](Hypriot-OS-Customisation-Instructions.md).

## Travel router configuration

In some cases it can be handy to use a so-called travel router which allows you to connect to a Wifi network on location, and still get access to a physical network with a fixed IP network address. This is because, currently, all nodes have statically configured IP addresses.

Details on how to configure such a (TP-Link TL-WR802N) router can be found [here](Configuring-TP-Link-Travel-Router-for-class-room-environment.md).

