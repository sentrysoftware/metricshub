keywords: debug
description: How to enable the debug mode of the Hardware Sentry Agent (core engine) and the OpenTelemetry Collector.

# Debugging

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

Depending on the issue you are experiencing, you will have to either enable the debug mode for the **Hardware Sentry Agent** or for the **OpenTelemetry Collector**.

The **Hardware Sentry Agent** (core engine) is a Java process which launches the _OpenTelemetry Collector_ and performs the hardware monitoring by connecting to each of the configured hosts and retrieving information about their hardware components. Enable its debug mode if there's no data available in your observability platform.

The **OpenTelemetry Collector** is in charge of pulling metrics, traces, and logs periodically and pushing them to the observability platform. Enable its debug mode, if data is missing (the monitoring coverage is incomplete). 

## Hardware Sentry Agent (core engine)

To enable the debug mode of the core engine, edit the **config/hws-config.yaml** file and add the `loggerLevel` property:

```yaml
loggerLevel: debug
hosts:
- host:
    # [...]
```

Set `loggerlevel` to either `all`, `trace`, `debug`, `info`, `warn`, `error`, `fatal`, `off`.

The debug output file is saved by default in the **logs** directory located under the **Hardware Sentry** home directory.

Examples:

* **C:\Program Files\hws\logs** on Windows
* **/opt/hws/logs** on Linux

To specify a different output directory, edit the **hws-config.yaml** file and add the `outputDirectory` parameter before the `hosts` section:

```yaml
loggerLevel: debug
outputDirectory: C:\Users\<username>\AppData\Local\Temp\logs2021

hosts:
- host:
    # [...]
```

## OpenTelemetry Collector

To get more details about the **${solutionName}** operations (initialization, pipeline, termination), first set the log `level` to `error`, `warn`, or `debug` in the **otel/otel-config.yaml** file:

```yaml
service:
  telemetry:
    logs:
      level: debug
  extensions: [health_check]
  pipelines:
  # [...]
```

Then, restart **${solutionName}** for these new settings to be taken into account.

Finally, check the **logs/otelcol-\\${timestamp}.log** file, where `\${timestamp}` is the time at which the log was started.

> Note: The **logs/otelcol-\\${timestamp}.log** file is reset each time the *Collector* is started. Previous logs are identified with the `\${timestamp}` value (ex: `otelcol-2022-09-19-02-05-18.log`). **${solutionName}** rotates the **otelcol-\\${timestamp}.log** file when it reaches a maximum size of **100MB** and retains old log files for **2 days**.

### What to look for in otelcol-\\${timestamp}.log

First check that the **Hardware Sentry Agent** successfully launched the _OpenTelemetry Collector_:

