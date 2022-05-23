keywords: grafana, dashboard, troubleshooting, no data
description: How to troubleshoot Grafana dashboards when data is not available or is inaccurate 

# Troubleshooting Grafana Dashboards

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

> This section describes the basic troubleshooting steps to follow.

## No sustainability data available

You usually get a "No data" message for sustainability metrics when:

* **${project.name}** has been running for less than 24 hours and does not have enough data to compute the annual type of metrics (typically, `Annual Energy Usage`, `Annual Cost`, `Annual CO₂ emission`)
* the `hw.site.pue_ratio`, `hw.site.electricity_cost_dollars`, and `hw.site.carbon_density_grams` options are not properly set in the **config/hws-config.yaml** file.

## Energy usage and carbon emissions are oddly low

If the values of the **Annual Energy Usage**, **Annual Cost** and **Annual CO₂ Emissions** panels seem low compared to the number of sites monitored, open each monitored site and check that values are returned for:

* `PUE`
* `Electricity cost`
* `CO₂ density`

If no values are displayed, open the **config/hws-config.yaml** file dedicated to each site and configure the following metrics:

* `hw.site.pue_ratio`
* `hw.site.electricity_cost_dollars`
* `hw.site.carbon_density_grams`

## No hardware metrics available for hosts

If you notice that no hardware metrics are displayed for hosts:

1. Connect to Prometheus `https://<prometheus-server>:9090/graph`:
  
     * Search for the missing metric in Prometheus. If the metric corresponding to the monitored host is:
       * found, **${project.name}** collects data and pushes it to Prometheus. The issue is on the Grafana level. Please proceed to step 2
       * not found, **${project.name}** does not collect data:
          * Open the **config/hws-config.yaml** file and verify that the monitoring configuration is correct
          * Enable debug and investigate further. It might be a java, a firewall, or a configuration issue. Refer to [Debugging](./debug.html) for more details. If the debug indicates that the issue is related to the instrumentation layer, use [Sentry's Troubleshooting tools](https://d8dt4sd6nzbfc.cloudfront.net/bmc/support/troubleshooting-tools.html) to identify the real source of the problem.

2. When data is available in Prometheus but not in Grafana:

   * Connect to Grafana
   * Open the host dashboard
   * Edit the panel of the hardware components for which no data is available
   * In the **Metrics browser** field, verify that the query and metrics labels are correct and make the corrections required.