keywords: otel, prometheus, collector, gRPC
description: A simple YAML file configures where ${project.name} must send the data it collects.

# Configure the OpenTelemetry Collector

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

As a regular [OpenTelemetry Collector](https://opentelemetry.io/docs/collector/), several properties of **${project.name}** are [configurable](https://opentelemetry.io/docs/collector/configuration/):

* [the components added to the collector (`extensions`)](https://opentelemetry.io/docs/collector/configuration/#extensions)
* [the source(s) of the data (`receivers`)](https://opentelemetry.io/docs/collector/configuration/#receivers)
* [the processing of the collected data (`processors`)](https://opentelemetry.io/docs/collector/configuration/#processors)
* [the destination of the processed data (`exporters`)](https://opentelemetry.io/docs/collector/configuration/#exporters)
* [the pipeline](https://opentelemetry.io/docs/collector/configuration/#service)

![Internal architecture of the ${project.name}](../images/otel-internal-architecture.png)

This version of **${project.name}** leverages **version ${otelVersion}** of OpenTelemetry.

By default, **${project.name}**'s configuration file is **config/otel-config.yaml**. You can start the *OpenTelemetry Collector* with the path to an alternate file (see [Installation](../install.md)).

## Extensions

### Hardware Sentry Agent

The **Hardware Sentry Agent** is the internal component which scrapes targets, collects metrics and pushes OTLP data to the OTLP receiver of the *OpenTelemetry Collector*. The `hws_agent` extension starts the **Hardware Sentry Agent** as a child process of the *OpenTelemetry Collector*.

```yaml
  hws_agent:
    grpc: http://localhost:4317
```

The `hws_agent` extension checks that **Hardware Sentry Agent** is up and running and restarts its process if needed.

The example above shows how to configure **Hardware Sentry Agent** to push metrics to the local _OTLP receiver_ using [gRPC](https://grpc.io/) on port **TCP/4317**.
If your OTLP receiver runs on another host or uses a different protocol or port, you will need to update the `grpc` option. Format: `<http|https>://<host>:<port>`.

By default, the **Hardware Sentry Agent**'s configuration file is **config/hws-config.yaml**. You can provide an alternate configuration file using the `--config` argument in the `extra_args` section.

```yaml
  hws_agent:
    grpc: http://localhost:4317
    extra_args:
      - --config=config\hws-config-2.yaml
```

To know how to configure the **Hardware Sentry Agent**, see [Monitoring Configuration](configure-agent.md)

#### Configuration

The `hws_agent` can be configured as the following: 
```yaml
  hws_agent:
```

Eventually configure these settings:

* `grpc`: the endpoint to which the **Hardware Sentry Agent** is going to push OpenTelemetry data. Default: `http://localhost:4317`.
* `extra_args`: the additional arguments for the **Hardware Sentry Agent**, such as `--config=config\hws-config-2.yaml`.
* `restart_delay`: to indicate the period of time after which the **Hardware Sentry Agent** is restarted when a problem has been detected. If  not set, the **Hardware Sentry Agent** will be restarted after 10 sec.
* `retries`: Number of restarts to be triggered until the **Hardware Sentry Agent** is up and running again. If not set, the extension will try restarting the **Hardware Sentry Agent** until it is up and running.

## Receivers

### OTLP gRPC

The primary data source is [`OTLP Receiver`](https://github.com/open-telemetry/opentelemetry-collector/tree/main/receiver/otlpreceiver), which is configured to receive the metrics collected by the  **Hardware Sentry Agent** via [gRPC](https://grpc.io/) on port **TCP/4317**.

```yaml
  otlp:
    protocols:
        grpc:
```

Do not edit this section unless you want the **Hardware Sentry Agent** to use a different configuration. See [Hardware Sentry Agent](#Hardware_Sentry_Agent)

### OpenTelemetry Collector Internal Exporter for Prometheus

The *OpenTelemetry Collector*'s internal *Exporter for Prometheus* is an optional source of data. It provides information about the collector activity (see [Health Check](../troubleshooting/status.md)). It's referred to as `prometheus/internal` in the pipeline and leverages the [standard `prometheus` receiver](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/receiver/prometheusreceiver).

You can use the `--metrics-addr 0.0.0.0:8888` argument to set the port it is running on (by default: **TCP/8888**).

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

By default, the collected metrics go through 5 processors:

* [`memory_limiter`](https://github.com/open-telemetry/opentelemetry-collector/tree/main/processor/memorylimiterprocessor) to limit the memory consumed by the *OpenTelemetry Collector* process (configurable)
* [`filter`](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/filterprocessor) to filter the metrics
* [`batch`](https://github.com/open-telemetry/opentelemetry-collector/tree/main/processor/batchprocessor) to process data in batches of 10 seconds (configurable)
* [`resourcedetection`](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/resourcedetectionprocessor) to associate local metrics with the actual host name of the system the collector is running on (instead of simply `localhost`)
* [`metricstransform`](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/metricstransformprocessor) to enrich the collected metrics.

This `metricstransform` processor is particularly useful when the receiving platform requires specific metrics labels that are not set by default by the **Hardware Sentry Agent**. The `metricstransform` processor has [many options to add, rename, delete labels and metrics](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/metricstransformprocessor).

Note that **Hardware Sentry Agent** can also be configured to [provide additional labels to the collected metrics](configure-agent.md).

## Exporters

The `exporters` section defines the destination of the collected metrics. **${project.name}** version **${project.version}** includes support for the below exporters:

* [OLTP/HTTP](https://github.com/open-telemetry/opentelemetry-collector/blob/main/exporter/otlphttpexporter/README.md)
* [OLTP/gRPC](https://github.com/open-telemetry/opentelemetry-collector/blob/main/exporter/otlpexporter/README.md)
* [`prometheusremotewrite`](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/prometheusremotewriteexporter)
* [`prometheusexporter`](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/prometheusexporter)
* [Datadog Exporter](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/datadogexporter)
* [logging](https://github.com/open-telemetry/opentelemetry-collector/tree/main/exporter/loggingexporter)

You can configure several exporters in the same instance of the *OpenTelemetry Collector* so the collected metrics are sent to multiple platforms.

Use the above links to learn how to configure these exporters. Specific integration scenarios are also described for:

* [Prometheus Server](../integration/prometheus.md)
* [BMC Helix](../integration/helix.md)

## The Pipeline

Configured extensions, receivers, processors and exporters are taken into account **if and only if** they are declared in the pipeline:

```yaml
service:
  telemetry:
    logs:
      level: info # Change to debug more more details
  extensions: [health_check, hws_agent]
  pipelines:
    metrics:
      receivers: [otlp, prometheus/internal]
      processors: [memory_limiter, batch, resourcedetection, metricstransform]
      exporters: [prometheusremotewrite/your-server, prometheus] # List here the platform of your choice
```
