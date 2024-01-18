keywords: about
description: ${solutionName} is an open source distribution of the OpenTelemetry Collector, designed to monitor diverse devices, applications, services, and hardware components across computers, network switches, and storage systems. It efficiently communicates the gathered metrics to any platform compatible with OpenTelemetry.

# Overview

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

**${solutionName}** is a universal metrics collector for OpenTelemetry, allowing customization for monitoring diverse technologies, encompassing applications, servers, and devices, particularly those without readily available monitoring solutions. It natively reports the power consumption, electricity costs and CO₂ emissions of the monitored resources to support SRE initiatives.

The **enterprise edition** includes the capability to monitor the hardware health of various resources including computers, network switches, and storage systems from nearly any vendor.

The agent exposes tailored metrics to any observability platform supporting [OpenTelemetry](https://opentelemetry.io/docs), including [Datadog](https://www.datadoghq.com), [BMC Helix](https://www.bmc.com/it-solutions/bmc-helix.html), [Prometheus](https://prometheus.io), Splunk, and more.


![**${solutionName}** Architecture](./images/architecture.png)

**${solutionName}** is available in 2 editions:

* **Community edition**: it includes fundamental features of the solution and connectors contributed by the community members.
The MetricsHub foundation enables the monitoring of operating systems (Windows, Linux, etc.) and collecting metrics exposed by the [**Community Connectors**](#Community_Connectors) such as MIB2 or LibreHardwareMonitor.
Additionally, the MetricsHub empowers users to create directives specifying how to monitor any resource with minimal configuration, involving low-code principles.

* **Enterprise edition**: In addition to the features included in the community edition, the enterprise edition includes the library of 250+ *Hardware Connectors*, **${solutionName}** becomes capable of performing the monitoring of almost any system from any manufacturer (**Cisco**, **Dell**, **EMC**, **Fujitsu**, **Hitachi**, **HP**, **IBM**, **Lenovo**, **NetApp**, **Oracle**, etc.) through `SNMP`, `IPMI`, `HTTP`, `WBEM`, `WMI`, `WINRM` or `SSH`.


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

## What's in the box?

**${solutionName}** is comprised of:

* The **MetricsHub Agent**
    * Its [configuration file](configuration/configure-agent.md) (`metricshub.yaml`)
    * The monitoring engine, with the [**Community Connectors**](#Community_Connectors): Windows, Linux, MIB2, Libre Hardware Monitor, HyperV, etc.
* The *OpenTelemetry Collector Contrib* executable
    * The *OpenTelemetry* [configuration file](configuration/configure-otel.md) (`otel-config.yaml`)
* MetricsHub CLI (`metricshub`)

> Note: The **Enterprise Edition** consists of Hardware connectors

![Internal architecture of the ${solutionName}](images/metricshub-internal-architecture.png)

> Note: **${solutionName}** is shipped with Java Runtime Environment 17.

### MetricsHub Agent

The **MetricsHub Agent** is the engine that performs the actual monitoring of the systems, based on its [configuration file](configuration/configure-agent.md), which specifies:

* Its internal polling cycle
* The hostnames and credentials of the resources to monitor.

The **MetricsHub Agent** is responsible for:

1. Spawning the **OpenTelemetry Collector Contrib**
2. Scraping resources
3. Collecting metrics
4. Pushing OTLP data to the OTLP receiver of the OpenTelemetry Collector.

### OpenTelemetry Collector Contrib

The *OpenTelemetry Collector Contrib* is responsible for:

1. Pulling its metrics periodically (internally)
2. Pushing these metrics to the specified platform.

### Community Connectors

By default, **${solutionName}** incorporates the [**Community Connectors**](./metricshub-connector-reference.html), these connectors are compatible with diverse [operating systems and platforms](./platform-requirements.html), including Windows, Linux, VMware ESX, HyperV, and more. The ${solutionName} Community Connectors is an open-source project featuring connectors developed by community contributors. Depending on your requirements, you can submit enhancement requests on the [MetricsHub Community Connectors Repository](https://github.com/sentrysoftware/metricshub-community-connectors) or actively contribute to the creation of new connectors, allowing the entire community to reap the benefits.

### Enterprise edition (Hardware Connector Library)

In the enterprise edition, the library of **250+** *Hardware Connectors* is provided as zip asset to be included in the **${solutionName}** installation. It is the same library that powers [Hardware Sentry KM for PATROL](https://www.sentrysoftware.com/products/km-hardware-sentry.html), the original and battle-seasoned module for PATROL, created in 2004 by Sentry Software and used on hundreds of thousands of systems around the world.

The platforms that can be monitored by **${solutionName}** entirely depends on this library. More details about the *Hardware Connector Library* are available as a [separate documentation](https://www.sentrysoftware.com/docs/hardware-connectors/latest/index.html).

### MetricsHub CLI (`metricshub`)

The [MetricsHub CLI](troubleshooting/cli.md) contains the same engine and library as *MetricsHub Agent*, but packaged as a command line interface, that can be invoked in a shell.

This tool is particularly useful to troubleshoot the monitoring of a system, protocols and credentials.
