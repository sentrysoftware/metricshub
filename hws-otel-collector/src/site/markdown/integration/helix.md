keywords: configuration, prometheus server, examples, helix, bmc
description: How to integrate the hardware metrics collected by **${project.name}** into BMC Helix Operations Management

# Integration with BMC Helix Operations Management

**${project.name}** can easily integrate with BMC Helix Operations Management to expose hardware health and performance metrics into Helix Dashboards. This is achieved by using the standard Prometheus *Remote Write* protocol, which can be ingested by the BMC Helix platform.

![${project.name} integration with BMC Helix](../images/helix-architecture.png)

## Configuration

Edit the `exporters` section of the [config/otel-config.yaml](../configuration/configure-otel.md) configuration file as in the below example:

```yaml
  prometheusremotewrite/helix:
    endpoint: https://<your-helix-env>.onbmc.com/metrics-gateway-service/api/v1.0/prometheus
    headers:
      Authorization: Bearer <apiToken>
```

where:

* `<your-helix-env>` is the host name of your BMC Helix environment, at **onbmc.com**
* `<apiToken>` is the API Key of your BMC Helix environment

To get your API Key, connect to **BMC Helix Operations Management**, go to the **Administration** &gt; **Repository** page, and click on the **Copy API Key** button.

![Copy API Key](../images/helix-api-key.png)

Then, make sure to declare the exporter in the pipeline section of **config/otel-config.yaml**:

```yaml
service:
  extensions: [health_check]
  pipelines:
    metrics:
      receivers: [prometheus_exec/hws-exporter,prometheus/internal]
      processors: [memory_limiter,batch,metricstransform]
      exporters: [prometheusremotewrite/helix] # Your helix config must be listed here
```
