keywords: configuration, helix, bmc
description: How to integrate the hardware metrics collected by **${solutionName}** into BMC Helix Operations Management

# Integration with BMC Helix Operations Management

**${solutionName}** can easily integrate with BMC Helix Operations Management to expose hardware health and performance metrics into Helix Dashboards. This is achieved by using the standard Prometheus *Remote Write* protocol, which can be ingested by the BMC Helix platform.

![${solutionName} integration with BMC Helix](../images/helix-architecture.png)

## Configuration

Edit the `exporters` section of the [otel/otel-config.yaml](../configuration/configure-otel.md) configuration file as in the below example:

```yaml
  prometheusremotewrite/helix:
    endpoint: https://<your-helix-env>.onbmc.com/metrics-gateway-service/api/v1.0/prometheus
    headers:
      Authorization: Bearer <apiToken>
    resource_to_telemetry_conversion:
      enabled: true
```

where:

* `<your-helix-env>` is the host name of your BMC Helix environment, at **onbmc.com**
* `<apiToken>` is the API Key of your BMC Helix environment
* `resource_to_telemetry_conversion` converts all the resource attributes to metric labels when enabled

To get your API Key, connect to **BMC Helix Operations Management**, go to the **Administration** &gt; **Repository** page, and click on the **Copy API Key** button.

![Copy API Key](../images/helix-api-key.png)

Then, make sure to declare the exporter in the pipeline section of **otel/otel-config.yaml**:

```yaml
service:
  extensions: [health_check]
  pipelines:
    metrics:
      receivers: [prometheus_exec/hws-exporter,prometheus/internal]
      processors: [memory_limiter,batch,metricstransform]
      exporters: [prometheusremotewrite/helix] # Your helix config must be listed here
```