```log
[2022-09-19T14:05:18,459][INFO ][c.s.h.a.p.r.Executable] Started process command line: [C:\Program Files\hws\app\..\otel\otelcol-contrib, --config, "C:\Program Files\hws\app\..\otel\otel-config.yaml", --feature-gates=pkg.translator.prometheus.NormalizeName]
[2022-09-19T14:05:19,363][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:19.363+0200	info	service/telemetry.go:115	Setting up own telemetry...
[2022-09-19T14:05:19,363][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:19.363+0200	info	service/telemetry.go:156	Serving Prometheus metrics	{"address": "localhost:8888", "level": "basic"}
[2022-09-19T14:05:19,775][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:19.774+0200	info	pipelines/pipelines.go:74	Starting exporters...
[2022-09-19T14:05:19,775][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:19.774+0200	info	pipelines/pipelines.go:78	Exporter is starting...	{"kind": "exporter", "data_type": "metrics", "name": "prometheusremotewrite/nb-prom"}
[2022-09-19T14:05:19,775][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:19.774+0200	info	pipelines/pipelines.go:82	Exporter started.	{"kind": "exporter", "data_type": "metrics", "name": "prometheusremotewrite/nb-prom"}
[2022-09-19T14:05:19,775][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:19.774+0200	info	pipelines/pipelines.go:78	Exporter is starting...	{"kind": "exporter", "data_type": "metrics", "name": "prometheus"}
[2022-09-19T14:05:19,776][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:19.775+0200	info	pipelines/pipelines.go:82	Exporter started.	{"kind": "exporter", "data_type": "metrics", "name": "prometheus"}
[2022-09-19T14:05:19,776][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:19.775+0200	info	pipelines/pipelines.go:78	Exporter is starting...	{"kind": "exporter", "data_type": "metrics", "name": "datadog/api"}
[2022-09-19T14:05:19,776][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:19.775+0200	info	pipelines/pipelines.go:82	Exporter started.	{"kind": "exporter", "data_type": "metrics", "name": "datadog/api"}
[2022-09-19T14:05:19,776][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:19.775+0200	info	pipelines/pipelines.go:78	Exporter is starting...	{"kind": "exporter", "data_type": "logs", "name": "logging"}
[2022-09-19T14:05:19,776][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:19.775+0200	info	pipelines/pipelines.go:82	Exporter started.	{"kind": "exporter", "data_type": "logs", "name": "logging"}
[2022-09-19T14:05:19,776][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:19.775+0200	info	pipelines/pipelines.go:86	Starting processors...
[2022-09-19T14:05:19,776][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:19.775+0200	info	pipelines/pipelines.go:90	Processor is starting...	{"kind": "processor", "name": "metricstransform", "pipeline": "metrics"}
[2022-09-19T14:05:19,776][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:19.775+0200	info	pipelines/pipelines.go:94	Processor started.	{"kind": "processor", "name": "metricstransform", "pipeline": "metrics"}
[2022-09-19T14:05:19,776][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:19.775+0200	info	pipelines/pipelines.go:90	Processor is starting...	{"kind": "processor", "name": "resourcedetection", "pipeline": "metrics"}
[2022-09-19T14:05:19,776][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:19.775+0200	info	internal/resourcedetection.go:136	began detecting resource information	{"kind": "processor", "name": "resourcedetection", "pipeline": "metrics"}
[2022-09-19T14:05:20,834][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:20.834+0200	info	internal/resourcedetection.go:150	detected resource information	{"kind": "processor", "name": "resourcedetection", "pipeline": "metrics", "resource": {"host.name":"pc-nassim.internal.sentrysoftware.net","os.type":"windows"}}
[2022-09-19T14:05:20,835][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:20.834+0200	info	pipelines/pipelines.go:94	Processor started.	{"kind": "processor", "name": "resourcedetection", "pipeline": "metrics"}
[2022-09-19T14:05:20,835][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:20.834+0200	info	pipelines/pipelines.go:90	Processor is starting...	{"kind": "processor", "name": "batch", "pipeline": "metrics"}
[2022-09-19T14:05:20,835][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:20.834+0200	info	pipelines/pipelines.go:94	Processor started.	{"kind": "processor", "name": "batch", "pipeline": "metrics"}
[2022-09-19T14:05:20,835][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:20.834+0200	info	pipelines/pipelines.go:90	Processor is starting...	{"kind": "processor", "name": "memory_limiter", "pipeline": "metrics"}
[2022-09-19T14:05:20,835][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:20.834+0200	info	pipelines/pipelines.go:94	Processor started.	{"kind": "processor", "name": "memory_limiter", "pipeline": "metrics"}
[2022-09-19T14:05:20,835][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:20.834+0200	info	pipelines/pipelines.go:90	Processor is starting...	{"kind": "processor", "name": "resourcedetection", "pipeline": "logs"}
[2022-09-19T14:05:20,835][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:20.834+0200	info	pipelines/pipelines.go:94	Processor started.	{"kind": "processor", "name": "resourcedetection", "pipeline": "logs"}
[2022-09-19T14:05:20,835][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:20.834+0200	info	pipelines/pipelines.go:90	Processor is starting...	{"kind": "processor", "name": "batch", "pipeline": "logs"}
[2022-09-19T14:05:20,835][DEBUG][c.s.h.a.s.o.p.OtelCollectorExecutable] 2022-09-19T14:05:20.834+0200	info	pipelines/pipelines.go:94	Processor started.	{"kind": "processor", "name": "batch", "pipeline": "logs"}

```

Then check that the exporters and processors properly started. 

Finally look for any connection issues or authentication failures to the configured observability platform(s) (Datadog, BMC Helix, Prometheus, Grafana, etc.).

### Getting more details about the exported data

You can enable the `logging` exporter in the **otel/otel-config.yaml** file to check which metrics, labels, and values are sent by the _Collector_ to the observability platforms and verify that the configured processors did not alter the collected data.

First, list the  `logging` exporter under the `exporters` section and set `loglevel` to `debug`:

```yaml
exporters:
# [...]
  logging:
    loglevel: debug
```

then declare the `logging` exporter in the pipeline:

```yaml
service:
  pipelines:
    metrics:
      receivers: # receivers
      processors: # processors
      exporters: [prometheusremotewrite/your-server,logging] # <-- added logging
```

Restart the _Collector_ for the new settings to be taken into account.

The metric name, its labels and value are listed in the **logs/otelcol-\\${timestamp}.log** file.

> **Important**: Disable the `logging` exporter when unused as its operation may affect the overall performance of the _Collector_ and fill your file system.

### Reducing the amount of information logged

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

We then declared the `filter/keep1HostOnly` processor in the pipeline and restarted the _Collector_:

```yaml
service:
  pipelines:
    metrics:
      receivers: [otlp, prometheus/internal]
      processors: [memory_limiter,batch,filter/keep1HostOnly] # <-- added filter
      exporters: # exporters
```

> **Important**: Remove the `filter` processor from your pipeline once the troubleshooting is completed.
