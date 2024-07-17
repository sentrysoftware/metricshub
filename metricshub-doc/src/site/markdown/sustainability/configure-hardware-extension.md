keywords: configuration, hardware extension, sustainability, dashboards, Grafana, Datadog
description: How to configure the hardware extension and ensure Datadog and Grafana dashboards are populated.

# Sustainability Metrics

**MetricsHub** enriches hardware data with **energy usage metrics**, such as `hw.host.energy`, `hw.host.heating_margin`, `hw.host.power`, to deliver an accurate assessment of a resource's carbon footprint. Visualized in user-friendly dashboards, these sustainability metrics offer a comprehensive understanding of the environmental impact of the IT infrastructure.

To ensure dashboards are properly populated, you must configure the **Hardware Extension** in the `config/metricshub.yaml` configuration file. 

## Configure the Hardware Extension

To obtain the electricity costs and carbon footprint of your resource, configure the `Metrics` section of the `config/metricshub.yaml` file as follows:

```yaml
    # Adds additional static metrics to all the resources in the group.
    metrics:
      hw.site.carbon_intensity: 230 # in g/kWh
      # Carbon dioxide produced per kilowatt-hour.
      # The average is 230g/kWh for Europe.
      # The average is 309g/kWh for Texas, USA.
      # The average is 40g/kWh for Ontario, Canada.
      # The average is 712g/kWh for Queensland, Australia.
      # Source: https://app.electricitymap.org/map

      hw.site.electricity_cost: 0.12 # in $/kWh
      # Electricity cost per kilowatt-hour. 
      # The average is $0.12/kWh for non-household in Europe.
      # The average is $0.159/kWh for non-household in the USA.
      # The average is $0.117/kWh for non-household in Canada.
      # The average is $0.225/kWh for non-household in Australia.
      # Source: https://www.globalpetrolprices.com/electricity_prices/

      hw.site.pue: 1.8
      # Power Usage Effectiveness. A ratio describing how efficiently a computer data center uses energy. The ideal ratio is 1.
```

where:

* `hw.site.carbon_intensity` is the **carbon intensity in grams per kiloWatthour**. This information is required to calculate the carbon emissions of your site. The carbon intensity corresponds to the amount of CO₂ emissions produced per kWh of electricity and varies depending on the country and the region where the data center is located. See the [electricityMap Web site](https://app.electricitymap.org/map) for reference.
* `hw.site.electricity_cost` is the **electricity price in the currency of your choice per kiloWattHour**. This information is required to calculate the energy cost of your site. Refer to your energy contract to know the tariff by kilowatt per hour charged by your supplier or refer to the [GlobalPetrolPrices Web site](https://www.globalpetrolprices.com/electricity_prices/). Make sure to always use the same currency for all instances of **MetricsHub** on all sites to allow cost aggregation in your dashboards that cover multiple sites.
* `hw.site.pue` is the **Power Usage Effectiveness (PUE)** of your site. By default, sites are set with a PUE of 1.8, which is the average value for typical data centers.

You can either hardcode a value or configure **MetricsHub** to retrieve that information.
