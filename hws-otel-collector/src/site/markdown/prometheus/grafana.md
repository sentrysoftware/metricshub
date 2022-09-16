keywords: grafana, dashboard, observability, sustainability, green, power consumption, carbon, CO₂
description: How to import and configure Hardware Sentry Dashboards for Grafana.

# Grafana Dashboards

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

The **Hardware Sentry** dashboards for Grafana give you immediate visibility into your monitored environment. The organized panels expose health metrics for all monitored hardware systems and bring real-time metrics and projected trends on electricity consumption and costs, as well as CO₂ emissions for your entire infrastructure.
Once you have configured the [dashboard provider](#Configuring_the_Dashboard_Provider) and the [data source](#Configuring_the_Data_Source), the dashboards are automatically available from the **Dashboard** menu on the **Home** page.

<!-- Update screenshot -->

![Hardware Sentry - Main View](../images/grafana-main.png)

## Prerequisites

Before you can start configuring and using **Hardware Sentry** dashboards, you must have:

1. configured the [Hardware Sentry Agent](../configuration/configure-agent.html)
2. configured the [Prometheus Server](prometheus.md)
3. run both **${project.name}** and the **Prometheus server**.

## Configuring the Dashboards

### Loading Dashboards in Grafana

First, download from [Sentry Software’s Web site](https://www.sentrysoftware.com/downloads/products-for-opentelemetry.html), the package of the **Hardware Dashboards for Grafana** compatible with your version of Grafana:

| **Grafana** | **Hardware Dashboards for Grafana** |
| ----------- | ----------------------------------- |
| v8.5        | v2                                  |
| v9.x.x      | v3                                  |

The **hardware-dashboards-for-grafana.zip** and **hardware-dashboards-for-grafana.tar.gz** packages contain:

![Dashboards Package](../images/hardware-dashboards-for-grafana-folders.png)

* the dashboards (.json files)
* the provisioning files (.yml files).

#### On Windows

1. Uncompress **hardware-dashboards-for-grafana.zip** in a temporary folder.
2. Copy the `provisioning` folder to the `grafana\conf` folder on the Grafana server (default: "C:\Program Files\GrafanaLabs\grafana\conf").
3. Copy the `Hardware Sentry` folder in the directory of your choice on the Grafana server (ex: "C:\Program Files\GrafanaLabs\grafana\public\dashboards").

   ![Copying Dashboards on Windows](../images/import-dashboards-windows.png)

#### On Linux and UNIX

1. Uncompress **hardware-dashboards-for-grafana.tar.gz** in a temporary folder.
2. Copy the `provisioning` folder to the `grafana` folder on the Grafana server (default: "/etc/grafana").
3. Copy the `Hardware Sentry` folder in the directory of your choice folder on the Grafana server (ex: "/var/lib/grafana/dashboards").

### Configuring the Dashboard Provider

1. Go to `%GRAFANA_HOME%\grafana\conf\provisioning\dashboards`.
2. Open the `hardware-sentry.yml` file.

   ![Configuring Dashboard Provider](../images/import_grafana_dashboard_provider-config.png)

3. Search for the `path: ''` parameter.
4. Specify the path to the folder where you uncompressed the _Hardware Sentry_ folder and save your changes.

Example:

```yaml
apiVersion: 1

providers:
- name: 'Sentry Software'
    orgId: 1
    folder: 'Hardware Sentry'
    folderUid: ''
    type: file
    updateIntervalSeconds: 60
    allowUiUpdates: true
    options:
    path: 'C:\Program Files\GrafanaLabs\grafana\public\dashboards'
    foldersFromFilesStructure: true
```

<div class="alert alert-warning"> The path should point to the folder containing the <i>Hardware Sentry</i> folder. This folder should only contain dashboards for Grafana.</div>

### Configuring the Data Source

The dashboards for Grafana query the Prometheus server to display the status of the hardware components. A Prometheus data source needs to be configured on the Grafana server.

1. In `\grafana\conf\provisioning\datasources`, open the _hardware-sentry-prometheus.yml_ file.
   ![Configuring Data Source Provider](../images/import_grafana_dashboards_config.png)
2. Enter the required settings to connect to your Prometheus server and save your changes. This will create a new data source called **hardware_sentry_prometheus** in Grafana.
3. Restart the Grafana service.

Example:

```yaml
# config file version
apiVersion: 1

datasources:
  # <string, required> name of the datasource. Required
  - name: hardware_sentry_prometheus
    # <string, required> datasource type. Required
    type: prometheus
    # <string, required> access mode. direct or proxy. Required
    access: proxy
    # <int> org id. will default to orgId 1 if not specified
    orgId: 1
    # <string> url
    url: http://myhost-01:9090
    # <string> database password, if used
    password:
    # <string> database user, if used
    user:
    # <string> database name, if used
    database:
    # <bool> enable/disable basic auth
    basicAuth: false
    # <string> basic auth username, if used
    basicAuthUser:
    # <string> basic auth password, if used
    basicAuthPassword:
    # <bool> enable/disable with credentials headers
    withCredentials:
    # <bool> mark as default datasource. Max one per org
    isDefault: true
    version: 1
    # <bool> allow users to edit datasources from the UI.
    editable: true
```

The following dashboards are now loaded in Grafana:

| Dashboard  | Description                                                                                 |
| ---------- | ------------------------------------------------------------------------------------------- |
| **[Main]** | Overview of all monitored hosts                                                             |
| **Site**   | Metrics associated to one _site_ (a data center or a server room) and its monitored _hosts_ |
| **Host**   | Metrics associated to one _host_ and its internal devices                                   |

## Using Hardware Sentry Dashboards

### Verifying that your IT infrastructure is fully monitored

The **Hardware Sentry Agent** monitors the hardware health of all the configured systems. A simple configuration issue and a part of your infrastructure is left unsupervised. The **Coverage** panel available in the **Overall information** section of the **Main** dashboard allows you to identify configuration issues.

![Grafana Dashboards - Monitoring Coverage](../images/grafana-main-coverage.png)

If the value displayed is below 100%, check the monitoring coverage of each **Site** to identify which configuration needs to be reviewed. Then open the corresponding **config/hws-config.yaml** file and verify that hosts are properly set.

### Monitoring the agent collection status

The **Hardware Sentry Agent Status** panel at the bottom of the **Main** dashboard, lists all the agents configured to collect data, by sites. This panel enables you to view the agents' hostnames, and verify that the agent and connector versions are up-to-date.
The **Last Seen** column indicates the last time an agent was seen during the past 6 hours. An agent going undetected for more than 2 minutes may indicate a potential problem with the host, the connection or the agent configuration.

![Verifying Hardware Sentry Agent Status](../images/dashboard_main-agent-status-collect.png)

### Observing the hardware health of the monitored hosts

The **Host** dashboard provides the essential hardware health and sustainability data available for the monitored host:

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

The **Main** and **Site** dashboards provide the number of **Critical Alerts** and **Warning Alerts** detected by **${project.name}**. Additional information about the **Current Alerts** is provided in the **Main** and **Host** dashboards.

In the example below, we can see that the two critical issues detected concern the charge level of the X2-BBU (Eaton 5P 1550) battery.

![Grafana Dashboards - Number of critical and warning alerts](../images/grafana-alerts.png)

### Reporting the energy usage and carbon emissions of your infrastructure

The **Power, Cost, and CO₂ emissions** section of the **Main** dashboard reports how much kWh your infrastructure consumes daily, monthly, and yearly,  the associated cost, as well as carbon footprint.

![Grafana - Reporting the energy usage and carbon emissions](../images/grafana-main-power-cost-carbon-emissions.png)

The accuracy of this information is provided with the **Margin of Error**. A lower **Margin of Error** means a more accurate estimation.

### Spotting the top consumer sites

You can spot the top consumer server rooms in the **Sites** section of the **Main** dashboard by referring to the **Total Power** column. This column displays the total power consumption of all hosts in a site.

![Grafana - Spotting the top consumer data centers](../images/grafana-main-top-consumer-datacenters.png)

Once you have identified the top consumer sites, you can scroll down to the **Top Consumers** panel available in the **Power and Host Information** section to find out the top ten hosts that consume the most electricity across your entire infrastructure.

![Grafana - Identifying the top consumer hosts](../images/grafana-main-top-consumer-hosts.png)

> Note: The data is automatically refreshed according to the defined collection interval (By default: 2m).

This information can help you implement the best strategy for efficiency, such as replacing older equipment with more efficient ones or moving servers to a site with a lower electricity cost.

Click on one of the histogram's bar to drill down to the host level and get detailed information about a specific device.

### Optimizing the ambient temperature in the data center

Data centers are energy-intensive facilities. This energy is converted into heat that must be dissipated away from the equipment racks to maintain an optimal room temperature. Therefore, the hardware devices' temperature is a critical parameter that must be closely monitored.

In most data centers, the air conditioning system ensures the entire room’s ambient temperature is maintained at 18 degrees Celsius, which is generally unnecessarily low to avoid overheating problems. Computer systems can safely operate with an ambient temperature significantly higher (see Google’s example, where they raised the temperature of their data centers to 80°F, i.e. 26.7°C). This is the fastest and cheapest method to reduce the energy consumed by a data center and improve its PUE. 

From the **Sites** section of the **Main** dashboard, refer to the **Ambient Temperature** column to spot the warmer sites. From cold blue to warm red, the color code helps you rapidly identify the sites where the overall temperature can be optimized.

![Monitoring the Ambient Temperature](../images/grafana-main-ambient-temp.png)

The **Heating Margin** column exposes the number of degrees Celsius (°C) remaining before the temperature reaches the closest warning temperature threshold of a monitored host for each monitored site.

> Note: Some hardware devices do not expose their overall temperature and are therefore not included in the heating margin computation. **${project.name}** provides the percentage of the **Monitoring Confidence** per site in the **Site** dashboard.

Scroll down to the **Hosts Temperatures** section to assess the ambient temperature per host andidentify the hosts that report the highest temperature.

![Monitoring the Hosts Temperature](../images/grafana-main-hosts-temp.png)

These indicators can help you optimize your overall data center's ambient temperature by acting at the site or the host's level. Increasing the ambient temperature in a facility by 1 degree Celsius can lower your electricity consumption and reduce your carbon emission by 5%, based on an average PUE of 1.80 (default).

### Estimating potential savings per site

The **Site Temperature Optimization** panel exposes detailed information about the heating margin for a specific site, including the temperature collected by hosts. This panel is particularly interesting to estimate the savings you could make if you increase the temperature of your facilities to the **Recommended Site Temperature** and how you can significantly reduce the carbon footprint of a site.

![Optimizing a Site Temperature](../images/grafana-site-monitoring-confidence.png)

Note that the accuracy of the estimated values increases proportionally with the **Monitoring Confidence** percentage. This percentage is based on the number of hosts reporting temperature readings. The higher the number of hosts reporting temperature readings, the higher the confidence.