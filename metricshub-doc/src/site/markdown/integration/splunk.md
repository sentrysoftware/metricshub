keywords: splunk, integration, signal fx, splunk observability cloud
description: How to push the metrics collected by MetricsHub to Splunk Observability Cloud

# Splunk integration

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

**MetricsHub** integrates seamlessly into Splunk Observability Cloud throught the [Splunk SignalFx exporter](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/signalfxexporter). 

![MetricsHub pushes the collected metrics to Splunk](../images/metricshub-splunk-diagram.png)

This integration allows you to:

* visualize IT infrastructure and sustainability metrics in your Splunk Observability Cloud platform 
* be informed when hardware failures are detected.

![Splunk alert for a failed disk detected by MetricsHub](../images/splunk-dashboard-hardware-main-failed-disk-alert.png)

## Prerequisites

Before you can start viewing the metrics collected by **MetricsHub** in Splunk Observability Cloud, you must have:

1. [Installed **MetricsHub**](../installation/index.md) on one or more systems that has network access to the resources to be monitored
2. [Configured the monitoring of your resources](../configuration/configure-monitoring.md)
3. [configured the sustainability metrics](../guides/configure-sustainability-metrics.md)
4. Generated an access token as explained in the [Splunk User Documentation](https://docs.splunk.com/observability/en/admin/authentication/authentication-tokens/manage-usage.html). 

## Configuration

### Pushing metrics to Splunk Observability Cloud

1. In the `exporters` section of the `otel/otel-config.yaml` configuration file, 
provide the access token to connect to the SignalFx exporter,
and other additional settings:

    ```yaml
      signalfx:
        access_token: <access_token> # Access token to send data to SignalFx.
        realm: eu0 # SignalFx realm where the data will be received.
        timeout: 10s # Timeout for the send operations.
        sync_host_metadata: true # Whether the exporter should scrape host metadata.
    ```
2. Declare the exporter in the pipeline section of `otel/otel-config.yaml`:

    ```yaml
      service:
        extensions: [health_check, basicauth]
        pipelines:
          metrics:
            receivers: [otlp, prometheus/internal]
            processors: [memory_limiter, batch, metricstransform]
            exporters: [signalfx] # List SignalFx here
    ```
3. Finally, restart **MetricsHub** to ensure that the configuration changes are taken into account.

### Using the Hardware dashboards

**MetricsHub** includes a collection of dashboards to expose infrastructure observability and sustainability KPIs:

Dashboard                                                                                                                  | Description                                                                                     |
-----------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| **Hardware - Main**![Hardware Main - Overview of all monitored sites](../images/splunk-dashboard-hardware-main.png)                            | Overview of all monitored systems, focusing on key hardware and sustainability metrics.         |
|**Hardware - Site**![Hardware Site - Metrics associated to one site and its monitored hosts](../images/splunk-dashboard-hardware-site.png)     | Metrics specific to a particular site (a data center or a server room) and its monitored hosts. |
|**Hardware - Host** ![Hardware Host - Metrics associated to one host and its internal components](../images/splunk-dashboard-hardware-host.png) | Metrics associated with one *host* and its internal devices. |