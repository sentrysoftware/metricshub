keywords: agent, configuration, self-monitoring, trace, span, pipeline
description: How to configure the self-monitoring of ${solutionName}.

# Self-Monitoring

<!-- MACRO{toc|fromDepth=1|toDepth=3|id=toc} -->

> **Important**: Self-monitoring is intended to help advanced users debug or troubleshoot **${solutionName}**. We recommend using it cautiously, as an incorrect configuration can lead to monitoring issues.

**${solutionName}** can monitor itself, granting you a way to access additional metrics, such as the number of requests for a specific protocol, or the current state of the JVM.
Using the **OpenTelemetry Java Agent**, **${solutionName}** can export traces and metrics of the **Hardware Sentry Agent** to observe its own performance.

Those traces provide you and **Sentry Desk** with a complementary way to troubleshoot any problem arising.
They describe the internal "path" that the **${solutionName}** application takes to execute its internal tasks and offer a clear overview of what is happening when a problem appears.

**Self-Monitoring** generates a variety of **traces** and tracks every protocol request such as:

* HTTP
* SNMP
* SSH
* Local Command Request
* IPMI
* WMI
* WBEM
* WinRM

The **metrics** range from the number of requests sent for each protocol to the latency measured for each request.

All the JVM metrics have the same attributes:
`host.arch`, `host.name`, `os.description`, `os.type`, `pool`, `process.command_line`, `process.executable.path`, `process.pid`, `process.runtime.description`, `process.runtime.name`, `process.runtime.name`, `process.runtime.version`, `service.name`, `service`, `telemetry.auto.version`, `telemetry.sdk.language`, `telemetry.sdk.name`, `telemetry.sdkversion`, `type`, `host`.

| JVM Metrics                                     | Description                                                   | Type    | Unit |
|-------------------------------------------------|---------------------------------------------------------------|---------|------|
| otel.process.runtime.jvm.cpu.utilization        | Recent CPU utilization for the JVM process                    | Gauge   |      |
| otel.process.runtime.jvm.system.cpu.utilization | Recent CPU utilization for the whole system                   | Gauge   |      |
| otel.process.runtime.jvm.classes.current_loaded | Number of classes currently loaded by the JVM process         | Gauge   |      |
| otel.process.runtime.jvm.classes.unloaded       | Number of classes unloaded by the JVM process since its start | Counter |      |
| otel.process.runtime.jvm.classes.loaded         | Number loaded classes by the JVM process since its start      | Counter |      |
| otel.process.runtime.jvm.threads.count          | JVM process' number of executing threads                      | Gauge   |      |
| otel.process.runtime.jvm.memory.committed       | JVM process' memory committed                                 | Gauge   | By   |
| otel.process.runtime.jvm.memory.init            | JVM process's initial memory requested                        | Gauge   | By   |
| otel.process.runtime.jvm.memory.limit           | JVM process' memory size limit                                | Gauge   | By   |
| otel.process.runtime.jvm.memory.usage           | JVM process' memory usage                                     | Gauge   | By   |


## Self-monitoring the agent

The **Self-Monitoring** feature is **disabled by default**. Follow the steps below to activate it.

### Enable OpenTelemetry traces

In order to export the traces to the observability back-end, comment out the traces pipeline inside the `otel/otel-config.yaml` file and configure the exporter which should send the traces.

```yaml
traces:  
  receivers: [otlp]  
  processors: [memory_limiter, batch, resourcedetection]  
  exporters: [logging, datadog/api] # List here the platforms on which you want to see the traces
```

### Configure Java Options

Now that the traces pipeline is configured, you need to add Java options to properly link the OpenTelemetry Java Agent to the **Hardware Sentry Agent** and configure the service and the exporter.

>**Warning**: you must change the `service.name` property if you are using multiple **Hardware Sentry Agents**, so each service has a unique identifier. Otherwise, the observability back-end will aggregate the services together, resulting in issues regarding latency and information loss.

