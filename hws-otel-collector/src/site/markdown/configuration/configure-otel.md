keywords: receivers, processors, exporters
description: A simple YAML file configures where ${project.name} must send the data it collects.

# OpenTelemetry Advanced Settings

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

**${project.name}** comes with a default configuration file (`otel/otel-config-example.yaml`) which is intended to work for most situations and only requires minor changes for **${project.name}** to operate properly (refer to the *Integration* pages for more information).

  > This page is therefore intended for **advanced users** who have a **deep knowledge of OpenTelemetry** and wish to learn more about the properties available in `otel/otel-config.yaml`.

As a regular *OpenTelemetry Collector*, **${project.name}** consists of:

* receivers
* processors
* exporters
* and several extensions.

![Internal architecture of the ${project.name}](../images/hws-internal-architecture.png)

This version of **${project.name}** leverages **version ${otelVersion}** of OpenTelemetry.

## Receivers

### OTLP gRPC

> **Warning**: Only update this section if you customized the [Hardware Sentry Agent settings](configure-agent.md#additional-settings-optional).
The **Hardware Sentry Agent** pushes the collected data to the [`OTLP Receiver`](https://github.com/open-telemetry/opentelemetry-collector/tree/main/receiver/otlpreceiver) via [gRPC](https://grpc.io/) on port **TCP/4317**.

The `OTLP Receiver` is configured by default with the self-signed certificate `security/otel.crt` and the private key `security/otel.key` to enable the TLS protocol. If you wish to set your own certificate file, configure the **Hardware Sentry Agent** with the correct [Trusted Certificates File](configure-agent.html#Trusted_certificates_file). Because the `OTLP Exporter` of the **Hardware Sentry Agent** performs hostname verification, you will also have to add the `localhost` entry (`DNS:localhost,IP:127.0.0.1`) to the `Subject Alternative Name (SAN)` extension of the new generated certificate.

Clients requests are authenticated with the [Basic Authenticator extension](#Basic_Authenticator).

```yaml
  otlp:
    protocols:
      grpc:
        endpoint: localhost:4317
        tls:
          cert_file: ../security/otel.crt
          key_file: ../security/otel.key
        auth:
          authenticator: basicauth
```

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

* [`metricstransform`](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/metricstransformprocessor) to enrich the collected metrics, typically with labels required by the observability platforms. The `metricstransform` processor has [many options to add, rename, delete labels and metrics](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/metricstransformprocessor). Note that **Hardware Sentry Agent** can also be configured to [enrich the collected metrics with extra labels](configure-agent.html#Extra_labels).
* [`memory_limiter`](https://github.com/open-telemetry/opentelemetry-collector/tree/main/processor/memorylimiterprocessor) to limit the memory consumed by the *OpenTelemetry Collector* process (configurable)
* [`resourcedetection`](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/resourcedetectionprocessor) to find out the actual host name of the system monitored
* [`filter`](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/filterprocessor) to include or exclude metrics
* [`batch`](https://github.com/open-telemetry/opentelemetry-collector/tree/main/processor/batchprocessor) to process data in batches of 10 seconds (configurable).

## Exporters

The `exporters` section defines the destination of the collected metrics. **${project.name}** version **${project.version}** includes support for all the [OpenTelemetry Collector Contrib exporters](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter), such as:

* [OLTP/gRPC Exporter](https://github.com/open-telemetry/opentelemetry-collector/blob/main/exporter/otlpexporter/README.md)
* [Prometheus Remote Write Exporter](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/prometheusremotewriteexporter)
* [Prometheus Exporter](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/prometheusexporter)
* [Datadog Exporter](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/datadogexporter)
* [Logging Exporter](https://github.com/open-telemetry/opentelemetry-collector/tree/main/exporter/loggingexporter)
* [Splunk SignalFx Exporter](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/signalfxexporter)
* [Dynatrace Exporter](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/dynatraceexporter)
* [Instana Exporter](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/instanaexporter)
* and [many more...](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter)

You can configure several exporters in the same instance of the *OpenTelemetry Collector* to send the collected metrics to multiple platforms.

Use the above links to learn how to configure these exporters. Specific integration scenarios are also described for:

* [Datadog](../integration/datadog.md)
* [Prometheus Server](../prometheus/prometheus.md)
* [BMC Helix](../integration/helix.md)

## Extensions

### HealthCheck

The [healthcheck](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/extension/healthcheckextension) extension checks the status of **${project.name}** . It is activated by default and runs on port 13133 ([`http://localhost:13133`](http://localhost:13133)).

Refer to [Check the collector is up and running](../troubleshooting/status.html#Check_collector_is_up_and_running) for more details.

### zpages

The **zpages** extension provides debug information about all the different components. It notably provides:

* general information about **${project.name}**
* details about the active pipeline
* activity details of each receiver and exporter configured in the pipeline.

Refer to [Check the pipelines status](../troubleshooting/status.html#Check_the_pipelines_status) for more details.

### Basic Authenticator

The [`Basic Authenticator`](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/extension/basicauthextension) extension authenticates the `OTLP Exporter` requests by comparing the *Authorization* header sent by the `OTLP Exporter` and the credentials provided in the `security/.htpasswd` file.
Refer to the [Apache htpasswd](https://httpd.apache.org/docs/2.4/programs/htpasswd.html) documentation to know how to manage user files for basic authentication.

```yaml
  basicauth:
    htpasswd:
      file: ../security/.htpasswd
```

The `.htpasswd` file is stored in the `security` directory.

> **Warning**: If a different password is specified in the `.htpasswd` file, update the [Basic Authentication Header](configure-agent.md#Basic_Authentication_Header) of the **Hardware Sentry Agent**.

## The Pipeline

Configured extensions, receivers, processors and exporters are taken into account **if and only if** they are declared in the pipeline:

```yaml
service:
  telemetry:
    logs:
      level: info # Change to debug for more details
    metrics:
      address: localhost:8888
      level: basic
  extensions: [health_check, basicauth]
  pipelines:
    metrics:
      receivers: [otlp, prometheus/internal]
      processors: [memory_limiter, batch, resourcedetection, metricstransform]
      exporters: [prometheusremotewrite/your-server] # List here the platform of your choice

    # Uncomment the section below to enable logging of hardware alerts.
    # logs:
    #   receivers: [otlp]
    #   processors: [memory_limiter, batch, resourcedetection]
    #   exporters: [logging] # List here the platform of your choice
```