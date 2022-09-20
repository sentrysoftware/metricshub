keywords: debug
description: There are several options to debug ${project.name} and troubleshoot its activity.

# Debugging

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

As there are 2 separate processes in **${project.name}**, there are 2 separate debugging mechanisms. Depending on the issue you're experiencing, you may need to troubleshoot one or the other of these main components:

1. **Hardware Sentry Agent**, a Java process that launches the _OpenTelemetry Collector_ and performs the actual hardware monitoring, connecting to each of the monitored hosts and retrieving information about their hardware components (core engine).
2. **OpenTelemetry Collector**, which is in charge of pulling metrics, traces and logs periodically and pushing them to the destination.

If you're not getting any data at all at the destination framework, you are probably facing issues with the _Collector_. If some data is missing (the monitoring coverage is incomplete), it's more likely a problem with the core engine.

## Hardware Sentry Agent

To enable the debug mode of the core engine, edit the **config/hws-config.yaml** file and add the `loggerLevel` property as in the below example:

```yaml
loggerLevel: debug
hosts:
- host:
    # [...]
```

Possible values are: `all`, `trace`, `debug`, `info`, `warn`, `error`, `fatal`, `off`.

By default, the debug output file is saved in the **logs** directory under the **Hardware Sentry OpenTelemetry Collector** home directory, examples:

* **C:\Program Files\hws-otel-collector\logs** on Windows
* **/usr/local/hws-otel-collector/logs** on Linux

If you want to specify another output directory, edit the **hws-config.yaml** file and add the `outputDirectory` parameter just before the `hosts` section:

```yaml
loggerLevel: debug
outputDirectory: C:\Users\<username>\AppData\Local\Temp\logs2021

hosts:
- host:
    # [...]
```

## OpenTelemetry Collector

The **${project.name}** writes details about its operations (from initialization, to pipeline, to termination) to the **logs/otelcol-\${timestamp}.log** file, where `${timestamp}` is the time at which the log was started.

This **logs/otelcol-\${timestamp}.log** file is reset each time the _Collector_ is started. Previous logs are identified with the `${timestamp}` value, example: `otelcol-2022-09-19-02-05-18.log`.

The **${project.name}** rotates the **otelcol-\${timestamp}.log** file when it reaches a maximum size of **100MB** and retains old log files for **2 days**. 

The level of details in **logs/otelcol-\${timestamp}.log** is configured in **otel/otel-config.yaml**. Set the log level to `error`, `warn`, `info` (default), or `debug` to get more details as in the below example:

```yaml
service:
  telemetry:
    logs:
      level: debug
  extensions: [health_check]
  pipelines:
  # [...]
```

You need to restart the **${project.name}** for these new settings to be taken into account.

### What to look for in otelcol-\${timestamp}.log

The first critical task the **Hardware Sentry Agent** has to complete is to launch _OpenTelemetry Collector_. Check in **logs/otelcol-\${timestamp}.log** that the sub-process is properly started and did not encounter any issue:

```log
[2022-09-19T14:05:18,459][INFO ][c.s.h.a.p.r.Executable] Started process command line: [C:\hws-otel-collector\lib\..\otel\otelcol-contrib, --config, "C:\hws-otel-collector\lib\..\otel\otel-config.yaml", --feature-gates=pkg.translator.prometheus.NormalizeName]
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

The **logs/otelcol-\${timestamp}.log** file will also include details about the processing steps and the connection to the output framework (Prometheus, BMC Helix, etc.). Any connection issue of authentication failures with the outside will be displayed in this log file.

### Getting more details about the exported data

By default, details about the collected metrics is not displayed in **logs/otelcol-\${timestamp}.log**. To see which metrics, which labels and values are sent by the _Collector_, you need to enable the `logging` exporter in **otel/otel-config.yaml**.

Make sure the `logging` exporter is listed under `exporters` with log level set to `debug`:

```yaml
exporters:
# [...]
  logging:
    loglevel: debug
```

Then add `logging` to the metrics exporters in the pipeline:

```yaml
service:
  pipelines:
    metrics:
      receivers: # receivers
      processors: # processors
      exporters: [prometheusremotewrite/your-server,logging] # <-- added logging
```

Restart the _Collector_ for the configuration to be taken into account.

The `logging` exporter acts as a regular exporter (like the Prometheus exporter, for example) and outputs all metrics going through the pipeline. Details about the metric name, its labels and value is displayed in **logs/otelcol-\${timestamp}.log**. This allows you to verify that the accuracy of the collected metrics before being sent to the receiving framework, and that the configured processors did not alter the data.

Do not leave the `logging` exporter enabled for too long as its output is verbose and may affect the overall performance of _Collector_ while filling up your file system.

If you are monitoring a large number of systems with one _Collector_, you may get overwhelmed by the quantity of data in the logs. Do not hesitate to configure the `filter` processor to keep only certain metrics in the output as in the example below:

```yaml
processors:
  filter/keep1HostOnly:
    metrics:
      include:
        match_type: expr
        expressions:
        - Label("host.name") == "my-server.big-corp.com"
```

Declare the `filter/keep1HostOnly` processor in the pipeline and restart the _Collector_:

```yaml
service:
  pipelines:
    metrics:
      receivers: [otlp, prometheus/internal]
      processors: [memory_limiter,batch,filter/keep1HostOnly] # <-- added filter
      exporters: # exporters
```

Remove the `filter` processor from your pipeline once the troubleshooting is completed.
