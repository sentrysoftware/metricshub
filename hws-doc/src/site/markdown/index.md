keywords: about
description: ${solutionName} is a distribution of the OpenTelemetry Collector that is able to monitor the health of the hardware of any sort of computer, network switch or storage system, and expose the corresponding metrics to any OpenTelemetry-compatible platform.

# Overview

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

**${solutionName}** is a hardware monitoring agent which monitors the health of the hardware of any kind of computer, network switch or storage system, and exposes the corresponding metrics to any observability platform supporting [OpenTelemetry](https://opentelemetry.io/docs) like [Datadog](./integration/datadog.md), [BMC Helix](./integration/helix.md), [Prometheus](./prometheus/prometheus.md), Splunk, etc.

![**${solutionName}** Architecture](./images/otel-architecture.png)

Thanks to its internal library of 250+ *Hardware Connectors*, **${solutionName}** can monitor almost any system from any manufacturer (**Cisco**, **Dell**, **EMC**, **Fujitsu**, **Hitachi**, **HP**, **IBM**, **Lenovo**, **NetApp**, **Oracle**, etc.) through `SNMP`, `IPMI`, `HTTP`, `WBEM`, `WMI`, `WINRM` or `SSH`.

**${solutionName}** monitors the health of the internal electronic components of the system:

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

**${solutionName}** also reports the energy usage and costs, as well as the carbon emissions of the monitored systems.

## What's in the box?

**${solutionName}** is comprised of:

* The **Hardware Sentry Agent**
  * Its [configuration file](configuration/configure-agent.md) (`hws-config.yaml`)
  * The monitoring engine, with the **Hardware Connector Library**
* The *OpenTelemetry Collector Contrib* executable
  * The *OpenTelemetry* [configuration file](configuration/configure-otel.md) (`otel-config.yaml`)
* Hardware Sentry CLI (`hws`)

![Internal architecture of the ${solutionName}](images/hws-internal-architecture.png)

### Hardware Sentry Agent

The **Hardware Sentry Agent** is the engine that performs the actual monitoring of the systems, based on its [configuration file](configuration/configure-agent.md), which specifies:

* Its internal polling cycle
* The hostnames and credentials of the systems to monitor.

The **Hardware Sentry Agent** is responsible for:

1. Spawning the **OpenTelemetry Collector Contrib**
2. Scraping hosts
3. Collecting metrics
4. Pushing OTLP data to the OTLP receiver of the OpenTelemetry Collector.

### OpenTelemetry Collector Contrib

The *OpenTelemetry Collector Contrib* is responsible for:

1. Pulling its metrics periodically (internally)
2. Pushing these metrics to the specified platform.

### Hardware Connector Library

The library of **250+** *Hardware Connectors* is included in the **Hardware Sentry Agent**. It is the same library that powers [Hardware Sentry KM for PATROL](https://www.sentrysoftware.com/products/km-hardware-sentry.html), the original and battle-seasoned module for PATROL, created in 2004 by Sentry Software and used on hundreds of thousands of systems around the world.

The platforms that can be monitored by **${solutionName}** entirely depends on this library. More details about the *Hardware Connector Library* are available as a [separate documentation](https://www.sentrysoftware.com/docs/hardware-connectors/latest/index.html).

### Hardware Sentry CLI (`hws`)

The [Hardware Sentry CLI](troubleshooting/cli.md) contains the same engine and library of *Hardware Connectors* as *Hardware Sentry Agent*, but packaged as a command line interface, that can be invoked in a shell.

This tool is particularly useful to troubleshoot the monitoring of a system, protocols and credentials.