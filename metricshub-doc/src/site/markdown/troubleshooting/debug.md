keywords: debug
description: How to enable the debug mode of the MetricsHub Agent (core engine) and the OpenTelemetry Collector.

# Debugging

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

Depending on the issue you are experiencing, you will have to either enable the debug mode for the **Hardware Sentry Agent** or for the **OpenTelemetry Collector**.

The **Hardware Sentry Agent** (core engine) is a Java process which launches the _OpenTelemetry Collector_ and performs the hardware monitoring by connecting to each of the configured hosts and retrieving information about their hardware components. Enable its debug mode if there's no data available in your observability platform to obtain these logs:

* `metricshub-agent-global-error-$timestamp.log`: fatal errors such as an application crash upon start-up
* `metricshub-agent-global-$timestamp.log`: agent global information (agent status, user, version, scheduler, yaml parser, etc.)
* `metricshub-agent-$hostId-$timestamp.log`: host logs.

The **OpenTelemetry Collector** is in charge of pulling metrics, traces, and logs periodically and pushing them to the observability platform. Enable its debug mode if data is missing (the monitoring coverage is incomplete) to obtain the `otelcol-$timestamp.log` log.

## MetricsHub Agent

The **MetricsHub Agent** automatically sets its internal logging system to `error` to capture and record all errors that may arise while it runs. This ensures that important errors are readily available in the log files (`metricshub-agent-*-$timestamp.log` files), making it easier to diagnose and address any issues. If you wish to obtain more comprehensive details, you need to edit the **config/metricshub-config.yaml** file, add the `loggerLevel` property, and set `loggerlevel` to either `all`, `trace`, `debug`, `info`, `warn`, `error`, or `fatal`. Each level corresponds to a different degree of information. For example, `all`, `trace` and `debug` provide the most comprehensive details, while `error` and `fatal` focus on identifying critical issues.

The configuration can be updated as follows:

```yaml
loggerLevel: debug

hosts:
- host:
    # [...]
```

The debug output files are saved by default in the **logs** directory located under the **MetricsHub** directory:

* On Windows, the output files are stored in the **%LOCALAPPDATA%\metricshub** folder of the account running the application:
  * When the Local System account starts the Hardware Sentry Agent service, the output files are stored under **C:\Windows\System32\config\systemprofile\AppData\Local\metricshub\logs**.
  * If a specific user starts the Hardware Sentry Agent service, the output files are stored under **C:\Users\\<username\>\AppData\Local\metricshub\logs**.

* On Linux, the output files are stored in the installation directory: **/opt/metricshub/logs**.

To specify a different output directory, edit the **metricshub.yaml** file and add the `outputDirectory` parameter before the `hosts` section:

```yaml
loggerLevel: debug
outputDirectory: C:\Users\<username>\AppData\Local\Temp\logs2021

hosts:
- host:
    # [...]
```

Set `loggerlevel` to `off` to disable the debug mode.

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

Then, restart **${solutionName}** for these new settings to be considered.

Finally, check the **logs/otelcol-\<timestamp\>.log** file, where `<timestamp>` is the time at which the log was started.

> Note: The **logs/otelcol-\<timestamp\>.log** file is reset each time the *Collector* is started. Previous logs are identified with the `<timestamp>` value (ex: `otelcol-2022-09-19-02-05-18.log`). **${solutionName}** rotates the **otelcol-\<timestamp\>.log** file when it reaches a maximum size of **100MB** and retains old log files for **2 days**.

### What to look for in otelcol-\<timestamp\>.log

First check that the **Hardware Sentry Agent** successfully launched the _OpenTelemetry Collector_:

```log
[2022-09-19T14:05:18,459][INFO ][c.s.h.a.p.r.Executable] Started process command line: [C:\Program Files\metricshub\app\..\otel\otelcol-contrib, --config, "C:\ProgramData\metricshub\otel\otel-config.yaml", --feature-gates=pkg.translator.prometheus.NormalizeName]
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

Restart the _Collector_ for the new settings to be considered.

The metric name, its labels and value are listed in the **logs/otelcol-\<timestamp\>.log** file.

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
