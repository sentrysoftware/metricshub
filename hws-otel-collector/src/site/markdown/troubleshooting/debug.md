keywords: debug
description: There are several options to debug ${project.name} and troubleshoot its activity.

# Debugging

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

As there are 2 separate processes in **${project.name}**, there are 2 separate debugging mechanisms. Depending on the issue you're experiencing, you may need to troubleshoot one or the other of these main components:

1. **OpenTelemetry Collector**, which is in charge of launching the core engine (**Hardware Sentry Agent**), extracting its data and pushing them to the destination.
2. **Hardware Sentry Agent**, a Java process that performs the actual hardware monitoring, connecting to each of the monitored hosts and retrieving information about their hardware components.

If you're not getting any data at all at the destination framework, you are probably facing issues with the *Collector*. If some data is missing (the monitoring coverage is incomplete), it's more likely a problem with the core engine.

## OpenTelemetry Collector

The **${project.name}** writes details about its operations (from initialization, to pipeline, to termination) to the **logs/otel.log** file.

This **logs/otel.log** file is reset each time the *Collector* is started. Previous logs are backed up as `logs/otel-timestamp.log` where `timestamp` is the time at which the log was backed up, example: `otel-2022-03-11T18-50-20.292.log`.

The **${project.name}** rotates the **otel.log** file when it reaches a maximum size of **100MB** and retains **3** old log files (**otel-timestamp.log**) for **30 days**.

The level of details in **logs/otel.log** is configured in **config/otel-config.yaml**. Set the log level to `error`, `warn`, `info` (default), or `debug` to get more details as in the below example:

```yaml
service:
  telemetry:
    logs:
      level: debug
  extensions: [health_check]
  pipelines:
  # [...]
```

You need to restart the *Collector* for these new settings to be taken into account.

### What to look for in otel.log

The first critical task the *Collector* has to complete is to launch the Java process of the **Hardware Sentry Agent** through the `prometheusexecreceiver`. Check in **logs/otel.log** that the sub-process is properly started and did not encounter any issue:

