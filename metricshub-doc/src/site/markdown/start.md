keywords: quick start, application monitoring, hardware, sustainability
description: Short step-by-step instructions to follow for installing and configuring MetricsHub

# Quick Start

This quick start guide walks you through the step-by-step instructions you should complete for integrating **${solutionName}** with supported third-party platforms to visualize health and sustainability metrics in easy-to-read dashboards.

## Step 1: Install MetricsHub

[Install MetricsHub](./install.html) on a system that has network access to the physical servers, switches and storage systems you need to monitor. We recommend to dedicate one instance of the **MetricsHub** to one "site" (i.e. each data center or server room).

## Step 2: Configure the MetricsHub Agent

First, add and define the resources you wish to monitor to the **config/metricshub.yaml** file. Simply provide the [hostname and type](./configuration/configure-agent.html#Monitored_resources) of the resource to be monitored, the [protocols and credentials](./configuration/configure-agent.html#Protocols_and_credentials).

Then, [define sites and sustainability](./configuration/configure-agent.html#Configure_the_sustainability_settings) settings. Monitored resources are grouped into resource groups (sites). The site represents a data center, a server room, or applications and services depending on your IT infrastructure and needs.

## Step 3: Configure the integration with the third-party platform

* [BMC Helix](change_me)
* [Datadog](change_me)
* [Grafana](change_me)
* [Prometheus](change_me)