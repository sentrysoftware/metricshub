keywords: prometheus, grafana, configuration, dashboards
description: How to integrate Hardware Sentry with Prometheus and Grafana.

# Quick start to Prometheus and Grafana integration

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

**${project.name}** leverages several protocols (`SNMP`, `IPMI`, `HTTP`, `WBEM`, `WMI`, `SSH`, etc.) to gather hardware health and sustainability information and push the related metrics into Prometheus. The information can be viewed in Grafana using the prebuilt **Hardware Observability and Sustainability** dashboards.

![Archirecture diagram](../images/hws_quick_start_architecture_diagram.png)

In this quick start guide, you will learn how to monitor several hosts and bring hardware observability and sustainability metrics in Prometheus and Grafana in a few quick steps.

![Quick start to Prometheus integration](../images/hws-prometheus-grafana-quick-start-steps.png)

## Prerequisites

1. Prometheus Server installed and started with the `--web.enable-remote-write-receiver` option
2. Grafana installed

## Step 1: [Install Hardware Sentry OpenTelemetry Collector](../install.html)

## Step 2: Push metrics to Prometheus Server

Copy the **config/otel-config-example.yaml** file and rename it **otel-config.yaml**.

In the **config/otel-config.yaml** file, locate the `prometheusremotewrite/your-server:` section and specify the remote write URL (`endpoint`):

```yaml
  prometheusremotewrite/your-server:
    endpoint: http://localhost:9090/api/v1/write
    resource_to_telemetry_conversion:
      enabled: true
```

then, add the `exporter` `prometheusremotewrite/your-server` to the pipeline:

```yaml
  pipelines:
    metrics:
      receivers: [otlp, prometheus/internal]
      processors: [memory_limiter, batch, resourcedetection, metricstransform]
      exporters: [prometheusremotewrite/your-server, prometheus, …] 
```

## Step 3: Specify sites (group of hosts)

Copy the **config/hws-config-example.yaml** file and rename it **hws-config.yaml**.

In the **config/hws-config.yaml** file, change `Datacenter 1` with the actual name of your datacenter or site (ex: Paris, New York, etc.)  

```yaml
extraLabels:
  site: Datacenter 1
```

## Step 4: Configure targets (hosts)

You can monitor as many hosts as required using either `IPMI`, `HTTP`, `WBEM`, `WMI`, `SSH`, or `SNMP`. Several examples are provided in the **config/hws-config-example.yaml** file. See below an example of how to monitor one host via `SNMP`.

In the **config/hws-config.yaml** file, copy these lines:

```yaml
- target:
    hostname: ecs1-01
    type: linux
  snmp:
    version: v1
    port: 161
    timeout: 120
```

and replace:

* `ecs1-01` with the name or IP address of one host belonging to the site previously configured
* `linux` with the host type. Refer to [Monitored Target](../configuration/configure-agent.html#Monitored_Targets) for more details.

## Step 5: Verify that metrics are stored in Prometheus Server

Open your Prometheus server (typically http://localhost:9090/graph) and type `hw` in the *Search* field to display the list of collected Hardware Sentry metrics:

![Verifying that metrics are stored in Prometheus Server](../images/hws_quick_start_check_list-metrics.png)

## Step 6: Import dashboards in Grafana

1. [Load Dashboards in Grafana](./integration/grafana.html#Loading_Dashboards_in_Grafana)
2. [Configure the Dashboard provider](./integration/grafana.html#Configuring_the_Dashboard_Provider)
3. [Configure the Data Source](./integration/grafana.html#Configuring_the_Data_Source)

## Step 7: View data in Grafana

Open Grafana (typically on http://localhost:3000/) and browse for the [MAIN] dashboard.

![**${project.name}** Sustainable IT Dashboard](../images/grafana-sustainable-it.png)