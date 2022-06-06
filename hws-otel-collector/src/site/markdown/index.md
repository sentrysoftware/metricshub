keywords: about
description: ${project.name} is a distribution of the OpenTelemetry Collector that is able to monitor the health of the hardware of any sort of computer, network switch or storage system, and expose the corresponding metrics to any OpenTelemetry-compatible platform.

# Overview

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

## What is **${project.name}**?

**${project.name}** is a *[distribution](https://opentelemetry.io/docs/concepts/distributions/)* of the [OpenTelemetry Collector](https://opentelemetry.io/docs/collector/) to monitor the health of the hardware of any sort of computer, network switch or storage system, and expose the corresponding metrics to any *OpenTelemetry*-compatible platform: [Prometheus](https://prometheus.io/), and other [commercial observability platforms](https://opentelemetry.io/vendors/), like [AWS](https://aws-otel.github.io/), [BMC Helix](https://www.bmc.com/it-solutions/bmc-helix-operations-management.html), [Datadog](https://docs.datadoghq.com/tracing/setup_overview/open_standards/), or [New Relic](https://newrelic.com/solutions/opentelemetry), [Splunk](https://www.splunk.com/en_us/blog/conf-splunklive/announcing-native-opentelemetry-support-in-splunk-apm.html).

![**${project.name}** Architecture](./images/otel-architecture.png)

**${project.name}** is able to monitor any system using `SNMP`, `IPMI`, `HTTP`, `WBEM`, `WMI`, `WINRM` or just `SSH`. It can monitor computers, switches and storage systems from **Cisco**, **Dell**, **EMC**, **Fujitsu**, **Hitachi**, **HP**, **IBM**, **Lenovo**, **NetApp**, **Oracle** and many others, thanks to its library of 250+ *Hardware Connectors*.

**${project.name}** monitors the health of the internal electronic components of the system:

* CPUs
* Memory modules
* GPUs
* Network cards
* Sensors
    * Temperatures
    * Voltages
    * Fans
    * Power
* Power supplies
* Storage
    * Controllers
    * Disks
    * RAIDs
    * HBAs
    * LUNs
    * Tape drives

## What's in the box?

**${project.name}** includes:

* a standard *OpenTelemetry Collector*
* Hardware Sentry Agent
* the Hardware Connector Library
* Hardware Sentry CLI (`hws`)

### OpenTelemetry Collector

**${project.name}** is comprised of:

* The *OpenTelemetry Collector* executable
  * The *OpenTelemetry* [configuration file](configuration/configure-otel.md)
  * The **Hardware Sentry Agent**
    * Its [configuration file](configuration/configure-agent.md) (hosts to monitor, credentials, etc.)
    * The monitoring engine, with the **Hardware Connector Library**

![Internal architecture of the ${project.name}](images/otel-internal-architecture.png)

The *OpenTelemetry Collector* is in charge of:

1. Spawning the internal **Hardware Sentry Agent**
2. Pulling its metrics periodically (internally)
3. Pushing these metrics to the specified platform

### Hardware Sentry Agent

The internal **Hardware Sentry Agent** is the engine that performs the actual monitoring of the systems, based on its [configuration file](configuration/configure-agent.md), which specifies:

* its internal polling cycle
* the hostnames and credentials of the systems to monitor

The **Hardware Sentry Agent** is the internal component which is responsible of scraping hosts, collecting metrics and pushing OTLP data to the OTLP receiver of the OpenTelemetry Collector.

### Hardware Connector Library

The library of **250+** *Hardware Connectors* is included in the **Hardware Sentry Agent**. It is the same library that powers [Hardware Sentry KM for PATROL](https://www.sentrysoftware.com/products/km-hardware-sentry.html), the original and battle-seasoned module for PATROL, created in 2004 by Sentry Software, used on hundreds of thousands of systems around the world.

The list of platforms that can be monitored by **${project.name}** entirely depends on this library. More details about the *Hardware Connector Library* are available as a [separate documentation](https://www.sentrysoftware.com/docs/hardware-connectors/latest/index.html).

### Hardware Sentry CLI (`hws`)

The [Hardware Sentry CLI](troubleshooting/cli.md) contains the same engine and library of *Hardware Connectors* as *Hardware Sentry Agent*, but packaged as a command line interface, that can be invoked in a shell.

This tool is particularly useful to troubleshoot the monitoring of a system, protocols and credentials.
