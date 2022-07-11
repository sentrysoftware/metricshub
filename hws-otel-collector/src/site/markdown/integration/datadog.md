keywords: datadog, integration
description: How to push to Datadog the hardware metrics collected by ${project.name} and leverage the information available in the provided dashboards.

# Datadog Integration

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

**${project.name}** integrates seamlessly with your Datadog environment. The **Hardware Sentry** app, available through the [Datadog marketplace](https://app.datadoghq.com/marketplace), includes a collection of dashboards and monitors designed to collect and expose observability and sustainability data for your IT infrastructure in a turn-key solution.

![${project.name} integration with Datadog](../images/hws-datadog-integration-architecture-diagram.png)

Integrating **Hardware Sentry** with your Datadog SaaS platform only requires a few installation and configuration steps.

## Prerequisites

Before you can start viewing the metrics collected by **${project.name}** in Datadog, you must have:

1. Susbcribed to **Hardware Sentry** from the [Datadog Marketplace](https://app.datadoghq.com/marketplace)
2. Created an API key in Datadog as explained in the [Datadog User Documentation](https://docs.datadoghq.com/account_management/api-app-keys/#add-an-api-key-or-client-token)
3. [Installed Hardware Sentry OpenTelemetry Collector](../install.html) on one or more systems that has network access to the physical servers, switches and storage systems you need to monitor. It is recommended to dedicate one collector per site, or data center, or server room, etc.

## Configuring the integration

### Pushing metrics to Datadog

1. Browse to open the **${project.name}** configuration directory (`hws-otel-collector\config` by default) and open the `config/otel-config.yaml` configuration file.
2. Find the `exporters` section and edit it as follows:

```yaml
exporters:
  # Datadog
  # <https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/datadogexporter>
  datadog/api:
    api:
      key: <apikey>
      # site: datadoghq.eu # Specify the Datadog site you are on (datadoghq.com for the US (default), datadoghq.eu for Europe, ddog-gov.com for Governement sites). Refer to https://docs.datadoghq.com/getting_started/site/ for more details. 
    metrics:
      resource_attributes_as_tags: true # IMPORTANT
```

where `<apikey>` corresponds to your Datadog API key.

and declare the exporter in the `pipelines` section as follows:

```yaml
service:
  metrics:
    exporters: [datadog/api] # Datadog must be listed here
```

Restart **${project.name}** to apply your changes.

Refer to [Configuring the OpenTelemetry Collector](../configuration/configure-otel.html) for more details.

### Configuring sites and sustainability settings

Monitored systems are grouped into sites. You can easily customize this grouping to represent data centers or server rooms to view their consumption in kilowatts per hour, related cost in dollars, and carbon footprint in metric tons.

To define sites, open the `config/hws-config.yaml` file and customize the `extraLabels` section as shown in the example below:

```yaml
extraLabels:
  site: boston 
```

You must define at least one site, but add as many sites as needed.

You also need to update the `extraMetrics` section as shown in the example below to allow **${project.name}** to calculate the electricity costs and the carbon footprint of your site:

```yaml 
extraMetrics:
  hw.site.carbon_density_grams: 350 # in g/kWh
  hw.site.electricity_cost_dollars: 0.12 # in $/kWh
  hw.site.pue_ratio: 1.8
```

where:
* `hw.site.carbon_density_grams` is the **carbon density in grams per kiloWatthour**. This information is required to calculate the carbon emissions of your IT infrastructure. The carbon density corresponds to the amount of CO₂ emissions produced per kWh of electricity and varies depending on the country and the region where the data center is located. See the [electricityMap Web site](https://app.electricitymap.org/map) for reference. 
* `hw.site.electricity_cost_dollars` is the **electricity price in dollars per kiloWattHour**. This information is required to calculate the energy cost of your IT infrastructure. Refer to your energy contract to know the tariff by kilowatt per hour charged by your supplier or refer to the [GlobalPetrolPrices Web site](https://www.globalpetrolprices.com/electricity_prices/#:~:text=The%20world%20average%20price%20is,1%2C000%2C000%20kWh%20consumption%20per%20year).
* `hw.site.pue_ratio` is the **Power Usage Effectiveness (PUE)** of your site. By default, sites are set with a PUE of 1.8, which is the average value for typical data centers.

### Configuring the hosts to monitored

For each host to be monitored, you need to specify its hostname, type, and protocol to be used in the **config/hws-config.yaml** file. Refer to [Configuring the Hardware Sentry Agent](../configuration/configure-agent.html) for more details.

### Adding monitors

To be notified in Datadog about any hardware failure, go to **Monitors > New Monitor** and add all the *Recommended* monitors for **Hardware Sentry**.

## Using the Hardware Sentry dashboards

**Hardware Sentry** comes with the following dashboards which leverage the metrics collected by **${project.name}**:

| Dashboard                  | Description                                                                                 |
| -------------------------- | ------------------------------------------------------------------------------------------- |
| **Hardware Sentry - Main** | Overview of all monitored hosts, with a focus on sustainability                             |
| **Hardware Sentry - Site** | Metrics associated to one *site* (a data center or a server room) and its monitored *hosts* |
| **Hardware Sentry - Host** | Metrics associated to one *host* and its internal devices                                   |

They allow you to perform the operations described below.

### Detecting monitoring configuration problems

The **Coverage** widget available in the **Hardware Sentry - Main** dashboard indicates the percentage of hosts configured that are actually monitored.

![Datadog Dashboards - Monitoring Coverage](../images/datadog-main-coverage.png)

If the coverage is below 100%, verify that your hosts are properly configured in the **config/hws-config.yaml** file.

You can also check the **Hardware Sentry Agents Information** widget at the bottom of the **Hardware Sentry - Main** dashboard to ensure your **Hardware Sentry Agents** in charge of collecting data are up and running.

![Datadog Dashboards - Hardware Sentry Agents Information](../images/datadog-agents-information.png)

#### Detecting and troubleshooting hardware failures

The **Current Hardware Issues** widget available in all dashboards displays the number of alerts and warnings triggered.

![Datadog Dashboards - Current Hardware Issues](../images/datadog-current-hardware-issues.png)

Click the **Alert** or **Warn** widget to access the **Triggered Monitors** page. Click a monitor to get more details about the failure:

![Datadog Monitors - Hardware Failures Detected](../images/datadog-events-explorer.png)

### Estimating the energy usage and carbon footprint of your infrastructure

After collecting metrics for a few hours, **{project.name}** can estimate the power consumption, energy costs, and carbon emissions of your overall IT infrastructure, sites and even hosts on a daily, monthly, and yearly basis.

![Datadog Dashboards -Estimated consumption and emissions](../images/datadog-main-estimated-consumption-and-emissions.png)

The **Margin of Error** indicates the percentage of error in the estimate. A lower value means a more accurate estimation.

**${project.name}** also reports the power consumption, energy costs, the CO₂ emissions of each monitored host in the corresponding **Hardware Sentry - Host** dashboard:

![Datadog Dashboards - Reporting the host's power consumption and carbon footprint](../images/datadog-host-power-consumption-and-emissions.png)

The **Power per Device Type** widget provides an estimation about the power consumed by the internal components of the monitored host.

### Comparing the efficiency and environmental impact of your sites

The **Power, Cost and CO₂ Emissions** widget available in the **Hardware Sentry - Main** dashboard allows you to quickly identify which of your sites:

* is the most energy-intensive (**Yearly Energy Usage (Wh)**)
* has the highest energy costs (**Yearly Cost ($)**)
* is the most harmful for the environment (**Yearly CO₂ Emissions (tons**)).

![Datadog Dashboards - Power, Cost and CO₂ Emissions by Site](../images/datadog-main-power-costs-emissions.png)

To find the first responses to your questions, refer to the **Sites** widget of the **Hardware Sentry - Main** dashboard as it provides:

* the **number of hosts** composing the site. A bigger site would logically consume more energy than a smaller one
* the **ambient temperature** and **heating margin** of each site. You can consider increasing the temperature of a site by a few degrees if its ambient temperature is particularly low compared to the [ASHRAE recommendations](https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=&cad=rja&uact=8&ved=2ahUKEwjZ_Ke56Oj4AhXawoUKHY2fBogQFnoECCEQAQ&url=https%3A%2F%2Ftpc.ashrae.org%2FFileDownload%3Fidx%3Dc81e88e4-998d-426d-ad24-bdedfb746178&usg=AOvVaw0CU3GVE4AuY5IJ0Q2u7Iin) and if you have an acceptable heating margin.

![Datadog Dashboards - Sites monitored](../images/datadog-main-sites.png)

If you want to follow the temperature optimization lead, click the site to open the corresponding **Hardware Sentry - Site** dashboard and navigate to the **Site Temperature Optimization** widget:

![Datadog Dashboards - Site Temperature Optimization](../images/datadog-site-temperature-optimization.png)

The **Site Temperature Optimization** widget exposes the heating margin at the site (*Heating Margin*) and hosts levels (*Heating Margin Host Distribution* ) as well as its evolution over time (*Site Heating Margin (°C)* widget). But this widget is particularly interesting to estimate the savings you could make if you increase the temperature of your site to the **Recommended Site Temperature** and how you could significantly reduce its carbon footprint.

Note that the accuracy of the estimated values increases proportionally with the **Monitoring Confidence** percentage. That percentage is based on the number of hosts reporting temperature readings. The more hosts report readings, the higher the monitoring confidence is.

### Detecting overheating risks for hosts

The **Heating Information** widget available in the **Hardware Sentry - Main** dashboard allows you to quickly identify the hosts that are at the risk of overheating. Each temperature sensor is individually monitored and exposed in a color-coding system. Warm colors indicate that hosts will soon reach their thermal limit:

![Datadog Dashboards - Heating Information](../images/datadog-main-heating-information.png)

Click the host to access its details.

### Observing the hardware health and environmental impact of the monitored hosts

The **Hardware Sentry - Host** dashboard exposes all the observability and sustainability data available for the monitored host, and notably:

* the status of its internal components
* the network traffic
* the storage usage
* the power consumption and related carbon emissions
* the temperature information
* etc. 

![Datadog Dashboard - Observability and sustainability data for the monitored hosts](../images/datadog-host.png)

Information about the monitoring itself (host information, connectors used, etc.) is provided in the **Monitoring Information** section.

![Datadog Dashboard - Host monitoring information](../images/datadog-host-monitoring-information.png).