keywords: grafana, troubleshooting
description: How to troubleshoot Grafana dashboards when sustainable data is not available or is inaccurate or hardware metrics are missing

# Troubleshooting Grafana Dashboards

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

This section describes common errors that may occur when using **Hardware Observability and Sustainability dashboards** in Grafana and the basic troubleshooting steps to follow. If you require further assistance, [subscribe to one of our support plans](https://www.sentrysoftware.com/pricing/) and get access to [Sentry Desk](https://www.sentrysoftware.com/desk), our superior customer support.  

## No sustainability data available

You usually get a "No data" message for sustainability metrics when:

* the query takes too long to complete. This could be due to a network issue, a lack of resources, or the Prometheus server running slow.
* **${solutionName}** has been running for less than 24 hours and does not have enough data to compute the annual type of metrics (typically, `Annual Energy Usage`, `Annual Cost`, `Annual CO₂ Emissions`)
* the `hw.site.pue_ratio`, `hw.site.electricity_cost_dollars`, and `hw.site.carbon_density_grams` options are not properly set in the **config/hws-config.yaml** file. Refer to [Configure the sustainability settings](../configuration/configure-agent.html##Configure_the_sustainability_settings) for more details.

## Energy usage and carbon emissions are oddly low

If the values of the **Annual Energy Usage**, **Annual Cost** and **Annual CO₂ Emissions** panels seem low compared to the number of sites monitored, open each monitored site and verify that values are returned for:

* `PUE`
* `Electricity cost`
* `CO₂ density`

If no values are displayed, open the **config/hws-config.yaml** file dedicated to each site and configure the following metrics:

* `hw.site.pue_ratio`
* `hw.site.electricity_cost_dollars`
* `hw.site.carbon_density_grams`

Refer to [Configure the sustainability settings](../configuration/configure-agent.html##Configure_the_sustainability_settings) for more details.

## No hardware metrics available for hosts

If you notice that no hardware metrics are displayed for hosts:

1. Connect to your Prometheus server and search for the missing metric. If the metric corresponding to the monitored host is:
   * found, **${solutionName}** collects data and pushes it to Prometheus. The issue is on the Grafana level. Please proceed to step 2.
   * not found:
     * in the `otel/otel-config.yaml` file, add `prometheus` under the `pipelines:metrics:exporters` section to enable the `Prometheus Exporter`:

      ```yaml
      pipelines:
        metrics:
         receivers: [otlp, prometheus/internal]
         processors: [memory_limiter, batch, resourcedetection, metricstransform]
          exporters: [prometheusremotewrite/your-server, prometheus] 
       ```

     * restart **${solutionName}**
     * connect to the server running the *Hardware Sentry Agent* and open the URL `http://<localhost>:24375/metrics` to verify that **${solutionName}** is collecting data. If data:

        * is collected, the Prometheus exporter is not properly set. Refer to [Integration with Prometheus Server](../prometheus/prometheus.html) for more details.
        * is not collected:
           * Open the **config/hws-config.yaml** file and verify that the monitoring configuration is correct
           * Enable debug and investigate further. It might be a java, a firewall, or a configuration issue. Refer to [Debugging](./debug.html) for more details. If the debug indicates that the issue is related to the instrumentation layer, use [Sentry Software's Troubleshooting tools](https:www.sentrysoftware.com/bmc/support/troubleshooting-tools.html) to identify the real source of the problem. If you [subscribed to one of our support plan](https://www.sentrysoftware.com/pricing/), you can also contact our Support Team.

2. When data is available in Prometheus but not in Grafana:
     * Connect to Grafana
     * Open the **Host** dashboard
     * Edit the panel of the hardware components for which no data is available
     * In the **Metrics browser** field, verify that the query and metrics labels are correct and make the corrections required.