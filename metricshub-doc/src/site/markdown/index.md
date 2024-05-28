keywords: about
description: MetricsHub is a universal metrics collector for OpenTelemetry which extracts metrics from any resource and pushes them to any observability back-end.

# Overview

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

## What is **MetricsHub**?

**MetricsHub** is a universal metrics collector for [OpenTelemetry](https://opentelemetry.io/docs) which **extracts metrics from any resource** - either **locally** or **remotely** - and **pushes the collected data to any observability back-end** supporting OpenTelemetry like Prometheus, New Relic, ServiceNow, Splunk, etc.

## What is **MetricsHub** for?

**MetricsHub** natively collects metrics about:

* Operating systems (typically Windows and Linux)
* A variety of platforms (HyperV, MIB2, LibreHardwareMonitor, and more).

Fully customizable, **MetricsHub**  can also be configured to **cover new use cases in no time**, such as the monitoring of systems or applications not covered out-of-the-box through protocols like `HTTP`, `IPMI`, `SNMP`,`SSH`, `WBEM`, `WinRM`, `WMI`.

Refer to [Community Connector Platforms](../metricshub-connector-reference.html) for the exhaustive list of supported platforms.

## How does **MetricsHub** work?

**MetricsHub** comes with the **MetricsHub Agent**, also called the engine, which performs the monitoring of all the configured resources. It collects and sends metrics to the OTLP receiver of your observability back-end.

**MetricsHub** also comes with a **CLI** (`metricshub`) you can invoke in a shell for troubleshooting the monitoring of a system, protocols, and credentials.

## How will **MetricsHub** be licensed?

**MetricsHub** will be available in two editions: the **Community** and **Enterprise** editions.

The **Community Edition** will be **open-source** and come with **basic monitoring features**. With this edition, you will be able to:

* submit enhancement requests on the [MetricsHub Community Connectors Repository](https://github.com/sentrysoftware/metricshub-community-connectors)
* contribute to the project by [creating new connectors](./develop/index.html).

The **Enterprise Edition** will grant access to a library of 250+ connectors which allows **monitoring the hardware of hundreds of systems** from any manufacturer (**Cisco**, **Dell**, **EMC**, **Fujitsu**, **Hitachi**, **HP**, **IBM**, **Lenovo**, **NetApp**, **Oracle**, etc.) through `HTTP`, `IPMI`, `SSH`, `SNMP`, `WBEM`, `WMI`, or `WINRM`.

This impressive library is the result of 20+ years of a development effort led by [Sentry Software](https://sentrysoftware.com). This library is used in production on hundreds of thousands of systems around the world.