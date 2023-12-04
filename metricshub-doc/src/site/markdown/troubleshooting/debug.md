keywords: debug
description: How to enable the debug mode of the MetricsHub Agent (core engine) and the OpenTelemetry Collector.

# Debugging

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

Depending on the issue you are experiencing, you will have to either enable the debug mode for the **MetricsHub Agent** or for the **OpenTelemetry Collector**.

The **MetricsHub Agent** (core engine) is a Java process which launches the _OpenTelemetry Collector_ and performs the monitoring by connecting to each of the configured resources and retrieving information about their components. Enable its debug mode if there's no data available in your observability platform to obtain these logs:

* `metricshub-agent-global-error-$timestamp.log`: fatal errors such as an application crash upon start-up
* `metricshub-agent-global-$timestamp.log`: agent global information (agent status, user, version, scheduler, yaml parser, etc.)
* `metricshub-agent-$resourceId-$timestamp.log`: resource logs.

The **OpenTelemetry Collector** is in charge of pulling metrics, traces, and logs periodically and pushing them to the observability platform. Enable its debug mode if data is missing (the monitoring coverage is incomplete) to obtain the `otelcol-$timestamp.log` log.

## MetricsHub Agent

The **MetricsHub Agent** automatically sets its internal logging system to `error` to capture and record all errors that may arise while it runs. This ensures that important errors are readily available in the log files (`metricshub-agent-*-$timestamp.log` files), making it easier to diagnose and address any issues. If you wish to obtain more comprehensive details, you need to edit the **config/metricshub.yaml** file, add the `loggerLevel` property, and set `loggerlevel` to either `all`, `trace`, `debug`, `info`, `warn`, `error`, or `fatal`. Each level corresponds to a different degree of information. For example, `all`, `trace` and `debug` provide the most comprehensive details, while `error` and `fatal` focus on identifying critical issues.

The configuration can be updated as follows:

```yaml
loggerLevel: debug

resourceGroups:
  resources:
    # [...]
```

The debug output files are saved by default in the **logs** directory located under the **MetricsHub** directory:

* On Windows, the output files are stored in the **%LOCALAPPDATA%\MetricsHub** folder of the account running the application:
  * When the Local System account starts the MetricsHub Agent service, the output files are stored under **C:\Windows\System32\config\systemprofile\AppData\Local\MetricsHub\logs**.
  * If a specific user starts the MetricsHub Agent service, the output files are stored under **C:\Users\\<username\>\AppData\Local\MetricsHub\logs**.

* On Linux, the output files are stored in the installation directory: **/opt/metricshub/logs**.

To specify a different output directory, edit the **metricshub.yaml** file and add the `outputDirectory` parameter before the `resourceGroups` section:

```yaml
loggerLevel: debug
outputDirectory: C:\Users\<username>\AppData\Local\Temp\logs2021

resourceGroups:
  resources:
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

First check that the **MetricsHub Agent** successfully launched the _OpenTelemetry Collector_:

```log
[2023-12-01T14:43:09,548][INFO ][c.s.m.a.p.r.AbstractProcess] Started process with command line: [C:\Program Files\MetricsHub\app\..\otel\otelcol-contrib, --config, "C:\ProgramData\metricshub\otel\otel-config.yaml", --feature-gates=pkg.translator.prometheus.NormalizeName]
[2023-12-01T14:43:11,335][DEBUG][c.s.m.a.s.OtelCollectorProcessService] 2023-12-01T14:43:11.334+0100	info	service/telemetry.go:111	Setting up own telemetry...
[2023-12-01T14:43:11,335][DEBUG][c.s.m.a.s.OtelCollectorProcessService] 2023-12-01T14:43:11.334+0100	info	service/telemetry.go:141	Serving Prometheus metrics	{"address": "localhost:8888", "level": "Basic"}
[2023-12-01T14:43:11,336][DEBUG][c.s.m.a.s.OtelCollectorProcessService] 2023-12-01T14:43:11.335+0100	debug	components/components.go:28	Stable component.	{"kind": "processor", "name": "batch", "pipeline": "traces", "stability": "Stable"}
[2023-12-01T14:43:11,338][DEBUG][c.s.m.a.s.OtelCollectorProcessService] 2023-12-01T14:43:11.335+0100	debug	components/components.go:28	Stable component.	{"kind": "receiver", "name": "otlp", "pipeline": "traces", "stability": "Stable"}
[2023-12-01T14:43:11,338][DEBUG][c.s.m.a.s.OtelCollectorProcessService] 2023-12-01T14:43:11.335+0100	debug	components/components.go:28	Stable component.	{"kind": "receiver", "name": "otlp", "pipeline": "metrics", "stability": "Stable"}
[2023-12-01T14:43:11,338][DEBUG][c.s.m.a.s.OtelCollectorProcessService] 2023-12-01T14:43:11.335+0100	info	service/service.go:88	Starting otelcol-contrib...	{"Version": "0.67.0", "NumCPU": 12}
[2023-12-01T14:43:11,338][DEBUG][c.s.m.a.s.OtelCollectorProcessService] 2023-12-01T14:43:11.335+0100	info	extensions/extensions.go:42	Starting extensions...
[2023-12-01T14:43:11,339][DEBUG][c.s.m.a.s.OtelCollectorProcessService] 2023-12-01T14:43:11.335+0100	info	extensions/extensions.go:45	Extension is starting...	{"kind": "extension", "name": "health_check"}
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
