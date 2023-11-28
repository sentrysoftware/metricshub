keywords: about
description: ${solutionName} is an open source distribution of the OpenTelemetry Collector, designed to oversee diverse devices, applications, services, and hardware components across computers, network switches, and storage systems. It efficiently communicates the gathered metrics to any platform compatible with OpenTelemetry.

# Overview

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

**${solutionName}** functions as a universal metrics collector for OpenTelemetry, allowing customization for overseeing diverse technologies, encompassing applications, servers, and devices, particularly those without readily available monitoring solutions.

The **enterprise edition** includes the capability to monitor the hardware health of different computers, network switches, and storage systems and reports the power consumption, electricity costs and CO₂ emissions to support SRE initiatives.

The agent exposes tailored metrics to any observability platform supporting [OpenTelemetry](https://opentelemetry.io/docs), including [Datadog](https://www.datadoghq.com), [BMC Helix](https://www.bmc.com/it-solutions/bmc-helix.html), [Prometheus](https://prometheus.io), Splunk, and more.


![**${solutionName}** Architecture](./images/architecture.png)

**${solutionName}** is available in 2 editions:

* **Enterprise edition**: Thanks to its internal library of 250+ *Hardware Connectors*, **${solutionName}** can monitor almost any system from any manufacturer (**Cisco**, **Dell**, **EMC**, **Fujitsu**, **Hitachi**, **HP**, **IBM**, **Lenovo**, **NetApp**, **Oracle**, etc.) through `SNMP`, `IPMI`, `HTTP`, `WBEM`, `WMI`, `WINRM` or `SSH`.


    *${solutionName}* monitors the health of the internal electronic components of the system:

      * Batteries
      * Blades
      * CPUs
      * GPUs
      * LEDs
      * Memory modules
      * Network cards
      * Power supplies
      * Robotics
      * Sensors
          * Temperatures
          * Voltages
          * Fans
          * Power
      * Storage
          * Controllers
          * Disks
          * RAIDs
          * HBAs
          * LUNs
          * Tape drives
      * Virtual Machines
      * etc.

    *${solutionName}* also reports the energy usage and costs, as well as the carbon emissions of the monitored systems.

* **Community edition**: It offers only basic and community connectors

## What's in the box?

**${solutionName}** is comprised of:

* The **MetricsHub Agent**
    * Its [configuration file](configuration/configure-agent.md) (`metricshub-config.yaml`)
    * The monitoring engine, with the **Hardware Connector Library**
* The *OpenTelemetry Collector Contrib* executable
    * The *OpenTelemetry* [configuration file](configuration/configure-otel.md) (`otel-config.yaml`)
* MetricsHub CLI (`metricshub`)

![Internal architecture of the ${solutionName}](images/metricshub-internal-architecture.png)

> Note: **${solutionName}** is shipped with Java Runtime Environment 11.

### MetricsHub Agent

The **MetricsHub Agent** is the engine that performs the actual monitoring of the systems, based on its [configuration file](configuration/configure-agent.md), which specifies:

* Its internal polling cycle
* The hostnames and credentials of the systems to monitor.

The **MetricsHub Agent** is responsible for:

1. Spawning the **OpenTelemetry Collector Contrib**
2. Scraping hosts
3. Collecting metrics
4. Pushing OTLP data to the OTLP receiver of the OpenTelemetry Collector.

### OpenTelemetry Collector Contrib

The *OpenTelemetry Collector Contrib* is responsible for:

1. Pulling its metrics periodically (internally)
2. Pushing these metrics to the specified platform.

### Hardware Connector Library

In the enterprise edition, the library of **250+** *Hardware Connectors* is included in the **MetricsHub Agent**. It is the same library that powers [Hardware Sentry KM for PATROL](https://www.sentrysoftware.com/products/km-hardware-sentry.html), the original and battle-seasoned module for PATROL, created in 2004 by Sentry Software and used on hundreds of thousands of systems around the world.
The community edition offers just basic and community connectors

The platforms that can be monitored by **${solutionName}** entirely depends on this library. More details about the *Hardware Connector Library* are available as a [separate documentation](https://www.sentrysoftware.com/docs/hardware-connectors/latest/index.html).

### MetricsHub CLI (`metricshub`)

The [MetricsHub CLI](troubleshooting/cli.md) contains the same engine and library of *Hardware Connectors* as *MetricsHub Agent*, but packaged as a command line interface, that can be invoked in a shell.

This tool is particularly useful to troubleshoot the monitoring of a system, protocols and credentials.
