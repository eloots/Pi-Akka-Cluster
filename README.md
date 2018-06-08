# Raspberry Pi - Kubernetes Cluster

## Description

This code is used on a Raspberry-Pi based Akka Cluster to demonstrate cluster formation, split-brain occurrence and Split Brain Resolver in a visual manner.

Each node is equipped with an 8-LED RGB strip where different LED's are used to show each node's state (`Joining`, `Up`, `Weakly-up`, `Unreachable`, `Leaving`, `Exiting` and `Down`/`Removed`). In addition, it shows if a node has a so-called _leader_ role and whether a node is running an Akka Cluster Singleton (if one is created on the cluster).

In the last series of exercises, we deploy an actor based application (a Sudoku Solver) on the cluster and add monitoring (`Cinnamon`/`Prometheus`/`Grafana`) and tracing to the set-up (`Open-tracing`/`Zipkin`).

## Maintainer(s)

Eric Loots    - eric.loots@lightbend.com

Kikia Carter  - kikia.carter@lightbend.com

Duncan Devore - duncan.devore@lightbend.com

## Changelog

2018/06/08: Add detailed exercise instructions
2018/06/08: Move Hypriot OS customisation instructions to a separate file
2018/03/14: Generalise code to allow for multiple clusters on a single network

## Instructions

- Make sure you completed the installation of
    - Java 8 SDK
    - Some terminal (iTerm-2 on Mac, Terminator on Linux style OS-es, Powershell or ConEmu on Windows
    - curl
    - 
- Clone the project on your laptop

## Pi-Hypriot customisation instructions

Detailed instructions on how to customise the Hypriot OS can be found [here](Hypriot-OS-Customisation-Instructions.md).

