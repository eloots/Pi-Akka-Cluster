# Raspberry Pi - Kubernetes Cluster

## Description

This code is used on a Raspberry-Pi based Akka Cluster to demonstrate cluster formation, split-brain occurrence and Split Brain Resolver in a visual manner.

Each node is equipped with an 8-LED RGB strip where different LED's are used to show each node's state (`Joining`, `Up`, `Weakly-up`, `Unreachable`, `Leaving`, `Exiting` and `Down`/`Removed`). In addition, it shows if a node has a so-called _leader_ role and whether a node is running an Akka Cluster Singleton (if one is created on the cluster).

In the last series of exercises, we deploy an actor based application (a Sudoku Solver) on the cluster and add monitoring (`Cinnamon`/`Prometheus`/`Grafana`) and tracing to the set-up (`Open-tracing`/`Zipkin`).

## Maintainers & Changelog

Removed the changelog & list of maintainers. Git history contains this information.

## Instructions

- Install the following on the laptop or desktop that you will use for connecting to the pi cluster
    - Java 8 SDK
    - Some terminal (iTerm-2 on Mac, Terminator on Linux style OS-es, Powershell or ConEmu on Windows
    - curl
- Clone the project on your laptop

## Running the exercises

- start from inside the project root
    - `cd Pi-Akka-Cluster`
- start the course using `sbt`
    - type `sbt` from inside the project root
- you should see the project starting from the initial state
the first time you run it. Subsequent starts will be from the 
last state where you left off. 
- to see the instructions for the current exercise type
    - `man e`
- you will find full instructions to run each exercise and progress to the next 
within the `man e`
- for a quickstart to using the course tools, see [here](course-tools-quickstart.md)

## Pi-Hypriot installation instructions

The easiest and _fastest_ way to flash a Hypriot OS instance that is ready to use for the Akka Cluster software can be found [here](Hypriot-OS-Installation-Instructions.md).

## Travel router configuration

In some cases it can be handy to use a so-called travel router which allows you to connect to a Wifi network on location, and still get access to a physical network with a fixed IP network address. This is because, currently, all nodes have statically configured IP addresses.

Details on how to configure such a (TP-Link TL-WR802N) router can be found [here](Configuring-TP-Link-Travel-Router-for-class-room-environment.md).

## Build your own Akka-Cluster on Raspberry Pi

- A bill of material can be found [here](images/BOM.md)
- A schematic of the LED strip can be found [here](images/Schema_LED_Strip.pdf)

