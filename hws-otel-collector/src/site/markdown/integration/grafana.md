keywords: grafana, dashboard, observability, sustainability, green, power consumption, carbon, CO₂
description: How to import and configure Hardware Sentry's Observability and Sustainability Dashboards for Grafana.

# Grafana Dashboards

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

The **Hardware Sentry Observability and Sustainability** dashboards for Grafana give you immediate visibility into your monitored environment. The organized panels expose health metrics for all monitored hardware systems and bring real-time metrics and projected trends on electricity consumption and costs, as well as CO₂ emissions for your entire infrastructure.
Once you have configured the [dashboard provider](#Configuring_the_Dashboard_Provider) and the [data source](#Configuring_the_Data_Source), the dashboards are automatically available from the **Dashboard** menu on the **Home** page.

![Hardware Sentry Observability and Sustainability Dashboard - Main View](../images/dashboard_main.png)

> **Warning**: The current version of the **Hardware Sentry Observability and Sustainability** dashboards has been tested with Grafana v8.5.0 Note that previous versions of Grafana may not be fully compatible with the built-in dashboards.

## Prerequisites

Before you can start configuring and using **Hardware Sentry Observability and Sustainability** dashboards, you must have:

1. configured [Hardware Sentry Agent](../configuration/configure-agent.html)
2. configured the [Prometheus server](../integration/prometheus.html)
3. run both **${project.name}** and the **Prometheus server**.

## Configuring the Dashboards

### Loading Dashboards in Grafana

First, download the latest version of **hardware-dashboards-for-grafana.zip** or **hardware-dashboards-for-grafana.tar.gz** from [Sentry Software’s Web site](https://www.sentrysoftware.com/downloads/products-for-opentelemetry.html). The package contains:

![Dashboards Package](../images/hardware-dashboards-for-grafana-folders.png)

* the dashboards (.json files)
* the provisioning files (.yml files)

#### On Windows

1. Uncompress **hardware-dashboards-for-grafana.zip** in a temporary folder.
2. Copy the `provisioning` folder to the `grafana\conf` folder on the Grafana server (default: "C:\Program Files\GrafanaLabs\grafana\conf").
3. Copy the `sustainable_IT` folder in the directory of your choice on the Grafana server (ex: "C:\Program Files\GrafanaLabs\grafana\public\dashboards").

    ![Copying Dashboards on Windows](../images/import-dashboards-windows.png)

#### On Linux and UNIX

1. Uncompress **hardware-dashboards-for-grafana.tar.gz** in a temporary folder.
2. Copy the `provisioning` folder to the `grafana` folder on the Grafana server (default: "/etc/grafana").
3. Copy the `sustainable_IT` folder in the directory of your choice folder on the Grafana server (ex: "/var/lib/grafana/dashboards").

### Configuring the Dashboard Provider

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

### Configuring the Data Source

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

## Understanding the Dashboards

Monitored systems are grouped into sites. You can easily customize this grouping to represent data centers, server rooms, or applications and services to view their consumption in kilowatts per hour, related cost in dollars, and carbon footprint in metric tons.

To define sites, open the `config/hws-config.yaml` file and customize the `extraLabels` as shown in the example below:

```
  yaml
extraLabels:
  site: <sitename> #
```

You must define at least one site, but add as many sites as needed. Note that it is recommended to dedicate one collector per site.

Also, you need to provide the information listed below that **${project.name}** will use as a reference to calculate the electricity costs and the carbon footprint of your monitored environment:

* **The carbon density (in grams per kiloWattHour)**: This is the CO₂ emissions in g per kWh used to calculate the total carbon emissions based on the electricity consumed by physical systems in your monitored infrastructure. See the [electricityMap Web site](https://app.electricitymap.org/map) for reference. This information is required to evaluate the carbon footprint of your IT infrastructure based on the energy source you are using.
* **The electricity price (in dollar per kiloWattHour)**: This is the price of the kWh of electricity used to calculate the energy cost of the physical systems in your monitored infrastructure. Refer to your energy contract to know the tariff by kilowatt per hour charged by your supplier or refer to the [GlobalPetrolPrices Web site](https://www.globalpetrolprices.com/electricity_prices/#:~:text=The%20world%20average%20price%20is,1%2C000%2C000%20kWh%20consumption%20per%20year).
* **The Power Usage Effectiveness (PUE)**: This is the metric used to determine the energy efficiency of a data center. PUE is determined by dividing the amount of power entering a data center by the power used to run the computer infrastructure. The typical PUE in data centers worldwide ranges between 1 to 2.5. By default, the **Hardware Sentry - Observability and Sustainability** dashboards use a PUE of 1.8, which is the average value for typical data centers.

To customize these settings, open the `config/hws-config.yaml` file and configure the `extraMetrics` section as shown in the example below:

```
extraMetrics:
  hw_carbon_density_grams: `350` # in g/kWh
  hw_electricity_cost_dollars: `0.12` # in $/kWh
  hw_pue_ratio: `1.8`
```

The collected data is translated into histograms that expose electricity consumption and cost, and carbon emissions in kilowatts per hour, by site.

### Spotting the top consumer sites

On average, and apart from the cooling systems, servers account for the greatest shares of direct electricity use in data centers, followed by storage and network devices.

The **Sites** section of the **Main** dashboard reports on the power consumption of all monitored sites. This metric corresponds to total power consumption of all devices in a site.

![Power Consumption by Site](../images/dashboard_main_top_consumer_sites.png)

The **Power Information** and **Hosts Information** panels in the **Main** dashboard expose the composition of your infrastructure distributed among storage, compute, network dedicated devices and vendors.

This information indicates the power consumption ratio used to sustain computing devices activity, data storage, and IP traffic in your data center.

![Viewing Power and Host Distribution by Type](../images/dashboard_main_power_hosts_info.png)

The **Top Consumers** panel lists the top ten devices that consume the most electricity per site. The data is automatically refreshed according to the defined collection interval (default: 2m).

This information can help you implement the best strategy for efficiency, such as replacing older equipment with more efficient ones or moving servers to a site with a lower electricity cost.

![Spotting the Top Consumer Devices](../images/dashboard_top_consumers.png)

Click on one of the histogram's bar to drill down to the host level and get detailed information about a specific device.

![Viewing Host Information](../images/dashboard_host_view.png)

### Optimizing the ambient temperature in the data center

Data centers are energy-intensive facilities. This energy is converted into heat that must be dissipated away from the equipment racks to maintain an optimal room temperature. Therefore, the hardware devices' temperature is a critical parameter that must be closely monitored.

In most data centers, the air conditioning system ensures the entire room’s ambient temperature is maintained at 18 degrees Celsius, which is generally unnecessarily low to avoid overheating problems. Computer systems can safely operate with an ambient temperature significantly higher (see Google’s example, where they raised the temperature of their data centers to 80°F, i.e. 26.7°C). This is the fastest and cheapest method to reduce the energy consumed by a data center and improve its PUE. From the **Sites** section of the **Main** dashboard, use the **Ambient Temperature** panel to spot the warmer sites. From cold blue to warm red, the color code helps you rapidly identify the sites where the overall temperature can be optimized.

![Monitoring the Ambient Temperature](../images/dashboard_main_ambient_temp.png)

The **Heating Margin** panel exposes the number of degrees Celsius (°C) remaining before the temperature reaches the closest warning threshold for each monitored site. **${project.name}** collects one day of heating margin measurements for each hardware device and exposes the minimum (critical) value of all the hardware devices in the site.

Additionally, the **Hosts Temperature** section exposes the ambient temperature per host and helps you quickly identify the hosts that report the highest temperature.

![Monitoring the Hosts Temperature](../images/dashboard_main_hosts_temp.png)

These indicators can help you optimize your overall data center's ambient temperature by acting at the site or the host's level. Increasing the ambient temperature in a facility by 1 degree Celsius can lower your electricity consumption by 5% and reduce your carbon emission by 5%, based on an average PUE of 1.80 (default).

Some hardware devices do not expose their overall temperature and are therefore not included in the heating margin computation. **${project.name}** provides the percentage of the **Monitoring Confidence** per site in the **site** dashboard. When there is not enough data to generate the daily, monthly, or yearly values, an extrapolation is applied to the current aggregation to calculate an estimation. The more data is available to calculate the costs and carbon emissions, the more confidence you can have in the estimate.

### Estimating potential savings per site

The **Site Temperature Optimization** panel exposes detailed information about the heating margin for a specific site, including the temperature collected by hosts. But this panel is particularly interesting to estimate the savings you could make if you increase the temperature of your facilities to the **Recommended Site Temperature** and how you can significantly reduce the carbon footprint of a site.

![Optimizing a Site Temperature](../images/dashboard_site_temp_optimization.png)

Note that the accuracy of the estimated values increases proportionally with the **Monitoring Confidence** percentage. This percentage is based on the number of hosts reporting temperature readings. The higher number of readings, the higher the monitoring confidence level.

### Monitoring the agent collection status

The **Hardware Sentry Agent Status** panel at the bottom of the **Main** dashboard, lists all the agents configured to collect data, by sites. This panel enables you to view the agents' hostnames, and verify that the agent and connector versions are up-to-date.
The **Last Seen** column indicates the last time an agent was seen during the past 6 hours. An agent going undetected for more that 2 minutes may indicate a potential problem with the host, the connection or the agent configuration.

![Verifying Hardware Sentry Agent Status](../images/dashboard_main-agent-status-collect.png)
