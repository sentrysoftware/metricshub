keywords: otel, prometheus, collector
description: A simple YAML file configures where ${project.name} must send the data it collects.

# Configure the OpenTelemetry Collector

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

As a regular [OpenTelemetry Collector](https://opentelemetry.io/docs/collector/), several properties of **${project.name}** are [configurable](https://opentelemetry.io/docs/collector/configuration/):

* [the source(s) of the data (`receivers`)](https://opentelemetry.io/docs/collector/configuration/#receivers)
* [the processing of the collected data (`processors`)](https://opentelemetry.io/docs/collector/configuration/#processors)
* [the destination of the processed data (`exporters`)](https://opentelemetry.io/docs/collector/configuration/#exporters)
* [the pipeline](https://opentelemetry.io/docs/collector/configuration/#service)

![Internal architecture of the ${project.name}](../images/otel-internal-architecture.png)

This version of **${project.name}** leverages **version ${otelVersion}** of OpenTelemetry.

By default, **${project.name}**'s configuration file is **config/otel-config.yaml**. You can start the *OpenTelemetry Collector* with the path to an alternate file (see [Installation](../install.md)).

## Receivers

### Hardware Sentry Exporter for Prometheus

The primary source of the data is [`prometheus_exec`](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/receiver/prometheusexecreceiver), which is configured to execute **Hardware Sentry**'s internal *Exporter for Prometheus*, and scrape the collected *metrics*. By default, it executes the internal *Exporter for Prometheus* on port **TCP/24375** and scrapes the metrics every 2 minutes.

```yaml
  prometheus_exec/hws-exporter:
    exec: "\"bin/hws-exporter\" --target.config.file=\"config/hws-config.yaml\" --server.port={{port} }"
    port: 24375
    scrape_interval: 2m
```

There is no need to edit this section, unless you need to configure the internal **Hardware Sentry Exporter for Prometheus** to use a different configuration file than the default one.

You declare multiple instances of `prometheus_exec`, which will run separate instances of **Hardware Sentry Exporter for Prometheus**, each on a different port. You will need to specify alternate configuration files and ports, as in the example below:

```yaml
  prometheus_exec/hws-exporter-1:
    exec: "\"bin/hws-exporter\" --target.config.file=\"config/hws-config-1.yaml\" --server.port={{port} }"
    port: 9011
    scrape_interval: 2m
  prometheus_exec/hws-exporter-2:
    exec: "\"bin/hws-exporter\" --target.config.file=\"config/hws-config-2.yaml\" --server.port={{port} }"
    port: 9012
    scrape_interval: 2m

# [...]

service:
  extensions: [health_check]
  pipelines:
    metrics:
      receivers: [prometheus_exec/hws-exporter-1,prometheus_exec/hws-exporter-2]
```

### OpenTelemetry Collector Internal Exporter for Prometheus

*OpenTelemetry Collector*'s own internal *Exporter for Prometheus*, which runs on port **TCP/8888** (this is configurable with the `--metrics-addr 0.0.0.0:8888` argument), is an optional source of data. This exporter provides internal metrics about the collector activity (see [Health Check](../troubleshooting/status.md)). It's referred to as `prometheus/internal` in the pipeline and leverages the [standard `prometheus` receiver](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/receiver/prometheusreceiver).

```yaml
  prometheus/internal:
    config:
      scrape_configs:
        - job_name: otel-collector-internal
          scrape_interval: 60s
          static_configs:
            - targets: ["0.0.0.0:8888"]
```

## Processors

By default, the collected metrics go through 3 processors:

* [`memory_limiter`](https://github.com/open-telemetry/opentelemetry-collector/tree/main/processor/memorylimiterprocessor) to limit the memory consumed by the *OpenTelemetry Collector* process (configurable)
* [`filter`](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/filterprocessor) to filter the metrics
* [`batch`](https://github.com/open-telemetry/opentelemetry-collector/tree/main/processor/batchprocessor) to process data in batches of 10 seconds (configurable)
* [`metricstransform`](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/metricstransformprocessor) to enrich the collected metrics

This `metricstransform` processor is particularly useful when the receiving platform requires specific labels on the metrics that are not set by default by **Hardware Sentry Exporter for Prometheus**. The `metricstransform` processor has [many options to add, rename, delete labels and metrics](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/metricstransformprocessor).

Note that **Hardware Sentry Exporter for Prometheus** can also be configured to [add additional labels to the collected metrics](configure-exporter.md).

## Exporters

The `exporters` section defines the destination of collected metrics. **${project.name}** version **${project.version}** includes support for the below exporters:

* [OLTP/HTTP](https://github.com/open-telemetry/opentelemetry-collector/blob/main/exporter/otlphttpexporter/README.md)
* [OLTP/gRPC](https://github.com/open-telemetry/opentelemetry-collector/blob/main/exporter/otlpexporter/README.md)
* [`prometheusremotewrite`](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/prometheusremotewriteexporter)
* [Datadog Exporter](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/datadogexporter)
* [logging](https://github.com/open-telemetry/opentelemetry-collector/tree/main/exporter/loggingexporter)

You can configure several exporters in the same instance of the *OpenTelemetry Collector* so the collected metrics are sent to multiple platforms.

Use the above links to learn how to configure these exporters. Specific integration scenarios are also described for:

* [Prometheus Server](../integration/prometheus.md)
* [BMC Helix](../integration/helix.md)

## The Pipeline

Configured receivers, processors and exporters are taken into account **if and only if** they are declared in the pipeline:

```yaml
service:
  extensions: [health_check]
  pipelines:
    metrics:
      receivers: [prometheus_exec/hws-exporter]
      processors: [memory_limiter,batch,metricstransform]
      exporters: [prometheusremotewrite/your-server]
```
