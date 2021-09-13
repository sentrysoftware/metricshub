keywords: prerequisites, install
description: How get started with Hardware Sentry Exporter for Prometheus: packages, installation, prerequisites.

# Getting Started

**${project.name}** is designed to collect health and performance metrics from any hardware device. It allies the monitoring performance of more than 250 hardware data connectors with the monitoring expertise of Hardware Sentry. Hardware connectors and Monitors, i.e. any physical or logical system entity that it is possible to monitor, contain all commands, queries, and scripts required to collect health and performance metrics from a hardware device or component. Several connectors can be combined to collect as much information as possible.

![**${project.name}** Architecture](./images/mat_prom_architecture_diagram.png)

**${project.name}** collects metrics from monitored endpoints or targets. Each individual target is an instance that provides metric data in a format Prometheus understands. All the targets dump their respective data in the format Prometheus can read from the /metrics location based on the scrape interval setting. A collection of instances that serve the same purpose is called a job. **${project.name}** exposes hardware metrics in a Prometheus format. A Prometheus server then collects those metrics via HTTP requests and saves them with timestamps in a database.

## Prerequisites

### Install the Java package

**${project.name}** requires Java Runtime Environment (JRE) version 11 or higher to be installed on the server where the ```hardware-sentry-prometheus-<version>.jar``` is running.

To verify the version installed on your system, run the following command:

```batch
java -version
```
You can download the latest version of the Java Runtime Environment from <a href="https://adoptium.net/" target="_blank">Adoptium (formerly AdoptOpenJDK)</a>.

Follow the standard installation procedure provided in the Oracle Java documentation.

### Install the ${project.name} package

Download the ```hardware-sentry-prometheus-<version>.jar``` from the Sentry Software’s Website.