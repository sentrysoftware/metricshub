keywords: overview
description: The ${project.name} (hws) is the core Hardware Sentry engine wrapped in a command line interface. System administrators can easily invoke this tool in a shell to discover the hardware components of the specified host and report any hardware-related problem.

# Overview

The **${project.name}** (`hws`) is the core *Hardware Sentry engine* wrapped in a command line interface. System administrators can easily invoke this tool in a shell to discover the hardware components of the specified host and report any hardware-related problem.

Discovered components include:

* Enclosure (manufacturer, model, serial number)
* Processors
* Memory modules
* GPUs
* Disks (HDD, SDD, RAID)
* Network and Fiber Channel Adapters
* Sensors (temperature, voltage, fans, power, LEDs)

Supported systems include:

* Servers (Linux, AIX, HP-UX, Solaris, Windows, in-band and out-of-band)
* Blade chassis
* Network switches
* SAN switches
* Storage systems (disk arrays, filers, tape libraries)

The detailed list of supported systems (manufacturer and product family and the required instrumentation stack) is listed in [Sentry's Hardware Connectors Library](https://www.sentrysoftware.com/docs/hardware-connectors/latest/platform-requirements.html).

The quantity and quality of the information that **${project.name}** will be able to gather depends on the instrumentation stack available on the targeted host.

![Output example for an HP ProLiant system](./images/hws-proliant.png)

Only a few options are required to run the `hws` command:

* Hostname or IP address of the device to be monitored
* Device type
* Protocols to be used:
    * HTTP
    * IPMI-over-LAN
    * SSH
    * SNMP
    * WBEM
    * WMI (on Windows only)
* Credentials

![Usage of hws](./images/hws-usage.png)

The `hws` command can be used to troubleshoot the monitoring performed by other *Hardware Sentry* products such as the KM for PATROL, or the Exporter for Prometheus.
