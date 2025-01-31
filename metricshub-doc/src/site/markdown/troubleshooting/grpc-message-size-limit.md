keywords: grpc, message size, export, troubleshooting
description: How to resolve gRPC message size limit issues in MetricsHub when exporting large metric sets

# gRPC message size limit issues

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

When **exporting a large number of metrics**, **MetricsHub** may encounter gRPC message size limit errors due to the default `4MB` gRPC payload size limit.

## Common errors

**MetricsHub log**

```
Failed to export metrics. The request could not be executed. Full error message: stream was reset: NO_ERROR
```

**OpenTelemetry Collector log**

```
grpc-message: grpc: received message larger than max (6117815 vs. 4194304)
```

## Possible causes

- **Large metric payloads exceeding the default 4MB limit**
- **High-cardinality data causing oversized requests**

## Solutions

### 1. Enable gzip compression

Add the following setting in your `metricshub.yaml` file to **reduce message size** using gzip.

```yaml
otel:
  otel.exporter.otlp.metrics.compression: gzip
```

### 2. Increase the maximum gRPC message size

In the `otel/otel-config.yaml` file, increase `max_recv_msg_size_mib` to `16`.

```yaml
receivers:
  otlp:
    protocols:
      grpc:
        max_recv_msg_size_mib: 16 # Increase to 16MB (adjust if needed)
```

### 3. Reduce the number of exported metrics

To **avoid exceeding gRPC limits**, consider excluding non-essential metrics.

For example, if **SAN storage volumes** are generating too many metrics, you can exclude them using the [`monitorFilters`](../configuration/configure-monitoring.md#example-6-excluding-monitors-for-a-specific-resource) setting in `metricshub.yaml`.

```yaml
resourceGroups:
  <resource-group-name>:
    resources:
    <resource-id>:
        monitorFilters: ["!volume"] # Exclude volume metrics if not needed
```

## Logs to check for troubleshooting

If issues persist, refer to:

- [OpenTelemetry Collector Logs](./otel-logs.md)
- [MetricsHub Logs](./metricshub-logs.md)