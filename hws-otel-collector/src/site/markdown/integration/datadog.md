keywords: datadog, integration
description: How to push to Datadog the hardware metrics collected by ${project.name} and leverage the information available in the provided dashboards.

# Datadog Integration

<!--About **Hardware Sentry**- **${project.name}** integrates seamlessly with your Datadog platform. It only requires a few installation and configuration steps to collect hardware metrics and push them to Datadog.

![${project.name} integration with Datadog](../images/hws-datadog-integration-architecture-diagram.png)

**Hardware Sentry** is available through the [Datadog marketplace](https://app.datadoghq.com/marketplace). Subscribing to the **Hardware Sentry** app allows you to install as many collectors as required (usually one collector per site) and configure the monitoring of all your servers, storage systems, and network switches. Your subscription fees will depend on the number of hosts monitored.-->

## Prerequisites

Before you can start viewing the metrics collected by **${project.name}** in Datadog, you must have:

1. Susbcribed to **Hardware Sentry** from the [Datadog Marketplace](https://app.datadoghq.com/marketplace)
2. Created an API key in Datadog as explained in the [Datadog User Documentation](https://docs.datadoghq.com/account_management/api-app-keys/#add-an-api-key-or-client-token)
3. [Installed Hardware Sentry OpenTelemetry Collector](./install.html) on a system that has network access to the physical servers, switches and storage systems you need to monitor. It is recommended to dedicate one collector per site, or data center, or server room, etc.

## Configuring the integration

### Pushing metrics to Datadog

Edit the `exporters` section of the [`config/otel-config.yaml` configuration file](./configuration/configure-otel.html) as follows:

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

where `<apikey>` corresponds to your Datadog API key.

Declare the exporter in the pipeline section of **config/otel-config.yaml** as follows:

```yaml
service:
  extensions: [health_check]
  pipelines:
    metrics:
      receivers: [prometheus_exec/hws-exporter,prometheus/internal]
      processors: [memory_limiter,batch,metricstransform]
      exporters: [datadog/api] # Datadog must be listed here
```

Restart **${project.name}** to apply your changes.

### Configuring sites and sustainability settings

Monitored systems are grouped into sites. You can easily customize this grouping to represent data centers or server rooms to view their consumption in kilowatts per hour, related cost in dollars, and carbon footprint in metric tons.

To define sites, open the `config/hws-config.yaml` file and customize the `extraLabels` section as shown in the example below:

```yaml
extraLabels:
  site: Datacenter 1  
```

You must define at least one site, but add as many sites as needed. Note that it is recommended to dedicate one collector per site.

Also, you need to provide the information listed below that **${project.name}** will use as a reference to calculate the electricity costs and the carbon footprint of your site:

* **The carbon density (in grams per kiloWattHour)**: This information is required to calculate the carbon emissions of your IT infrastructure. The carbon density corresponds to the amount of CO₂ emissions produced per kWh of electricity and varies depending on the country and the region where the data center is located. See the [electricityMap Web site](https://app.electricitymap.org/map) for reference.
* **The electricity price (in dollars per kiloWattHour)**: This information is required to calculate the energy cost of your IT infrastructure. Refer to your energy contract to know the tariff by kilowatt per hour charged by your supplier or refer to the [GlobalPetrolPrices Web site](https://www.globalpetrolprices.com/electricity_prices/#:~:text=The%20world%20average%20price%20is,1%2C000%2C000%20kWh%20consumption%20per%20year). 
* **The Power Usage Effectiveness (PUE)**: This information is used to determine the data center efficiency. It is calculated by dividing the amount of power entering a data center by the power used to run the computer infrastructure. The typical PUE in data centers worldwide ranges between 1 to 2.5. By default, sites are set with a PUE of 1.8, which is the average value for typical data centers.

To customize these settings, open the `config/hws-config.yaml` file and configure the `extraMetrics` section as shown in the example below:

```yaml 
extraMetrics:
  hw_carbon_density_grams: `350` # in g/kWh
  hw_electricity_cost_dollars: `0.12` # in $/kWh
  hw_pue_ratio: `1.8`
```

### Configuring the hosts to monitored

For each host to be monitored, you need to specify its hostname, type, and protocol to be used in the **config/hws-config.yaml** file. Refer to [Configuring the Hardware Sentry Agent](./configuration/configure-agent.html) for more details.

### Adding Monitors

To report hardware failures in Datadog, use the **Monitors > New Monitor** interface to add all the *Recommended* monitors for **Hardware Sentry**.

## Using the Dashboards

**Hardware Sentry** comes with the following dashboards which leverage the metrics collected by **${project.name}**:

| Dashboard                  | Description                                                                                 |
| -------------------------- | ------------------------------------------------------------------------------------------- |
| **Hardware Sentry - Main** | Overview of all monitored hosts, with a focus on sustainability                             |
| **Hardware Sentry - Site** | Metrics associated to one *site* (a data center or a server room) and its monitored *hosts* |
| **Hardware Sentry - Host** | Metrics associated to one *host* and its internal devices                                   |

### Detecting monitoring configuration problems

The **Coverage** widget available in the **Hardware Sentry - Main** dashboard indicates the percentage of hosts configured that are actually monitored.

![Datadog Dashboards - Monitoring Coverage](../images/datadog-main-coverage.png)

If the coverage is below 90%, verify that your hosts are properly configured in the **config/hws-config.yaml** file.

You can also check the **Hardware Sentry Agents Information** widget at the bottom of the **Hardware Sentry - Main** dashboard to ensure your **Hardware Sentry Agents** in charge of collecting data are up and running.

![Hardware Sentry Agents Information](../images/datadog-agents-information.png)

#### Detecting and troubleshooting hardware failures

The **Current Hardware Issues** widget available in all dashboards displays the number of alerts and warnings triggered.

![Datadog Dashboards - Current Hardware Issues](../images/datadog-current-hardware-issues.png)

Click the **Alert** or **Warn** widget to access the **Triggered Monitors** page. Click a monitor to get more details about the failure:

![Triggered Monitors](../images/datadog-events-explorer.png)

### Estimating the power consumption and carbon emissions of your IT infrastructure

After collecting metrics for a few hours, **{project.name}** is able to estimate the power consumption, energy costs, and CO₂ emissions of the IT infrastructure on a daily, monthly, and yearly basis and provide that information in the **Hardware Sentry - Main** and **Hardware Sentry - Site** dashboards:

![Estimated consumption and emissions](../images/datadog-main-estimated-consumption-and-emissions.png)

The **Margin of Error** indicates the percentage of error in the estimate. The lower its value, the more reliable it is.

**${project.name]$** also reports the power consumption, energy costs, the CO₂ emissions of each monitored host in the corresponding **Hardware Sentry - Host** dashboard:

![Reporting the host's power consumption and carbon footprint](../images/datadog-host-power-consumption-and-emissions.png)

You can also refer to the **Power per Device Type** widget to know which device consumes more power.

### Comparing the efficiency and environmental impact of your sites

The **Power, Cost and CO₂ Emissions** widget available in the **Hardware Sentry - Main** dashboard allows you to quickly identify which of your sites:

* is the most energy-intensive (**Yearly Energy Usage (Wh)**)
* has the higest energy costs (**Yearly Cost ($)**)
* is the most harmful for the environment (**Yearly CO₂ Emissions (tons**).

![Power, Cost and CO₂ Emissions by Site](../images/datadog-main-power-costs-emissions.png)

To find the first responses to your questions, refer to the **Sites** widget of the **Hardware Sentry - Main** dashboard as it provides:

* the **number of hosts** composing the site. A bigger site would logically consume more energy than a smaller one
* the **ambient temperature** and **heating margin** of each site. You can consider increasing the temperature of a site by a few degrees if its ambient temperature is unnecessary low compared to the heating margin.

![Sites monitored](../images/datadog-main-sites.png)

If you want to follow the temperature optimization lead, click the site to open the corresponding **Hardware Sentry - Site** dashboard and navigate to the **Site Temperature Optimization** widget:

![Site Temperature Optimization](../images/datadog-site-temperature-optimization.png)

The **Site Temperature Optimization** widget exposes the heating margin at the site and hosts levels as well as its evolution over time. But this widget is particularly interesting to estimate the savings you could make if you increase the temperature of your site to the **Recommended Site Temperature** and how you can significantly reduce the carbon footprint of a site.

Note that the accuracy of the estimated values increases proportionally with the **Monitoring Confidence** percentage.

### Detecting overheating risks for hosts

The **Heating Information** widget available in the **Hardware Sentry - Main** dashboard allows you to quickly identify the hosts that are at the risk of overheating. Each temperature sensor is individually monitored and exposed in a color-coding system. Warm colors indicate that hosts will soon reach their thermal limit:

![Heating Information](../images/datadog-main-heating-information.png)

Click the host to access its details.



<!-- 
### Hardware Sentry - Host

The **Hardware Sentry - Host** dashboard displays what we monitor on the machine:
* Monitors and Events
* Power Consumption and Carbon Emissions + Power consumption per type of device
* Temperature + Temperature Forecast for the next 24hours
* Network traffic
* CPU, voltage, batteries
* Storage information (disk)
* Map  to indicate what is monitored
* Monitored devices
* Monitoring information: agent, connectors, status

![Hardware Sentry - Host dashboard](../images/datadog-host.png)-->

<!-- ### Optimizing the ambient temperature in the data center

In most data centers, the air conditioning system ensures the entire room’s ambient temperature is maintained at 18 degrees Celsius, which is generally unnecessarily low to avoid overheating problems. Computer systems can safely operate with an ambient temperature significantly higher (see Google’s example, where they raised the temperature of their data centers to 80°F, i.e. 26.7°C). This is the fastest and cheapest method to reduce the energy consumed by a data center and improve its PUE. From the **Sites** section of the **Main** dashboard, use the **Ambient Temperature** panel to spot the warmer sites. From cold blue to warm red, the color code helps you rapidly identify the sites where the overall temperature can be optimized.

![Monitoring the Ambient Temperature](../images/dashboard_main_ambient_temp.png)

The **Heating Margin** panel exposes the number of degrees Celsius (°C) remaining before the temperature reaches the closest warning threshold for each monitored site. **${project.name}** collects one day of heating margin measurements for each hardware device and exposes the minimum (critical) value of all the hardware devices in the site.

Additionally, the **Hosts Temperature** section exposes the ambient temperature per host and helps you quickly identify the hosts that report the highest temperature.

![Monitoring the Hosts Temperature](../images/dashboard_main_hosts_temp.png)

These indicators can help you optimize your overall data center's ambient temperature by acting at the site or the host's level. Increasing the ambient temperature in a facility by 1 degree Celsius can lower your electricity consumption by 5% and reduce your carbon emission by 5%, based on an average PUE of 1.80 (default).

Some hardware devices do not expose their overall temperature and are therefore not included in the heating margin computation. **${project.name}** provides the percentage of the **Monitoring Confidence** per site in the **site** dashboard. When there is not enough data to generate the daily, monthly, or yearly values, an extrapolation is applied to the current aggregation to calculate an estimation. The more data is available to calculate the costs and carbon emissions, the more confidence you can have in the estimate. --> 