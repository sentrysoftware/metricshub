keywords: configuration, helix, bmc
description: How to integrate the hardware metrics collected by **${solutionName}** into BMC Helix Operations Management

# Integration with BMC Helix Operations Management

**${solutionName}** can easily integrate with BMC Helix Operations Management to expose hardware health and performance metrics into Helix Dashboards. This is achieved by using the standard Prometheus *Remote Write* protocol, which can be ingested by the BMC Helix platform.

![${solutionName} integration with BMC Helix](../images/helix-architecture.png)

## Prerequisites

1. [Install](../install.html) and [configure](../configuration/configure-otel.html) Hardware Sentry OpenTelemetry Collector
2. [Configure](#Configuration) the monitoring of the host(s)
3. Check the metrics are stored in the Prometheus Server
4. [Download](https://www.sentrysoftware.com/downloads/products-for-opentelemetry.html#hardware-sentry-opentelemetry-collector-1-0-00)
5. [Download](#Dashboards) the latest version of hardware-dashboards-for-helix.zip or hardware-dashboards-for-helix.tar.gz from the Sentry Software's'Web site.
6. [Import](https://docs.bmc.com/docs/helixdashboards/223/sharing-and-importing-dashboards-1102359494.html?src=search) Hardware Sentry dashboards into BMC Helix.

## Configuration

Edit the `exporters` section of the [otel/otel-config.yaml](../configuration/configure-otel.md) configuration file as in the below example:

```yaml
  prometheusremotewrite/helix:
    endpoint: https://<your-helix-env>.onbmc.com/metrics-gateway-service/api/v1.0/prometheus
    headers:
      Authorization: Bearer <apiToken>
    resource_to_telemetry_conversion:
      enabled: true
```

where:

* `<your-helix-env>` is the host name of your BMC Helix environment, at **onbmc.com**
* `<apiToken>` is the API Key of your BMC Helix environment
* `resource_to_telemetry_conversion` converts all the resource attributes to metric labels when enabled

To get your API Key, connect to **BMC Helix Operations Management**, go to the **Administration** &gt; **Repository** page, and click on the **Copy API Key** button.

![Copy API Key](../images/helix-api-key.png)

Then, make sure to declare the exporter in the pipeline section of **otel/otel-config.yaml**:

```yaml
service:
  extensions: [health_check]
  pipelines:
    metrics:
      receivers: [prometheus_exec/hws-exporter,prometheus/internal]
      processors: [memory_limiter,batch,metricstransform]
      exporters: [prometheusremotewrite/helix] # Your helix config must be listed here
```
## Using Hardware Sentry dashboards in BMC Helix

**Hardware Sentry** comes with a set of dashboards which leverage the metrics collected by **${project.name}**:

* **Main**: Global hardware and sustainability information for your entire monitored infrastructure  
* **Site**: Deep insight into the health and performance of a specific site (server room)
* **Host**: Detailed metrics for a host and its internal components

Dashboards are organized into panels exposing systems' health metrics, real-time and projected data on energy consumption and costs, as well as carbon emissions for your monitored infrastructure.

Access the **Hardware Sentry** dashboards from the **BMC Helix Dashboards Home Page** or browse the **Dashboards** menu and select the **Hardware Sentry** folder.

### Understanding the monitoring coverage of your IT infrastructure

The **Coverage** panel exposes the percentage of hosts that are actually monitored in your entire infrastructure or in one specific site depending on the dashboard you are viewing (**Main** or **Site**).

A coverage percentage below 100% indicates that some hosts are not monitored adequatly:

* Review your hosts configuration in the **config/hws-config.yaml** file.
* Check the **Hardware Sentry Agents Status** panel at the bottom of the **Main** dashboard make sure that the **Hardware Sentry Agents** responsible for collecting data are fully operational.
* Open each **Site** dashboard to spot the hosts with no collected data. Then, from the host page verify the status of the configured **Protocol(s)** and matching **Connector(s)**. If their status is *Down* or *Failed*, open the **config/hws-config.yaml** file and verify the host configuration. If you have manually defined the connectors, check the `selectedConnectors` and `excludedConnectors` parameters (See [Configuring the Hardware Sentry Agent](../configuration/configure-agent.html)).

Note: A low coverage value will automatically increase the *Margin of Error*, minimizing the accuracy of the data and estimated trends reported in the **Power, Costs and CO2 Emissions** panel.

### Verifying the agent collection status

The **Hardware Sentry Agent Status** panel, at the bottom of the **Hardware Sentry - Main** dashboard, lists all the agents configured to collect data, by sites. It displays the agents' hostnames, and indicates if the agent and connector versions are up-to-date.
The **Last Seen** column indicates the last time an agent was seen over the past 6 hours. An agent going undetected for more than 2 minutes may indicate a potential problem with the host, the connection or the agent configuration.

### Monitoring energy usage and carbon emissions

The **Power, Cost, and CO₂ emissions** section of the **Hardware Sentry - Main** and **Hardware Sentry - Site** dashboards display live and historical metrics about the kWh your infrastructure consumes daily, monthly, and yearly as well as the associated cost and carbon emissions.

----
Screenshot
----

The **Margin of Error** panel indicates the percentage points the reported values could differ from the real values. This value represents the level of confidence in the reported values. Therefore, the lower the **Margin of Error**, the more accurate the estimate.

**${project.name}** also reports the power consumption, energy costs, the CO₂ emissions of each monitored host in the corresponding **Hardware Sentry - Host** dashboard:

----
Screenshot
----

The **Power per Device Type** panel shows an estimation of the power consumed by the internal components, by type, of the monitored host.

### Identifying the top consumer sites

The **Total Power** column of the **Sites** section in the **Hardware Sentry - Main** dashboard displays the total power consumption of the monitored hosts by site.
The sites with the higher **Total Power** are the most energy consuming.

----
Screenshot
----

Scroll down to the **Top Consumers** panel available in the **Power and Host Information** section where the **Top Consumenrs** panel lists the top ten most energy intensive hosts in your entire infrastructure.

----
Screenshot
----

> Note: Metrics are automatically updated according to the collection interval (By default: 2m).

Replacing older equipment with more efficient ones or moving servers to a site with a lower electricity cost can help you improve your sustainability goals and reduce your energy costs.

Click the histogram bar of a host view detailed information about a specific system.

XXXXXXXXXX
