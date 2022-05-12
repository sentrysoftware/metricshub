keywords: prometheus, grafana, configuration, dashboards
description: How to integrate Hardware Sentry with Prometheus and Grafana.

# Quick start to Prometheus integration

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

This Quick Start guides you through the process of installing and configuring **${project.name}** settings to make it interact with Prometheus and Grafana for visualizing health and sustainability metrics in easy-to-read dashboards.

This guide is intended for people who need to extend the observability capabilities of the Prometheus toolkit to maintain their data center healthy and energy efficient.

You will learn about getting started with Hardware Sentry OpenTelemetry Collector and how to add servers, network switches or storage systems to your monitoring environment and what it looks like in the Hardware Sentry dashboard.

## Principle

**${project.name}** integrates seamlessly with Prometheus (and Grafana) to expose collected health, performance, and sustainability metrics to Prometheus Server.

### Hardware Monitoring

Hardware Sentry detects and even predict failures in processors, memory modules, disks, network cards, controllers, power supplies, fans, temperature sensors, etc.

### Sustainability KPIs Monitoring

In addition to physical health monitoring, Hardware Sentry also reports the energy usage of each monitored system. Combined with metrics representing the electricity cost and the carbon density, the provided dashboards report the electricity usage of your infrastructure in kWh and its carbon footprint in tons of CO₂.

100% Software, Hardware Sentry does not require smart PDUs, even for systems that are not equipped with an internal power sensor.

![Archirecture diagram](../images/hws_quick_start_architecture_diagram.png)

## Prerequisites

**${project.name}** requires Java Runtime Environment (JRE) version 11 or higher.

## Installing Hardware Sentry

1. Download the latest version of Hardware Sentry
2. Follow the installation instructions (we recommend installing 1 collector on each site, i.e. each data center, each server room, etc.).

## Configuring the OpenTelemetry Collector

This step is required to specify how and where the OpenTelemetry Collector should send the collected data.

1. Open the **${project.name}** configuration file `config/otel-config.yaml` to integrate **${project.name}** with Prometheus Server.
2. Follow the [configuration instructions](../configuration/configure-otel.html).

## Configuring Hosts monitoring

The monitoring of systems is performed by the internal Hardware Sentry Agent. The Hardware Sentry Agent is the internal component responsible for scraping targets, collecting metrics, and pushing OTLP data to the OTLP receiver of the OpenTelemetry Collector.

1. Open the Hardware Sentry Agent configuration file `config/hws-config.yaml` to specify the internal polling cycle and the hostnames and credentials of the systems to monitor.

    ![Adding hosts to the configuration file](../images/hws_quick_start_config_target.png)

1. Specify the `<hostname>`: name of the target, or its IP address
2. Provide the `<target-type>`. Refer to the Hardware Sentry Agent documentation for details.
3. Save your changes.

## Verifying that metrics are stored in Prometheus Server

In the *Search* field of your Prometheus Server, type `hw`, which is the prefix of all Hardware Sentry metrics. You should see the list of collected metrics:

![Verifying that metrics are stored in Prometheus Server](../images/hws_quick_start_check_list-metrics.png)

## Import Grafana Dashboards

Combined with **${project.name}**, Grafana displays collected metrics stored in Prometheus Server. Sentry Software provides pre-built Sustainable IT dashboards that leverage these metrics to report on the health of the hardware of the monitored systems, and on the carbon emissions of these systems.

1. Download the Grafana Dashboard package and install it with the Grafana installer file. Refer to the [Grafana documentation](https://grafana.com/search/?term=install&type=dashboard%2Cdoc&section=Grafana) for details.
2. Go to `%GRAFANA_HOME%\grafana\conf\provisioning\dashboards`.
3. Open the `hardware-sentry.yml` file.
4. Search for the path: `'' parameter`.
5. Specify the path to the folder where you uncompressed the `sustainable_IT` folder and save your changes.
6. Configure the data source. In *\grafana\conf\provisioning\datasource*, open the `hardware-sentry-prometheus.yml` file.
7. Enter the required settings to connect to your Prometheus Server and save your changes. This will create a new data source called **hardware_sentry_prometheus** in Grafana.
8. Restart the Grafana service.
9. The dashboards are now loaded in Grafana.

## Verifying that metrics are exposed in dashboard

Open the Grafana platform in your Web browser and verify that the dashboards are displaying metrics.


