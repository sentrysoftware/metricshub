keywords: about
description: ${project.name} is a distribution of the OpenTelemetry Collector that is able to monitor the health of the hardware of any sort of computer, network switch or storage system, and expose the corresponding metrics to any OpenTelemetry-compatible platform.

# Overview

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

**${project.name}** is a distribution of the [OpenTelemetry Collector](https://opentelemetry.io/docs/collector/) which monitors the health of the hardware of any kind of computer, network switch or storage system, and exposes the corresponding metrics to any observability platform supporting *OpenTelemetry* like [Datadog](./integration/datadog.md), [BMC Helix](./integration/helix.md), [Prometheus](./prometheus/prometheus.md), Splunk, etc.

![**${project.name}** Architecture](./images/otel-architecture.png)

Thanks to its internal library of 250+ *Hardware Connectors*, **${project.name}** can monitor almost any system from any manufacturer (**Cisco**, **Dell**, **EMC**, **Fujitsu**, **Hitachi**, **HP**, **IBM**, **Lenovo**, **NetApp**, **Oracle**, etc.) through `SNMP`, `IPMI`, `HTTP`, `WBEM`, `WMI`, `WINRM` or `SSH`.

**${project.name}** monitors the health of the internal electronic components of the system:

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

**${project.name}** also reports the energy usage and costs, as well as the carbon emissions of the monitored systems.

## What's in the box?

**${project.name}** includes:

* a standard *OpenTelemetry Collector*
* the Hardware Sentry Agent
* the Hardware Connector Library
* Hardware Sentry CLI (`hws`)

### OpenTelemetry Collector

**${project.name}** is comprised of:

* The *OpenTelemetry Collector* executable
  * The *OpenTelemetry* [configuration file](configuration/configure-otel.md) (`otel-config.yaml`)
  * The **Hardware Sentry Agent**
    * Its [configuration file](configuration/configure-agent.md) (`hws-config.yaml`)
    * The monitoring engine, with the **Hardware Connector Library**

![Internal architecture of the ${project.name}](images/hws-internal-architecture.png)

The *OpenTelemetry Collector* is in charge of:

1. Spawning the internal **Hardware Sentry Agent**
2. Pulling its metrics periodically (internally)
3. Pushing these metrics to the specified platform

### Hardware Sentry Agent

The internal **Hardware Sentry Agent** is the engine that performs the actual monitoring of the systems, based on its [configuration file](configuration/configure-agent.md), which specifies:

* its internal polling cycle
* the hostnames and credentials of the systems to monitor.

The **Hardware Sentry Agent** is the internal component which is responsible of scraping hosts, collecting metrics and pushing OTLP data to the OTLP receiver of the OpenTelemetry Collector.

### Hardware Connector Library

The library of **250+** *Hardware Connectors* is included in the **Hardware Sentry Agent**. It is the same library that powers [Hardware Sentry KM for PATROL](https://www.sentrysoftware.com/products/km-hardware-sentry.html), the original and battle-seasoned module for PATROL, created in 2004 by Sentry Software and used on hundreds of thousands of systems around the world.

The platforms that can be monitored by **${project.name}** entirely depends on this library. More details about the *Hardware Connector Library* are available as a [separate documentation](https://www.sentrysoftware.com/docs/hardware-connectors/latest/index.html).

### Hardware Sentry CLI (`hws`)

The [Hardware Sentry CLI](troubleshooting/cli.md) contains the same engine and library of *Hardware Connectors* as *Hardware Sentry Agent*, but packaged as a command line interface, that can be invoked in a shell.

This tool is particularly useful to troubleshoot the monitoring of a system, protocols and credentials.