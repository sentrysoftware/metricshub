keywords: command-line tool, hardware monitoring
description: The Hardware Sentry CLI is a free command-line tool to check the platform prerequisites of the Sentry Software's Hardware Monitoring solutions.

# What is ${project.name}?

The **${project.name}** is the core Hardware Sentry engine wrapped in a command line interface. System administrators can easily invoke this tool in a shell to discover the hardware components of the specified host and report any hardware-related problem.

The **${project.name}** can be used to troubleshoot the monitoring performed by other Hardware Sentry products such as the KM for PATROL, or the Exporter for Prometheus.

As the  **${project.name}** runs on Java, it can be used similarly on Windows or Linux to monitor the local system or any remote host.

![The ${project.name}](./images/running-hardware-sentry-cli.png)

Only a few options are required to run the **${project.name}**:

* the hostname or IP address of the device to be monitored
* the device type
* the protocol to be used. The **${project.name}** currently supports:

    * HTTP
    * IPMI
    * SSH
    * SNMP
    * WBEM
    * WMI.
