keywords: job, configuration, prometheus
description: How to configure the Prometheus server to pull information from the Hardware Sentry Exporter for Prometheus

# Integration with Prometheus Server

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

There are 2 ways to integrate **${project.name}** with [Prometheus Server](https://prometheus.io/docs/introduction/overview/):

* **Push**, where each *OpenTelemetry Collector* pushes the metrics to the Prometheus Server
* **Pull**, where the Prometheus Server connects to each *OpenTelemetry Collector* instance to scrape its metrics

We recommend the **Push** method as its network requirements (firewall) are lower and the configuration simpler.

## Push (*Remote Write*)

In this setup, each instance of **${project.name}** is configured to send the collected metrics to the Prometheus Server, using the *Remote Write Protocol*.

Your Prometheus Server must be configured to allow the *Remote Write* feature:

* [Enable the Remote Write Receiver](https://prometheus.io/docs/prometheus/latest/feature_flags/#remote-write-receiver) with the `--enable-feature=remote-write-receiver` option
* [Configure the Remote Write Receiver](https://prometheus.io/docs/prometheus/latest/configuration/configuration/#remote_write)

Once *Remote Write* is enabled and configured on your Prometheus Server, edit the `exporters` section of the [config/otel-config.yaml](../configuration/configure-otel.md) configuration file as in the below example:

```yaml
exporters:
  prometheusremotewrite/your-prom-server:
    endpoint: http://your-prom-server:9090/api/v1/write
```

You can customize the [`prometheusremotewrite` exporter configuration](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/prometheusremotewriteexporter) to match your Prometheus Server's configuration, notably the `endpoint` URL.

Make sure to declare the exporter in the pipeline section of **config/otel-config.yaml**:

```yaml
service:
  extensions: [health_check]
  pipelines:
    metrics:
      receivers: [prometheus_exec/hws-exporter,prometheus/internal]
      processors: [memory_limiter,batch,metricstransform]
      exporters: [prometheusremotewrite/your-prom-server] # Your prometheusremotewrite exporter must be listed here
```

## Pull (*Scrape*)

In this setup, each instance of **${project.name}** exposes the collected metrics via HTTP, and the Prometheus Server connects to each instance to scrape the `/metrics` endpoint. You need to ensure the Prometheus Server has network access to each collector.

By default, the internal **Hardware Sentry Exporter for Prometheus** is started on port **TCP/24375**. The port can be [modified in the `receivers` section of **config/otel-config.yaml**](../configuration/configure-otel.md).

As the Prometheus Server will communicate directly with the internal **Hardware Sentry Exporter for Prometheus**, the *OpenTelemetry Collector* pipeline is completely skipped (receivers, processors and exporters). In fact, in this scenario, you can even decide to run the **Hardware Sentry Exporter for Prometheus** process standalone, without the *OpenTelemetry Collector*.

Once **Hardware Sentry Exporter for Prometheus** is running, you can configure a job in the [`scrape_configs` section of your Prometheus Server configuration](https://prometheus.io/docs/prometheus/latest/configuration/configuration/#scrape_config):

```yaml
  - job_name: hardware_sentry
    scrape_interval: <duration>
    scrape_timeout: <duration>
    static_configs:
    scheme: <http or https>
    - targets: ['<hostname:port_number>' ]
```

`<duration>` is a duration that **must be greater than the `collectPeriod`** defined in [config/hws-config.yaml](../configuration/configure-exporter.md) to avoid gaps and duplicate points in the metrics, which will affect the calculation of rates.

`scheme` is a string that can take the values `http` or `https`. If you need HTTPS to encrypt communications between your Prometheus Server and the **Hardware Sentry Exporter for Prometheus**, you need to start the exporter with the `--server.ssl.enabled=true` option. This can be done in the [`receivers` section of **config/otel-config.yaml**](../configuration/configure-otel.md).

Example:

```yaml
  - job_name: hardware_sentry
    scrape_interval: 2m
    scrape_timeout: 30s
    static_configs:
    - targets: ['hws-exporter-siteA:8080']
```
