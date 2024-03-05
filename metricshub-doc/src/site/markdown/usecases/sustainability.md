keywords: IT sustainability monitoring
description: How to configure MetricsHub Agent to collect IT sustainability metrics.

# IT Sustainability

## Configure the sustainability settings


Once you've specified the `site` attribute within the `attributes` section of your resource group, take the next step to acquire insightful data regarding the electricity costs and carbon footprint of your site.
This involves configuring the `metrics` section within your resource group in the `config/metricshub.yaml` file, as demonstrated below:

```yaml
resourceGroups:
  <resource-group-name>:
    attributes:
      site: <site-name>
    metrics:
      hw.site.carbon_intensity: <carbon-intensity-value> # in g/kWh
      hw.site.electricity_cost: <electricity-cost-value> # in $/kWh
      hw.site.pue: <pue-value>
```

where:
* `hw.site.carbon_intensity` is the **carbon intensity in grams per kiloWatthour**. This information is required to calculate the carbon emissions of your site. The carbon intensity corresponds to the amount of CO₂ emissions produced per kWh of electricity and varies depending on the country and the region where the data center is located. See the [electricityMap Web site](https://app.electricitymap.org/map) for reference.
* `hw.site.electricity_cost` is the **electricity price in the currency of your choice per kiloWattHour**. This information is required to calculate the energy cost of your site. Refer to your energy contract to know the tariff by kilowatt per hour charged by your supplier or refer to the [GlobalPetrolPrices Web site](https://www.globalpetrolprices.com/electricity_prices/). Make sure to always use the same currency for all instances of MetricsHub on all sites to allow cost aggregation in your dashboards that cover multiple sites.
* `hw.site.pue` is the **Power Usage Effectiveness (PUE)** of your site. By default, sites are set with a PUE of 1.8, which is the average value for typical data centers.

Replace `<resource-group-name>`, `<site-name>`, `<carbon-intensity-value>`, `<electricity-cost-value>`, and `<pue-value>` with your specific resource group name, site name, and corresponding values for carbon intensity, electricity cost, and PUE. For example:

```yaml
resourceGroups:
  boston:
    attributes:
      site: boston
    metrics:
      hw.site.carbon_intensity: 350 # in g/kWh
      hw.site.electricity_cost: 0.12 # in $/kWh
      hw.site.pue: 1.8
```