```log
2021-12-22T11:26:13.870+0100	debug	hwsagentextension@v0.41.0/process.go:146	Starting hws_agent	{"kind": "extension", "name": "hws_agent", "command": "C:\\Windows\\system32\\cmd.exe"}
2021-12-22T11:26:13.870+0100	info	builder/exporters_builder.go:48	Exporter started.	{"kind": "exporter", "name": "prometheus"}
2021-12-22T11:26:13.870+0100	info	service/service.go:96	Starting processors...
2021-12-22T11:26:13.870+0100	info	builder/pipelines_builder.go:54	Pipeline is starting...	{"name": "pipeline", "name": "metrics"}
2021-12-22T11:26:13.870+0100	info	internal/resourcedetection.go:126	began detecting resource information	{"kind": "processor", "name": "resourcedetection"}
2021-12-22T11:26:14.722+0100	debug	hwsagentextension@v0.41.0/process.go:129	hws_agent changed state	{"kind": "extension", "name": "hws_agent", "state": "Running"}
2021-12-22T11:26:15.755+0100	info	internal/resourcedetection.go:139	detected resource information	{"kind": "processor", "name": "resourcedetection", "resource": {"host.name":"pc-nassim.internal.sentrysoftware.net","os.type":"WINDOWS"}}
2021-12-22T11:26:15.755+0100	info	builder/pipelines_builder.go:65	Pipeline is started.	{"name": "pipeline", "name": "metrics"}
2021-12-22T11:26:15.755+0100	info	service/service.go:101	Starting receivers...
2021-12-22T11:26:15.755+0100	info	builder/receivers_builder.go:68	Receiver is starting...	{"kind": "receiver", "name": "prometheus/internal"}
2021-12-22T11:26:15.755+0100	debug	discovery/manager.go:195	Starting provider	{"kind": "receiver", "name": "prometheus/internal", "provider": "static/0", "subs": "[otel-collector-internal]"}
2021-12-22T11:26:15.755+0100	debug	discovery/manager.go:213	Discoverer channel closed	{"kind": "receiver", "name": "prometheus/internal", "provider": "static/0"}
2021-12-22T11:26:16.284+0100	debug	hwsagentextension@v0.41.0/process.go:229	[36m[2021-12-22T11:26:16,281][INFO ][c.s.h.a.HardwareSentryAgentApp] Starting HardwareSentryAgentApp using Java 11.0.6 on pc-nassim with PID 236 (C:\Program Files\hws-otel-collector\lib\hws-agent-1.1-SNAPSHOT.jar started by nassim in c:\Program Files\hws-otel-collector)	{"kind": "extension", "name": "hws_agent"}
2021-12-22T11:26:16.287+0100	debug	hwsagentextension@v0.41.0/process.go:229	[m[32m[2021-12-22T11:26:16,287][DEBUG][c.s.h.a.HardwareSentryAgentApp] Running with Spring Boot v2.4.5, Spring v1.1-SNAPSHOT	{"kind": "extension", "name": "hws_agent"}
2021-12-22T11:26:16.288+0100	debug	hwsagentextension@v0.41.0/process.go:229	[m[36m[2021-12-22T11:26:16,288][INFO ][c.s.h.a.HardwareSentryAgentApp] No active profile set, falling back to default profiles: default	{"kind": "extension", "name": "hws_agent"}
2021-12-22T11:26:16.608+0100	info	builder/receivers_builder.go:73	Receiver started.	{"kind": "receiver", "name": "prometheus/internal"}
2021-12-22T11:26:16.608+0100	info	builder/receivers_builder.go:68	Receiver is starting...	{"kind": "receiver", "name": "otlp"}
2021-12-22T11:26:16.608+0100	info	otlpreceiver/otlp.go:69	Starting GRPC server on endpoint localhost:4317	{"kind": "receiver", "name": "otlp"}
2021-12-22T11:26:16.608+0100	info	builder/receivers_builder.go:73	Receiver started.	{"kind": "receiver", "name": "otlp"}
2021-12-22T11:26:16.608+0100	info	healthcheck/handler.go:129	Health Check state change	{"kind": "extension", "name": "health_check", "status": "ready"}
2021-12-22T11:26:16.608+0100	info	service/telemetry.go:92	Setting up own telemetry...
2021-12-22T11:26:16.609+0100	info	service/telemetry.go:116	Serving Prometheus metrics	{"address": ":8888", "level": "basic", "service.instance.id": "05d27cc8-30f6-47cc-8c2f-0c6c1181fa96", "service.version": "latest"}
2021-12-22T11:26:16.609+0100	info	service/collector.go:239	Starting hws-otel-collector...	{"Version": "1.1-SNAPSHOT (Build 6b2c8efc on Dec 22, 2021 at 10:30:28 AM)", "NumCPU": 12}
2021-12-22T11:26:16.609+0100	info	service/collector.go:135	Everything is ready. Begin running and processing data.
2021-12-22T11:26:18.060+0100	debug	hwsagentextension@v0.41.0/process.go:229	[m[36m[2021-12-22T11:26:18,060][INFO ][c.s.h.a.s.TaskSchedulingService] Scheduled Job for host id ecs1-01	{"kind": "extension", "name": "hws_agent"}
2021-12-22T11:26:18.061+0100	debug	hwsagentextension@v0.41.0/process.go:229	[m[36m[2021-12-22T11:26:18,060][INFO ][c.s.h.a.s.t.StrategyTask] Calling the engine to discover host: ecs1-01.	{"kind": "extension", "name": "hws_agent"}
2021-12-22T11:26:18.061+0100	debug	hwsagentextension@v0.41.0/process.go:229	[m[36m[2021-12-22T11:26:18,061][INFO ][c.s.h.a.s.TaskSchedulingService] Scheduled Job for host id ankara	{"kind": "extension", "name": "hws_agent"}
2021-12-22T11:26:18.061+0100	debug	hwsagentextension@v0.41.0/process.go:229	[m[36m[2021-12-22T11:26:18,061][INFO ][c.s.h.a.s.t.StrategyTask] Calling the engine to discover host: ankara.	{"kind": "extension", "name": "hws_agent"}
2021-12-22T11:26:18.377+0100	debug	hwsagentextension@v0.41.0/process.go:229	[m[36m[2021-12-22T11:26:18,377][INFO ][c.s.h.a.HardwareSentryAgentApp] Started HardwareSentryAgentApp in 2.485 seconds (JVM running for 3.634)	{"kind": "extension", "name": "hws_agent"}
```

The **logs/otel.log** file will also include details about the processing steps and the connection to the output framework (Prometheus, BMC Helix, etc.). Any connection issue of authentication failures with the outside will be displayed in this log file.

### Getting more details about the exported data

By default, details about the collected metrics is not displayed in **logs/otel.log**. To see which metrics, which labels and values are sent by the *Collector*, you need to enable the `logging` exporter in **config/otel-config.yaml**.

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

Restart the *Collector* for the configuration to be taken into account.

The `logging` exporter acts as a regular exporter (like the Prometheus exporter, for example) and outputs all metrics going through the pipeline. Details about the metric name, its labels and value is displayed in **logs/otel.log**. This allows you to verify that the accuracy of the collected metrics before being sent to the receiving framework, and that the configured processors did not alter the data.

Do not leave the `logging` exporter enabled for too long as its output is verbose and may affect the overall performance of *Collector* while filling up your file system.

If you are monitoring a large number of systems with one *Collector*, you may get overwhelmed by the quantity of data in the logs. Do not hesitate to configure the `filter` processor to keep only certain metrics in the output as in the example below:

```yaml
processors:
  filter/keep1HostOnly:
    metrics:
      include:
        match_type: expr
        expressions:
        - Label("host.name") == "my-server.big-corp.com"
```

Declare the `filter/keep1HostOnly` processor in the pipeline and restart the *Collector*:

```yaml
service:
  pipelines:
    metrics:
      receivers: [prometheus_exec/hws-exporter,prometheus/internal]
      processors: [memory_limiter,batch,filter/keep1HostOnly] # <-- added filter
      exporters: # exporters
```

Remove the `filter` processor from your pipeline once the troubleshooting is completed.

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
