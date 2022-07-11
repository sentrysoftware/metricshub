keywords: quick start, datadog, helix, prometheus, grafana
description: Short step-by-step instruction to follow for installing and configuring Hardware Sentry OpenTelemetry Collector with third-party platforms.

# Quick Start

This quick start guide walks you through the step-by-step instructions you should complete for integrating **${project.name}** with supported third-party platforms to visualize health and sustainability metrics in easy-to-read dashboards.

## Step 1: Install Hardware Sentry OpenTelemetry Collector

[Install Hardware Sentry OpenTelemetry Collector](./install.html) on a system that has network access to the physical servers, switches and storage systems you need to monitor. We recommend to dedicate one instance of the **Hardware Sentry OpenTelemetry Collector** to one "site" (i.e. each data center or server room).

## Step 2: Configure the Hardware Sentry Agent

First, add and define the hosts you wish to monitor to the **config/hws-config.yaml** file. Simply provide the [hostname and type](./configuration/configure-agent.html#Monitored_Hosts) of the host to be monitored, the [connection protocol and credentials](./configuration/configure-agent.html#Protocols_and_Credentials).

Then, [define sites and sustainability](./configuration/configure-agent.html#Site_and_Sustainable_IT_Settings) settings. Monitored hosts must be grouped into sites. You must define at least one site to represent a data center, a server room, or applications and services depending on your IT infrastructure and needs.

## Step 3: Configure the integration with the third-party platform

* [BMC Helix](./integration/helix.html)
* [Datadog](./integration/datadog.html)
* [Grafana](./prometheus/grafana.html)
* [Prometheus](./prometheus/prometheus.html)
