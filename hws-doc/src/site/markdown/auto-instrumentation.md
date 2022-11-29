keywords: agent, configuration, auto-instrumentation, trace, span, pipeline
description: How to configure the automatic instrumentation for ${solutionName}.

# Auto-instrumentation

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

The **${solutionName}** is able to monitor itself, granting the user a way to access additional metrics, such as the number of requests for a specific protocol, or the energy consumed by the agent.

## Traces pipeline

The traces pipeline has to be configured in order to export the spans correctly to the user's platform of choice.
The syntax is the same as the **metrics** and **logs** pipelines.

```yaml
traces:  
  receivers: [otlp]  
  processors: [memory_limiter, batch, resourcedetection]  
  exporters: [logging, datadog/api] # List here the platforms on which you want to see the traces
```

## Enabling the auto-instrumentation of the agent

The automatic instrumentation is **disabled by default**.
To turn it on, follow the procedure corresponding to your OS.

### On Windows

>**Warning!** If you are using multiple ${solutionName} agents, please change the `service.name` property so each service has a unique identifier. Otherwise the visualization platform will aggregate the services together and this will result in issues regarding latency and information loss.

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

### On Debian / RHEL

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


> **Important**: Do not forget to uncomment the traces pipeline and to configure it properly according to the receivers, processors and exporters you want to use.

## Trace example

The traces are made of spans that describe the current state of the program. Every time a request is sent, a span will be created. This is what a span looks like:

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

We can clearly see that a span is made of 3 main parts:

1. `ScopeSpans #n1`, n1 being the number associated to this group of spans.

2. `Span #n2`, n2 being the current span number inside this scope of spans.
   There are common fields to every span:
    * `Trace ID` : Unique ID of the trace.
    * `Parent ID` : To keep track of the hierarchy between the spans.
    * `ID` : Unique ID of the span.
    * `Name` : Name of the span, usually describes the step that is tracked by the span.
    * `Kind` : Type of the span, either `SPAN_KIND_INTERNAL` or `SPAN_KIND_CLIENT` (for HTTP requests).
    * `Start time` : Timestamp of the start of the lifecycle of the span.
    * `End time` : Timestamp of the end of the lifecycle of the span.
    * `Status code` : Status code of the span, either `STATUS_CODE_UNSET` or `STATUS_CODE_ERROR`.
    * `Status message` : Message associated to the `Status code`.

3. `Attributes`, that represent the various parameters that are used where the span is created in the code.
   These attributes are specific to the created span. For example, a span that tracks an HTTP request will not have the same attributes as a span that tracks an SNMP request.