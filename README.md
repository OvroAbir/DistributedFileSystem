# DistributedFileSystem
This is a simplified implementation of Google Drive file system. This tool was implemented as a part of course work of [CS555](https://www.cs.colostate.edu/~cs555/index.html). It has the idea of master node, chunk servers, heart beat message passing, multiple copies of the same file is stored on 3 different chunk servers. This is basically a distributed, fault tolerant file system. 

## Prerequisites
Java 1.8 needs to be installed. No other external library is needed to be installed. This project can be build and run by importing the project in Eclipse.

## Source Code
* The master node or control node can be run using [ControlNodeRun.java](src/ControlNodeRun.java) file.
* The chunk servers can be run using [ChunkServerRunner.java](src/ChunkServerRunner.java) file. This file should be run for each instance of different chunk servers.
* The client node should be run using [ClientRun.java](src/ClientRun.java) file.

## Algorithm & Implementation Idea
The detailed algorithm and requirements can be found in details in [CS555-Fall2019-HW1.pdf](docs/CS555-Fall2019-HW1.pdf) file.

## Authors
* [Joy Ghosh](https://www.ijoyghosh.com)

 