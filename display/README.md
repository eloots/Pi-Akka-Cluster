# Raspberry Pi - Kubernetes Cluster with Display

## Description
This code is used on Raspberry PI display to demonstrate Akka cluster formation with sharding and related technologies.

Each node is equipped with OLED (ER-OLEDM032) display to show cluster node state, performance metrics, actor states etc.
Display is graphical with grades of gray. It is slow enough so user can see changes that are usually to fast to capture. 
System has several logical views and user can switch between them with button click

Code should be used in addition to 10-LED RGB strip cluster installation 

## Add displays to Akka cluster
Following guidelines describe only modifications that need to be done on LED cluster installation

- A bill of material for adding display [here](BOM.md)
- Schema for display connector [here](displaySchema.md)
- Schema for button connector [here](buttonSchema.md)