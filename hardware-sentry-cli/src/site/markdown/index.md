# Getting Started

## What is ${project.name}?

The **${project.name}** is an open source tool that enables you to monitor the hardware of almost **any server** (physical, virtual, and blade servers) and **external storage device** (disk arrays, fiber switches, and tape libraries) available in datacenters using commands in your command-line shell.

With minimal configuration, **${project.name}** can report the hardware health of your systems. You just have to specify:

* the hostname or IP address of the device to be monitored
* its type
* the protocol to be used.

## How does it work?

**${project.name}** relies on the **Hardware Connector Library** which consists of several hardware connectors (*.hdf files) that describe how to discover hardware components and detect failures. The connectors are automatically selected based on the device type and enabled protocols. The protocols currently supported are:  

* HTTP
* SNMP
* WBEM
* WMI

**${project.name}** provides detailed information about each monitored component (vendor, model, serial number, part number, FRU number, location in the chassis, etc.).

For more information about the **Hardware Connector Library**, refer to the <a href="//www.sentrysoftware.com/library/hc/" target="_blank">User Documentation</a>.