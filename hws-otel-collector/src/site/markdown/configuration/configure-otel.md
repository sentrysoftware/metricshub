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

The **Hardware Sentry Agent** is the internal component which scrapes hosts, collects metrics and pushes OTLP data to the OTLP receiver of the *OpenTelemetry Collector*. The `hws_agent` extension starts the **Hardware Sentry Agent** as a child process of the *OpenTelemetry Collector*, checks that this child process is up and running and restarts it if needed.

Configure the `hws_agent` extension as follows:
```yaml
  hws_agent:
    grpc: <http|https>://<host>:<port>   # Default: https://localhost:4317
    extra_args: [ <string> ... ]         # Example: [ --config=config/alternate-configuration-file.yaml ]
    restart_delay: <duration>            # Default: 10s
    retries: <int>                       # Default: -1 (Means no limit)
```
where:
- `grpc` is the endpoint to which the **Hardware Sentry Agent** will push OpenTelemetry data. By default, the **Hardware Sentry Agent** pushes metrics to the local *OTLP receiver* using [gRPC](https://grpc.io/) on port **TCP/4317** (By default: `https://localhost:4317`).
- `extra_args` specifies a list of additional arguments to be used by the **Hardware Sentry Agent**. By default, the **Hardware Sentry Agent**'s configuration file is **./config/hws-config.yaml** but you can provide an alternate configuration file by adding a new extra argument. Example: `--config=C:\Program Files\hws-otel-collector\config\hws-config-2.yaml`.
- `restart_delay` specifies the period of time after which the **Hardware Sentry Agent** is restarted when a problem has been detected. If not set, the **Hardware Sentry Agent** will be restarted after 10 seconds.
- `retries` specifies the number of restarts to be triggered until the **Hardware Sentry Agent** is up and running again. If not set, the extension will try restarting the **Hardware Sentry Agent** until it is up and running.

To know how to configure the **Hardware Sentry Agent**, see [Monitoring Configuration](configure-agent.md)

### Basic Authenticator

The [`Basic Authentication Extension`](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/extension/basicauthextension) authenticates the `OTLP Exporter` requests by comparing the *Authorization* header sent by the `OTLP Exporter` and the credentials provided in the `security/.htpasswd` file.
Refer to the [Apache htpasswd](https://httpd.apache.org/docs/2.4/programs/htpasswd.html) documentation to know how to manage user files for basic authentication.

```yaml
  basicauth:
    htpasswd:
      file: security/.htpasswd
```

The `.htpasswd` file is stored in the `security` directory.

> **Warning**: If a different password is specified in the `.htpasswd` file, update the [Basic Authentication Header](configure-agent.md#Basic_Authentication_Header) of the **Hardware Sentry Agent**.

## Receivers

### OTLP gRPC

The primary data source is [`OTLP Receiver`](https://github.com/open-telemetry/opentelemetry-collector/tree/main/receiver/otlpreceiver), which is configured to receive the metrics collected by the  **Hardware Sentry Agent** via [gRPC](https://grpc.io/) on port **TCP/4317**.

To make network communications encrypted, by default, the `OTLP Receiver` is configured with the self-signed certificate `security/otel.crt` and the private key `security/otel.key` to enable the TLS protocol

Clients requests are authenticated through the [Basic Authenticator](#Basic_Authenticator) type `basicauth`.

```yaml
  otlp:
    protocols:
      grpc:
        endpoint: localhost:4317
        tls:
          cert_file: security/otel.crt
          key_file: security/otel.key
        auth:
          authenticator: basicauth
```

If you want to set your own certificate file, please make sure to configure the **Hardware Sentry Agent** to set the correct [Trusted Certificates File](configure-agent.md#Trusted_Certificates_File).

Since the communication operates on the same host, you need to add the `localhost` entry (`DNS:localhost,IP:127.0.0.1`) to the `Subject Alternative Name (SAN)` extension of your new generated certificate because the **Hardware Sentry Agent**'s `OTLP Exporter` performs the hostname verification by default.

> **Warning**: Do not edit this section unless you want the **Hardware Sentry Agent** to use a different configuration. See [Hardware Sentry Agent](#Hardware_Sentry_Agent).

### OpenTelemetry Collector Internal Exporter for Prometheus

The *OpenTelemetry Collector*'s internal *Exporter for Prometheus* is an optional source of data. It provides information about the collector activity (see [Health Check](../troubleshooting/status.md)). It's referred to as `prometheus/internal` in the pipeline and leverages the [standard `prometheus` receiver](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/receiver/prometheusreceiver).

```yaml
  prometheus/internal:
    config:
      scrape_configs:
        - job_name: otel-collector-internal
          scrape_interval: 60s
          static_configs:
            - targets: [ localhost:8888 ]
```

Under the `service:telemetry:metrics` section, you can set the metrics `level` or the `address` of the OpenTelemetry Collector Internal Exporter (by default: **localhost:8888**).

```yaml
service:
  telemetry:
    metrics:
      address: localhost:8888
      level: basic
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

* [OLTP/HTTP Exporter](https://github.com/open-telemetry/opentelemetry-collector/blob/main/exporter/otlphttpexporter/README.md)
* [OLTP/gRPC Exporter](https://github.com/open-telemetry/opentelemetry-collector/blob/main/exporter/otlpexporter/README.md)
* [Prometheus Remote Write Exporter](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/prometheusremotewriteexporter)
* [Prometheus Exporter](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/prometheusexporter)
* [Datadog Exporter](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/datadogexporter)
* [Logging Exporter](https://github.com/open-telemetry/opentelemetry-collector/tree/main/exporter/loggingexporter)
* [Splunk SignalFx Exporter](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/signalfxexporter)

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
    metrics:
      address: localhost:8888
      level: basic
  extensions: [health_check, basicauth, hws_agent]
  pipelines:
    metrics:
      receivers: [otlp, prometheus/internal]
      processors: [memory_limiter, batch, resourcedetection, metricstransform]
      exporters: [prometheusremotewrite/your-server] # List here the platform of your choice
    logs:
      receivers: [otlp]
      processors: [memory_limiter, batch, resourcedetection]
      exporters: [] # List here the platform of your choice     
```
