keywords: datadog, integration
description: How to integrate ${project.name} into Datadog.

# Datadog Integration

**Hardware Sentry** is made available through the [Datadog marketplace](https://app.datadoghq.com/marketplace). Subscribing to the **Hardware Sentry** app allows you to install as many collectors as required (usually one collector per site) and configure the monitoring of all your servers, storage systems, and network switches. Your subscription fees will depend on the number of hosts monitored.

The **Hardware Sentry** app comes with a set of dashboards that leverage the metrics collected by **[Hardware Sentry OpenTelemetry Collector](https://www.sentrysoftware.com/products/hardware-sentry-opentelemetry-collector.html)**:

| Dashboard                                        | Description                                                                                               |
| ------------------------------------------------ | --------------------------------------------------------------------------------------------------------- |
| Hardware Sentry - Observability & Sustainability | Overview of all monitored hosts, with a focus on sustainability                                           |
| Hardware Sentry - Site                           | Metrics associated to one _site_ (a data center or a server room) and its monitored _hosts_               |
| Hardware Sentry - Host                           | Metrics associated to one _host_ and its internal devices                                                 |

![${project.name} integration with Datadog](../images/hws-datadog-integration-architecture-diagram.png)

## Prerequisites

Before you can start viewing the metrics collected by **${project.name}** in Datadog, you must have:

1. Susbcribed to **Hardware Sentry** from the [Datadog Marketplace](https://app.datadoghq.com/marketplace). Subscribing to the **Hardware Sentry** app allows you to install as many collectors as required (usually one collector per site) and configure the monitoring of all your servers, storage systems, and network switches
2. Created an API key in Datadog as explained in the [Datadog User Documentation](https://docs.datadoghq.com/account_management/api-app-keys/#add-an-api-key-or-client-token)
3. [Installed Hardware Sentry OpenTelemetry Collector](./install.html) on a system that has network access to the physical servers, switches and storage systems you need to monitor. It is recommended to install one collector on each site — that is, each data center, each server room, etc.

## Configuring the integration

### Pushing metrics to Datadog

Edit the [`config/otel-config.yaml` configuration file](./configuration/configure-otel.html) as follows to push metrics to Datadog:

   ```yaml
   exporters:
     # Datadog
     # <https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/datadogexporter>
     datadog/api:
       api:
         key: <apikey> # 
         # site: datadoghq.eu # Uncomment for Europe only
       metrics:
         resource_attributes_as_tags: true # IMPORTANT
   ```

Replace `<apikey>` with your Datadog API key and restart of the **${project.name}** to apply your changes.

### Configuring the hosts to monitored

Refer to [Configuring the Hardware Sentry Agent](./configuration/configure-agent.html) for more details. 

### Adding Monitors

To report hardware failures in Datadog, use the **Monitors > Manage Monitors > Create New Monitor** interface to add all the monitors listed for **Hardware Sentry** in the _Recommended_ tab.

## Understanding the Dashboards

<! -- 

Monitored systems are grouped into sites. You can easily customize this grouping to represent data centers, server rooms, or applications and services to view their consumption in kilowatts per hour, related cost in dollars, and carbon footprint in metric tons.

To define sites, open the `config/hws-config.yaml` file and customize the `extraLabels` as shown in the example below:

```
  yaml
extraLabels:
  site: <sitename> #
```

You must define at least one site, but add as many sites as needed. Note that it is recommended to dedicate one collector per site.

Also, you need to provide the information listed below that **${project.name}** will use as a reference to calculate the electricity costs and the carbon footprint of your monitored environment:

* **The electricity price (in dollar per kiloWattHour)**: This is the price of the kWh of electricity used to calculate the energy cost of the physical systems in your monitored infrastructure. Refer to your energy contract to know the tariff by kilowatt per hour charged by your supplier or refer to the [GlobalPetrolPrices Web site](https://www.globalpetrolprices.com/electricity_prices/#:~:text=The%20world%20average%20price%20is,1%2C000%2C000%20kWh%20consumption%20per%20year).
* **The carbon density (in grams per kiloWattHour)**: This is the	CO₂ emissions in kg per kWh used to calculate the total carbon emissions based on the electricity consumed by physical systems in your monitored infrastructure. See the [electricityMap Web site](https://app.electricitymap.org/map) for reference. This information is required to evaluate the carbon footprint of your IT infrastructure based on the energy source you are using.
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

Note that the accuracy of the estimated values increases proportionally with the **Monitoring Confidence** percentage.

### Monitoring the agent collection status

The **Hardware Sentry AgentS Information** panel, located at the bottom of the **Hardware Sentry - Observability & Sustainability** dashboard, lists all the agents configured to collect data. This panel enables you to view the agents' hostnames, verify that the Hardware Connector Library and **${project.name}$** versions are up-to-date, the sites they apply to, and their operating status.-->