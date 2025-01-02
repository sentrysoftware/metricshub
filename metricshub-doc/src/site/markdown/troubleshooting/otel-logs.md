keywords: logs
description: How to enable the logging mode of the OpenTelemetry Collector.

# OTel Logs

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

The **OTel Collector** is in charge of pulling metrics, traces, and logs periodically and pushing them to the observability platform. Enable its debug mode if data is missing to obtain the `otelcol-$timestamp.log` log and get more details about the **MetricsHub** operations (initialization, pipeline, termination.

## Enable debug

First set the log `level` to `error`, `warn`, or `debug` in the **otel/otel-config.yaml** file:

```yaml
service:
  telemetry:
    logs:
      level: debug
  extensions: [health_check]
  pipelines:
  # [...]
```

Then, restart **MetricsHub** for these new settings to be considered.

Finally, check the **logs/otelcol-\<timestamp\>.log** file, where `<timestamp>` is the time at which the log was started.

> Note: The **logs/otelcol-\<timestamp\>.log** file is reset each time the *Collector* is started. Previous logs are identified with the `<timestamp>` value (ex: `otelcol-2022-09-19-02-05-18.log`). **MetricsHub** rotates the **otelcol-\<timestamp\>.log** file when it reaches a maximum size of **100MB** and retains old log files for **2 days**.

### What to look for in otelcol-\<timestamp\>.log

First check that the **MetricsHub Agent** successfully launched the *OpenTelemetry Collector*.

Then check that the exporters and processors properly started.

Finally look for any connection issues or authentication failures to the configured observability platform(s) (Datadog, BMC Helix, Prometheus, Grafana, etc.).

### Get more details about the exported data

You can enable the `logging` exporter in the **otel/otel-config.yaml** file to check which metrics, labels, and values are sent by the *Collector* to the observability platforms and verify that the configured processors did not alter the collected data.

First, list the `logging` exporter under the `exporters` section and set `verbosity` to `detailed`:

```yaml
exporters:
# [...]
  logging:
    verbosity: detailed
```

Then, declare the `logging` exporter in the pipeline:

```yaml
service:
  pipelines:
    metrics:
      receivers: # receivers
      processors: # processors
      exporters: [prometheusremotewrite/your-server,logging] # <-- added logging
```

Restart the *Collector* for the new settings to be considered.

The metric name, its labels and value are listed in the **logs/otelcol-\<timestamp\>.log** file.

> **Important**: Disable the `logging` exporter when unused as its operation may affect the overall performance of the *Collector* and fill your file system.

### Reduce the amount of information logged

To reduce the amount of information logged, you can configure the `filter` processor to only log metrics of specific hosts. In the example below, we configured the `filter/keep1HostOnly` processor to only log information about systems whose hostname contains `my-server.big-corp.com`:

```yaml
processors:
  filter/keep1HostOnly:
    metrics:
      include:
        match_type: expr
        expressions:
        - Label("host.name") == "my-server.big-corp.com"
```

We then declared the `filter/keep1HostOnly` processor in the pipeline and restarted the *Collector*:

```yaml
service:
  pipelines:
    metrics:
      receivers: [otlp, prometheus/internal]
      processors: [memory_limiter,batch,filter/keep1HostOnly] # <-- added filter
      exporters: # exporters
```

> **Important**: Remove the `filter` processor from your pipeline once the troubleshooting is completed.
