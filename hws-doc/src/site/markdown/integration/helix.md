keywords: configuration, helix, bmc
description: How to integrate the hardware metrics collected by **${solutionName}** into BMC Helix Operations Management

# Integration with BMC Helix Operations Management

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

**${solutionName}** easily integrates with BMC Helix Operations Management to expose hardware health, performance metrics, and sustainability indicators into Helix Dashboards. The integration is achieved by using the standard Prometheus *Remote Write* protocol, which can be ingested by the BMC Helix platform.

![${solutionName} integration with BMC Helix](../images/helix-architecture.png)

## Prerequisites

1. [Download](https://www.sentrysoftware.com/downloads/products-for-opentelemetry.html#hardware-sentry-opentelemetry-collector-1-0-00), [install](../install.html) and [configure](../configuration/configure-otel.html) ${solutionName}
2. [Configure](#Configuration) the monitoring of the host(s)
3. Verify that the metrics are stored in the Prometheus Server
4. [Download](#Dashboards) the latest version of hardware-dashboards-for-helix.zip or hardware-dashboards-for-helix.tar.gz from the Sentry Software's'Web site.
5. [Import](https://docs.bmc.com/docs/helixdashboards/223/sharing-and-importing-dashboards-1102359494.html?src=search) Hardware Sentry dashboards into BMC Helix.

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

**${solutionName}** comes with a set of 3 dashboards that expose health, performance and sustainability metrics and indicators:

* **Main**: Global hardware and sustainability information for your entire monitored infrastructure
* **Site**: Deep insight into a specific site (server room)
* **Host**: Detailed metrics for a host and its internal components

Dashboards are organized into panels exposing systems' health and performance metrics, real-time and projected data on energy consumption and costs, as well as carbon emissions for your monitored infrastructure.

Access the **${solutionName}** dashboards from the **BMC Helix Dashboards Home Page** or browse the **Dashboards** menu and select the **${solutionName}** folder.

![Helix Dashboards - Accessing Hardware Sentry dashboards](../images/helix-dashboards-folder.png)

### Understanding the monitoring coverage of your IT infrastructure

The ability to monitor your entire IT infrastructure is essential if you want to prevent or fix performance issues before they affect your customers and business. Reaching 100% coverage is a legitimate goal. The **Coverage** panels of the **Hardware Sentry - Main** and **Hardware Sentry - Site** dashboards expose the percentage of hosts that are actually monitored.

A coverage percentage below 100% indicates that some hosts are not monitored adequatly. In such a case, you can:

* Review your hosts configuration in the **config/hws-config.yaml** file.
* Check the **Hardware Sentry Agents Status** panel at the bottom of the **Main** dashboard to make sure that the **Hardware Sentry Agents** responsible for collecting data are fully operational.
* Open each **Site** dashboard to spot the hosts with no collected data. Then, from the host page, verify the configured **Protocol(s)** status and matching **Connector(s)**. If their status is *Down* or *Failed*, open the **config/hws-config.yaml** file and verify the host configuration. If you have manually defined the connectors, check the `selectedConnectors` and `excludedConnectors` parameters (See [Configuring the Hardware Sentry Agent](../configuration/configure-agent.html)).

> Note: A low coverage value will automatically increase the *Margin of Error*, minimizing the accuracy of the estimated trends reported in the **Power, Costs, and CO2 Emissions** panel.

### Verifying the agent collection status

An agent going undetected for more than 2 minutes may indicate a potential problem with the host, the connection, or the agent configuration.

The **Hardware Sentry Agent Status** panel at the bottom of the **Hardware Sentry - Main** dashboard lists all the agents configured to collect data by sites. It displays the agents' hostnames and indicates if the agent and connector versions are up-to-date.

The **Last Seen** column indicates the last time an agent was seen over the past 6 hours.

### Monitoring energy usage and carbon emissions

In addition to detailed operational data, **${solutionName}** dashboards expose unified, accurate, and continuous views of measured power consumption, energy costs, and carbon emissions. These indicators allow IT administrators to assess the current situation, identify excessive energy usage or other inefficiencies, and take action to gain in efficiency.

The **Power, Cost, and CO₂ emissions** section of the **Hardware Sentry - Main** and **Hardware Sentry - Site** dashboards display live, and historical metrics about the kWh your infrastructure consumes daily, monthly, and yearly as well as the associated costs and carbon emission levels.

![Helix Dashboards - Monitoring energy usage and carbon emissions](../images/helix-power-costs-CO2emissions-main.png)

The **Margin of Error** panel indicates the level of confidence in the estimated values. The lower the **Margin of Error**, the more accurate the estimate.

**${solutionName}** also reports the power consumption, energy costs, and the CO₂ emissions of each monitored host in the corresponding **Hardware Sentry - Host** dashboard.

![Helix Dashboards - Monitoring energy usage and carbon emissions](../images/helix-power-costs-CO2emissions-host.png)

The **Power per Device Type** panel shows an estimation of the energy consumed by the internal components, by type, of the monitored host.

### Identifying the top consumer sites

High electricity consumption in a server room (site), the resulting costs, and CO2 emission levels can be multifactorial: a large number of servers, servers that no longer meet high-efficiency standards, unused and not decommissioned servers, etc.
Hardware Sentry can help you identify the most energy-intensive sites to reduce your electricity bills and carbon emissions.

The **Total Power** column of the **Sites** section in the **Hardware Sentry - Main** dashboard displays the total power consumption of the monitored hosts by site.
The sites with the higher **Total Power** are the most energy-consuming.

![Helix Dashboards - Identifying the top consumer sites](../images/helix-total-power-per-site.png)

Scroll down to the **Top Consumers** panel available in the **Power and Host Information** section. The **Top Consumers** panel lists the top ten most energy-intensive hosts in your entire infrastructure.

![Helix Dashboards - Identifying the top consumer sites](../images/helix-top-consumer-sites.png)

> Note: Metrics are automatically updated according to the collection interval (By default: 2m).

Replacing older equipment with more efficient ones or moving servers to a site with a lower electricity cost can help you improve your sustainability goals and reduce your energy costs.

Click the histogram bar of a host to view detailed information about a specific system.

### Optimizing a site's temperature

Data centers are energy-intensive facilities. This energy is converted into heat that must be dissipated away from the equipment racks to maintain an optimal room temperature. Therefore, the hardware devices' temperature is a critical parameter that must be closely monitored.

The **Heating Margin** panel exposes the number of degrees Celsius (°C) remaining before the temperature reaches the closest warning temperature threshold of a monitored host for each site.

> Note: Some hardware devices do not expose their overall temperature and are, therefore, not included in the heating margin computation. **${solutionName}** provides the percentage of the **Monitoring Confidence** per site in the **Site** dashboard.

![Helix Dashboards - Viewing potential savings](../images/helix-site-temperature-optimization.png)

Combining the temperature indicators collected from each monitored server and component, **${solutionName}** calculates the **Recommended Site Temperature** that determines the temperature at which the site can safely operate. Increasing a server room temperature can generate substantial savings on your electricity bill (**Potential Yearly Savings**) and significantly reduce your facility's carbon emissions ( **Potential Yearly CO2 Reduction**).

![Helix Dashboards - Viewing potential savings](../images/helix-power-CO2emissions-savings.png)