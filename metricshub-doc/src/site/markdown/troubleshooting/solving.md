keywords: Solve, setback, error, issue
description: How to solve minor setbacks.

# Solving Minor Setbacks

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

## Failed to export metrics (Windows/Linux)

The following error occurs if a local OTLP receiver is unavailable to collect MetricsHub logs:

```
Feb 27, 2024 1:24:26 PM io.opentelemetry.sdk.internal.ThrottlingLogger doLog WARNING: Failed to export metrics. 
Server responded with gRPC status code 2. Error message: Failed to connect to localhost/[0:0:0:0:0:0:0:1]:4317
```

To solve this problem, ensure that the `OTLP` receiver and more specifically the `otel.exporter.otlp.metrics.endpoint` and `otel.exporter.otlp.logs.endpoint` parameters are [correctly set](../configuration/configure-agent.html#configure-the-otlp-receiver) in the `metricshub.yaml` configuration file.