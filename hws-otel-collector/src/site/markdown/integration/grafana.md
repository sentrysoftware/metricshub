keywords: grafana, dashboard, sustainable, sustainability, green
description: How to import and configure Hardware Sentry's Sustainable IT Dashboards for Grafana.

# Grafana Dashboards

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

> **Warning**: The current version of the Grafana dashboards has been designed and tested with Grafana v8.3.6. Note that previous versions of Grafana are not compatible with the built-in dashboards.

[Grafana](https://grafana.com/) can easily display the metrics collected by **${project.name}** and stored in a Prometheus Server. Sentry Software provides pre-built **Observability and Sustainability** dashboards that leverage these metrics to report on the health of the hardware of the monitored systems, and on the carbon emissions of these systems:

![**${project.name}** Sustainable IT Dashboard](../images/grafana-sustainable-it.png)

## Prerequisites

Before you can start configuring and using **${project.name}** dashboards, you must have:

1. configured [Hardware Sentry Agent](../configuration/configure-agent.html).
2. configured the [Prometheus server](../integration/prometheus.html)
3. run both **${project.name}** and the **Prometheus server**.

## Loading Dashboards in Grafana

First, download the latest version of **hardware-dashboards-for-grafana.zip** or **hardware-dashboards-for-grafana.tar.gz** from [Sentry Software’s Web site](https://www.sentrysoftware.com/downloads/products-for-opentelemetry.html). The package contains:

![Dashboards Package](../images/hardware-dashboards-for-grafana-folders.png)

* the dashboards (.json files)
* the provisioning files (.yml files)

### On Windows

1. Uncompress **hardware-dashboards-for-grafana.zip** in a temporary folder.
2. Copy the `provisioning` folder to the `grafana\conf` folder on the Grafana server (default: "C:\Program Files\GrafanaLabs\grafana\conf").
3. Copy the `sustainable_IT` folder in the directory of your choice on the Grafana server (ex: "C:\Program Files\GrafanaLabs\grafana\public\dashboards").

    ![Download Dashboards on Windows](../images/import-dashboards-windows.png)

### On Linux and UNIX

1. Uncompress **hardware-dashboards-for-grafana.tar.gz** in a temporary folder.
2. Copy the `provisioning` folder to the `grafana` folder on the Grafana server (default: "/etc/grafana").
3. Copy the `sustainable_IT` folder in the directory of your choice folder on the Grafana server (ex: "/var/lib/grafana/dashboards").

## Configuring the Dashboard Provider

1. Go to `%GRAFANA_HOME%\grafana\conf\provisioning\dashboards`.
2. Open the `hardware-sentry.yml` file.

    ![Configuring Dashboard Provider](../images/import_grafana_dashboard_provider-config.png)

3. Search for the `path: ''` parameter.
4. Specify the path to the folder where you uncompressed the *sustainable_IT* folder and save your changes.

Example:

```yaml
apiVersion: 1

providers:
- name: 'Sentry Software'
    orgId: 1
    folder: 'Sustainable IT by Sentry Software'
    folderUid: ''
    type: file
    updateIntervalSeconds: 60
    allowUiUpdates: true
    options:
    path: 'C:/Program Files/GrafanaLabs/grafana/public/dashboards'
    foldersFromFilesStructure: true
```

<div class="alert alert-warning"> The path should point to the folder containing the <i>sustainable_IT</i> folder. This folder should only contain dashboards for Grafana.</div>

## Configuring the Data Source

The dashboards for Grafana query the Prometheus server to display the status of the hardware components. A Prometheus data source needs to be configured on the Grafana server.

1. In `\grafana\conf\provisioning\datasource`, open the *hardware-sentry-prometheus.yml* file.
   ![Configuring Data Source Provider](../images/import_grafana_dashboards_config.png)
2. Enter the required settings to connect to your Prometheus server and save your changes. This will create a new data source called **hardware_sentry_prometheus** in Grafana.
3. Restart the Grafana service.

 The dashboards are now loaded in Grafana.

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
## Understanding the Hardware Sentry - Observability and Sustainability Dashboard

The **Hardware Sentry - Observability and Sustainability** dashboard gives you immediate visibility into your monitored environment.  The organized views expose health metrics for all monitored hardware systems and bring real-time metrics and projected trends on electricity consumption and expenses, as well as CO₂ emissions for your entire infrastructure into a unified observability dashboard.
Once you have configured the dashboard provider and the data source as explained above, the **Observability and Sustainability** dashboard is automatically available from the **Dashboard** menu's **Home** page.

### Preliminary Settings

Systems are grouped into sites. This grouping can be easily customized to represent data centers, server rooms, or applications and services. For each site, you can view the consumption in kilowatt per hour, its related cost in dollars, and carbon footprint in metric tons.

You can instantly shift from an overview of your data to a more detailed and granular view within the same collected dataset by clicking on a panel or a metric.

To define sites, open the `hws-config.yaml` file and customize the `extraLabels` as shown in the example below:

```
  yaml
extraLabels:
  site: <sitename> #
```

You must define at least one site. You can add as may sites as needed. Note that it is recommended to dedicate one collector per site.

Also, you need to provide the electricity rate and the estimated CO₂ emissions that **${project.name}** will use to calculate the cost of electricity and carbon footprint of your monitored environment:

* the electricity price (in dollar per kiloWattHour): This is the price of the kWh of electricity used to calculate the energy cost of the physical systems in your monitored infrastructure. Refer to your energy contract to know the tariff by kilowatt/hour charged by your supplier.
* the carbon density (in grams per kiloWattHour): This is the	CO₂ emissions in kg per kWh used to calculate the total carbon emissions based on the electricity consumed by physical systems in your monitored infrastructure. See the [electricityMap Web site](https://app.electricitymap.org/map) for reference. This information is required to evaluate the carbon footprint of your IT infrastructure based on the energy source you are using.
* the Power Usage Effectiveness (PUE) of the data center: This is the metric used to determine the energy efficiency of a data center. PUE is determined by dividing the amount of power entering a data center by the power used to run the computer infrastructure within it. The typical PUE in data centers worldwide ranges between 1 to 2.5.

To customize these settings, open the `hws-config.yaml` file and customize the `extraLabels` as shown in the example below:

```
extraMetrics:
  hw_carbon_density_grams: 350 # in g/kWh
  hw_electricity_cost_dollars: 0.12 # in $/kWh
  hw_pue_ratio: 1.8
```

The **Observability and Sustainability** dashboard is optimized to 

Based on your actual energy use and user-defined reference grids, it also calculates monthly and yearly trends to help you anticipate and control expenditures, identify opportunities to reduce environmental impact and prepare for future legislation.

The dashboard allows you to instantly shift from an overview of your data to a more detailed and granular view within the same collected dataset by clicking on a panel or a metric.

### Global View

The Global View provides an overall comprehensive visibility across your entire monitored environment. It reports on the number of monitored Hosts, their current and forecasted power consumption, electricity cost and carbon emission levels. It also 

   ![Global Information](../images/dashboard_global_info.png)

### Site View

This grouping can be easily customized to represent data centers, server rooms, or applications and services. For each zone, we have it’s annual consumption, in kiloWattHours, it’s annual cost in dollars (or whatever currency you may use), and its annual carbon footprint, in metric tons.

Collected Data Rendering

The **Hardware Sentry - Observability and Sustainability** dashboard consists of several panels displaying graphs or values. Click on any graph (histograms, gauges, pie charts, etc.) to drill-down to the lower level of information, for example from the site level to the host and device level.
The level of temperature is represented with colours from blue (coolest) to red (hottest) allowing you to quickly pinpoint the site, host or device

============================================

USE CASES

### Identifying the most efficient sites

Based on the information you have provided through the `extraLabels`, you can view in chart a classification of the most energy-consuming and highest carbon dioxide emissions sites.

The left-hand chart shows the costs of kiloWattHour per site. In our example below, you can see that *Wellington* is the site with the highest price per kW/h while the cost in *Ottawa* is the more economical.

The right-hand chart displays the sites sorted from the highest to the lowest carbon dioxide emitter based on the energy type used to operate the site. In our example, *Mumbai* and *Ottawa* are at both ends of the spectrum, *Ottawa* being the greener site.

![Identifying the Most Efficient Sites](../images/dashboard_cost_co2_sites_efficiency_list.png)

This indicates that you might want to consider moving some of your activity to *Ottawa* to save money on your electricty costs and lower your carbon footprint.

### Spotting the top consumer devices

The **Top Consumers** panel lists the top ten devices that consume the most electricity per site. The data is automatically refreshed according to the defined collection interval (default: 2m).

The most power-consuming devices could be replaced with more efficient ones or moved to a site with a less expensive electricity tariff.

![Spotting the Top Consumer Devices](../images/dashboard_top_consumers.png)

### Power usage distribution

The **Power Distribution** panel shows the breakdown of the electricity consumption among all your sites. Pass the mouse over the chart to display the amount of kiloWatt consumed by a specific site.

The **Host Types** panel shows the composition of your infrastructure distributed among storage, compute and network dedicated devides, while the **Total Power by Host Type** panel displays the electricity consumption and distribution per type.

![Viewing Power Distribution by Site](../images/dashboard_power_distribution.png)

This information provides the ratio of power actually consumed to sustain data storage, IP traffic, and computing devices activity in your data center.

### Optimizing the ambient temperature

Data centers are energy-intensive facilities. This energy is converted into heat that needs to be dissipated away from the equipment racks to maintain an optimal room temperature. The hardware devices temperature is then a critical parameter that needs to be closely monitored.

The **Heating Margin** gauges represent the number of degrees Celsius (°C) remaining before the temperature reaches the closest warning threshold. **${project.name}** collects 1 day of heating margin measurements for each hardware device and keeps the minimum (critical) value of all the hardware devices in the site.

![Observing Sites'Heating Margin](../images/dashboard_hearting_margin_all_sites.png)

Some hardware devices do not expose their overall temperature and are therefore not included in the heating margin computation.

The **Ambient Temperature** and **Hosts Ambient Temperature** panels displays indicators that pinpoint the sites and hosts emitting the less and most heat, while the **Hosts Highest Temperature Sensors** panel shows the hosts with a value collected from the temperature sensors from the coolest to the hottest.

All these indicators can help you optimize the ambient temperature of your overall datacenter by action at the site or the levels. Increasing the ambient temperature in a facility of 1 degrees Celsius can make you lower your electricity consumption of 5% and reduce your carbon emission by 5% as well, based on an average PUE of 1.80.

### Monitoring the agent collection status

The bottom of the global view of the dashboard lists all the agents configured to collect data in your infrastructure. The list is sortable by site, hostname, agent version, agent build date, connector and timestamp of the latest collect.


List of Agents (otel-config), version and build.
Status de la connexion par agant. Vérfie last collect to see if the collect is ok.
You can start as many collector (hws-config) as you want on a single Agent.







Saving on Energy Costs

Cooling Servers
Servers generate a significant amount of heat that organizations need to counter with cooling
techniques.

Safely Increasing the Ambiant Temperature in a Server Room


Measure Electricity Consumption 





