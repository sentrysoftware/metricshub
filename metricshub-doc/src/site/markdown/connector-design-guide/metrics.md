keywords: guide, connector, metrics
description: This page shows how to defines in a connector the OpenTelemetry metrics that this connector will collect and report.

# Metrics

## Format

```yaml
connector:
  # ...

metrics:
  <metricName>: # <object>
    unit: # <string>
    description: # <string>
    type: # oneOf [ <enum>, <object> ] | possible values for <enum> [ Gauge, Counter, UpDownCounter ]
      stateSet: # <string-array>
      output: # <enum> | possible values [ Gauge, Counter, UpDownCounter ] | Optional | Default: UpDownCounter
``` 