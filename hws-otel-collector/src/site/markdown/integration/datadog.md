keywords: datadog, integration
description: How to integrate ${project.name} into Datadog.

# Datadog Integration

**Hardware Sentry** is made available through the Datadog marketplace. It comes with a set of dashboards that leverage the metrics collected by **[Hardware Sentry OpenTelemetry Collector](https://www.sentrysoftware.com/products/hardware-sentry-opentelemetry-collector.html)**:

| Dashboard                                        | Description                                                                                               |
| ------------------------------------------------ | --------------------------------------------------------------------------------------------------------- |
| Hardware Sentry - Observability & Sustainability | Overview of all monitored hosts, with a focus on sustainability                                           |
| Hardware Sentry - Site                           | Metrics associated to one _site_ (a data center or a server room) and its monitored _hosts_               |
| Hardware Sentry - Host                           | Metrics associated to one _host_ and its internal devices                                                 |
| Hardware Sentry - _\<Device\>_                   | Metrics specific to a particular class of devices (CPU, memory, disk, network, etc.) in a specific _host_ |

![${project.name} integration with Datadog](../images/hws-datadog-integration-architecture-diagram.png)

## Prerequisites

1. Susbcribe to **Hardware Sentry** from the [Datadog Marketplace](https://app.datadoghq.com/marketplace)
2. [Install Hardware Sentry OpenTelemetry Collector](./install.html) on a system that has network access to the physical servers, switches and storage systems you need to monitor
3. [Configure the hosts to be monitored](../configuration/configure-agent.html)

## Configuring the integration

Edit the [`config/otel-config.yaml` configuration file](./configuration/configure-otel.html) as follows to push metrics to Datadog:

   ```yaml
   exporters:
     # Datadog
     # <https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/datadogexporter>
     datadog/api:
       api:
         key: <apikey> # Check your Datadog organization's settings
         # site: datadoghq.eu # Uncomment for Europe only
       metrics:
         resource_attributes_as_tags: true # IMPORTANT
   ```

4. Start the collector.
5. Configure the hosts to monitor by [editing `config/hws-config.yaml`](https://www.sentrysoftware.com/docs/hws-otel-collector/latest/configuration/configure-exporter.html).

To report hardware failures in Datadog, use the **Manage Monitors > Create New Monitor** interface to add all monitors listed for **Hardware Sentry** in the _Recommended_ tab.

## Understanding the Dashboards