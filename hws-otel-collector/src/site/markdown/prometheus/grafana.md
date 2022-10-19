keywords: grafana, dashboard, green, power consumption, carbon, CO₂
description: How to import, configure, and use Hardware Sentry Dashboards for Grafana.

# Grafana Dashboards

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

The **Hardware Dashboards for Grafana** give you immediate visibility into your monitored environment. The organized panels expose health metrics for all monitored hardware systems and bring real-time metrics and projected trends on electricity consumption and costs, as well as CO₂ emissions for your entire infrastructure.
Once you have installed and loaded the dashboards, they are automatically available from the **Dashboard** menu on the **Home** page.

![Hardware Sentry - Main View](../images/grafana-main.png)

## Prerequisites

Before using the **Hardware Dashboards for Grafana**, you must have:

1. configured the [Hardware Sentry Agent](../configuration/configure-agent.html)
2. configured the [Prometheus Server](prometheus.md)
3. run both **${project.name}** and the **Prometheus server**.

## Preparing for the installation

Download from [Sentry Software’s Web site](https://www.sentrysoftware.com/downloads/products-for-opentelemetry.html), the package of the **Hardware Dashboards for Grafana** compatible with your version of Grafana:

| **Grafana** | **Hardware Dashboards for Grafana** |
| ----------- | ----------------------------------- |
| v9.x.x      | v4                                  |
| v8.5        | v2                                  |

## Upgrading from v2

If you are using a version older than v4, first delete the following folders on the Grafana server:

- `provisioning`: this folder is generally located in `C:\Program Files\GrafanaLabs\grafana\conf` on Windows, `/etc/grafana` on Linux
- `Hardware Sentry`: this folder is generally located in `C:\Program Files\GrafanaLabs\grafana\public\dashboards` on Windows, `/var/lib/grafana/dashboards` on Linux.

Then log on to Grafana, go to **Dashboards > Browse** and delete the **Sustainable_IT** dashboard.

Restart the Grafana server before installing the dashboards.

## Installing the dashboards

### On Windows

1. Uncompress **hardware-dashboards-for-grafana.zip** in a temporary folder.
2. Copy the `.json` files in the directory of your choice on the Grafana server (ex: `C:\Program Files\GrafanaLabs\grafana\public\dashboards`).

### On Linux and UNIX

1. Uncompress **hardware-dashboards-for-grafana.tar.gz** in a temporary folder.
2. Copy the `.json` files in the directory of your choice on the Grafana server (ex: `/var/lib/grafana/dashboards`).

## Loading the dashboards in Grafana

1. Log on to Grafana
2. Under the **Dashboards** icon, click **Browse** first, then **Import**
3. Click **Upload JSON File**
4. Browse to the folder containing the **Hardware Sentry - Main**, **Hardware Sentry - Host**, and **Hardware Sentry - Site** files. Select one of them
5. Select the **Prometheus** datasource, and click **Load**
6. Repeat the procedure for the other 2 files.

The following dashboards are now loaded in Grafana:

| Dashboard                  | Description                                                                                 |
| -------------------------- | ------------------------------------------------------------------------------------------- |
| **Hardware Sentry - Main** | Overview of all monitored hosts                                                             |
| **Hardware Sentry - Site** | Metrics associated to one _site_ (a data center or a server room) and its monitored _hosts_ |
| **Hardware Sentry - Host** | Metrics associated to one _host_ and its internal devices                                   |

## Using Hardware Sentry Dashboards

### Verifying that your IT infrastructure is fully monitored

The **Coverage** panel available in the **Overall Information** section of the **Hardware Sentry - Main** dashboard indicates the percentage of hosts that are actually monitored.

![Grafana Dashboards - Monitoring Coverage](../images/grafana-main-coverage.png)

A **host** is considered as _not monitored_ if no connectors match the configured system. If the value displayed is below 100%, open each **Site** to identify the hosts for which no data is available. Then access each host page and check the status of the configured **Protocol(s)** and matching **Connector(s)**:

![Grafana Dashboards - Protocol and connector status](../images/grafana-host-protocol-and-connector-status.png)

If their status is not OK, open the corresponding **config/hws-config.yaml** file and verify the host configuration. If you manually specified the connectors to be used, check the `selectedConnectors` and `excludedConnectors` parameters (See [Configuring the Hardware Sentry Agent](../configuration/configure-agent.html)).

### Monitoring the agent collection status

The **Hardware Sentry Agent Status** panel, at the bottom of the **Hardware Sentry - Main** dashboard, lists all the agents configured to collect data, by sites. This panel enables you to view the agents' hostnames, and verify that the agent and connector versions are up-to-date.
The **Last Seen** column indicates the last time an agent was seen during the past 6 hours. An agent going undetected for more than 2 minutes may indicate a potential problem with the host, the connection or the agent configuration.

![Verifying Hardware Sentry Agent Status](../images/grafana-main-agent-status-collect.png)

### Observing the hardware health of the monitored hosts

The **Hardware Sentry - Host** dashboard provides the essential hardware health and sustainability data available for the monitored host:

* the status of its internal components
* the network traffic
* the storage usage
* the power consumption and related carbon emissions
* the temperature information
* etc.

![Grafana Dashboards - Hardware health of the monitored host](../images/grafana-host.png)

Information about the monitoring itself (host information, connectors used, etc.) is provided in the **Monitoring Information** panel.

![Grafana Dashboards - Host monitoring information](../images/grafana-host-monitoring-information.png)

### Detecting hardware failures

The **Hardware Sentry - Main** and **Hardware Sentry - Site** dashboards provide the number of **Critical Alerts** and **Warning Alerts** detected by **${project.name}**. Additional information about the **Current Alerts** is provided in the **Hardware Sentry - Main** and **Hardware Sentry - Host** dashboards.

![Grafana Dashboards - Number of critical and warning alerts](../images/grafana-main-alerts.png)

### Reporting the energy usage and carbon emissions of your infrastructure

The **Power, Cost, and CO₂ emissions** section of the **Hardware Sentry - Main** dashboard reports how much kWh your infrastructure consumes daily, monthly, and yearly, the associated cost and carbon emissions.

![Grafana - Reporting the energy usage and carbon emissions](../images/grafana-main-power-cost-carbon-emissions.png)

The accuracy of this information is provided with the **Margin of Error**. A lower **Margin of Error** means a more accurate estimation.

### Spotting the top consumer sites

You can spot the top consumer server rooms in the **Sites** section of the **Hardware Sentry - Main** dashboard by referring to the **Total Power** column. This column displays the total power consumption of all hosts in a site.

![Grafana - Spotting the top consumer data centers](../images/grafana-main-top-consumer-datacenters.png)

Once you have identified the top consumer sites, you can scroll down to the **Top Consumers** panel available in the **Power and Host Information** section to find out the top ten hosts that consume the most electricity across your entire infrastructure.

![Grafana - Identifying the top consumer hosts](../images/grafana-main-top-consumer-hosts.png)

> Note: The data is automatically refreshed according to the defined collection interval (By default: 2m).

This information can help you implement the best strategy for efficiency, such as replacing older equipment with more efficient ones or moving servers to a site with a lower electricity cost.

Click on one of the histogram's bar to drill down to the host level and get detailed information about a specific device.

### Optimizing the ambient temperature in the data center

Data centers are energy-intensive facilities. This energy is converted into heat that must be dissipated away from the equipment racks to maintain an optimal room temperature. Therefore, the hardware devices' temperature is a critical parameter that must be closely monitored.

In most data centers, the air conditioning system ensures the entire room’s ambient temperature is maintained at 65 degrees Fahrenheit (18 degrees Celsius), which is generally unnecessarily low to avoid overheating problems. Computer systems can safely operate with an ambient temperature significantly higher (see Google’s example, where they raised the temperature of their data centers to 80°F, i.e. 26.7°C). This is the fastest and cheapest method to reduce the energy consumed by a data center and improve its PUE.

From the **Sites** section of the **Hardware Sentry - Main** dashboard, refer to the **Ambient Temperature** column to spot the warmer sites. From cold blue to warm red, the color code helps you rapidly identify the sites where the overall temperature can be optimized.

![Grafana - Monitoring the Ambient Temperature](../images/grafana-main-ambient-temperature.png)

The **Heating Margin** column exposes the number of degrees Celsius (°C) remaining before the temperature reaches the closest warning temperature threshold of a monitored host for each monitored site.

> Note: Some hardware devices do not expose their overall temperature and are therefore not included in the heating margin computation. **${project.name}** provides the percentage of the **Monitoring Confidence** per site in the **Site** dashboard.

Scroll down to the **Hosts Temperatures** section to assess the ambient temperature per host and identify the hosts that report the highest temperature.

![Grafana - Monitoring the Hosts Temperature](../images/grafana-main-hosts-temp.png)

These indicators can help you optimize your overall data center's ambient temperature by acting at the site or the host's level. Increasing the ambient temperature in a facility by 1 degree Celsius can lower your electricity consumption and reduce your carbon emission by 5%, based on an average PUE of 1.80 (default).

### Estimating potential savings per site

The **Site Temperature Optimization** panel exposes detailed information about the heating margin for a specific site, including the temperature collected by hosts. This panel is particularly interesting to estimate the savings you could make if you increase the temperature of your facilities to the **Recommended Site Temperature** and how you can significantly reduce the carbon footprint of a site.

![Optimizing a Site Temperature](../images/grafana-site-monitoring-confidence.png)

Note that the accuracy of the estimated values increases proportionally with the **Monitoring Confidence** percentage. This percentage is based on the number of hosts reporting temperature readings. The higher the number of hosts reporting temperature readings, the higher the confidence.