#### On Windows

Add the following options to the `agent.cfg` file located in `C:\Program Files\hws\app`.

```java
java-options=-javaagent:otel\opentelemetry-javaagent.jar
java-options=-Dotel.resource.attributes=service.name=Hardware-Sentry-Agent
java-options=-Dotel.traces.exporter=otlp
java-options=-Dotel.metrics.exporter=otlp
java-options=-Dotel.exporter.otlp.endpoint=https://localhost:4317
java-options=-Dotel.exporter.otlp.certificate=security\otel.crt
java-options=-Dotel.exporter.otlp.headers=Authorization=Basic aHdzOlNlbnRyeVNvZnR3YXJlMSE=
```

#### On Linux

Add the following options to the `agent.cfg` file located in `/opt/hws/lib/app`.

```java
java-options=-javaagent:/opt/hws/otel/opentelemetry-javaagent.jar
java-options=-Dotel.resource.attributes=service.name=Hardware-Sentry-Agent
java-options=-Dotel.traces.exporter=otlp
java-options=-Dotel.metrics.exporter=otlp
java-options=-Dotel.exporter.otlp.endpoint=https://localhost:4317
java-options=-Dotel.exporter.otlp.certificate=/opt/hws/security/otel.crt
java-options=-Dotel.exporter.otlp.headers=Authorization=Basic aHdzOlNlbnRyeVNvZnR3YXJlMSE=
```

## Trace example

The traces are made of spans that describe the current state of **${solutionName}**. Every time a request is sent, a span is created. This is what a span looks like:

```log
 ScopeSpans #0
 ScopeSpans SchemaURL: 
 InstrumentationScope io.opentelemetry.okhttp-3.0 1.18.0-alpha
 Span #0
     Trace ID       : 0990c796d9cd3944aae417e191e8a65b
     Parent ID      : 5051d56bbe729bbe
     ID             : 9883e54ca96403d8
     Name           : HTTP POST
     Kind           : SPAN_KIND_CLIENT
     Start time     : 2022-11-03 08:39:34.2384308 +0000 UTC
     End time       : 2022-11-03 08:39:34.7447889 +0000 UTC
     Status code    : STATUS_CODE_UNSET
     Status message : 
 Attributes:
      -> thread.id: INT(33)
      -> net.transport: STRING(ip_tcp)
      -> net.peer.name: STRING(localhost)
      -> http.flavor: STRING(2.0)
      -> http.url: STRING(https://localhost:4317/opentelemetry.proto.collector.metrics.v1.MetricsService/Export)
      -> net.peer.port: INT(4317)
      -> http.method: STRING(POST)
      -> http.status_code: INT(200)
      -> thread.name: STRING(OkHttp https://localhost:4317/...)
```

A span is made of 3 main parts:

1. `ScopeSpans #n1`, n1 being the number associated to this group of spans.

2. `Span #n2`, n2 being the current span number inside this scope of spans.
   There are common attributes to every span:
    * `Trace ID`: Unique ID of the trace.
    * `Parent ID`: To keep track of the hierarchy between the spans.
    * `ID`: Unique ID of the span.
    * `Name`: Name of the span, usually describes the step that is tracked by the span.
    * `Kind`: Type of the span, either `SPAN_KIND_INTERNAL` or `SPAN_KIND_CLIENT` (for HTTP requests).
    * `Start time`: Timestamp of the start of the lifecycle of the span.
    * `End time`: Timestamp of the end of the lifecycle of the span.
    * `Status code`: Status code of the span, either `STATUS_CODE_UNSET` or `STATUS_CODE_ERROR`.
    * `Status message`: Message associated to the `Status code`.

3. `Attributes`, that represent the various parameters that are used where the span is created in the code. These attributes are specific to each span. For example, a span that tracks an HTTP request will not have the same attributes than a span that tracks an SNMP request.