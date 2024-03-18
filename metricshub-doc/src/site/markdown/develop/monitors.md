keywords: develop, connector, monitors
description: This page describes how to specify the monitor to discovery and collect in a connector file.

# Monitors

<div class="alert alert-warning"><span class="fa-solid fa-person-digging"></span> Documentation under construction...</div>

This page describes how to specify the monitor to discovery and collect in a connector file.

## Format

```yaml
connector:
  # ...
monitors:
  <monitorName>: # <object>
    discovery: # <object>
      sources: # <object>
    collect: # <object>
      type: # <string> | possible values [ multiInstance, monoInstance ]
      keys: # <string-array> | Only for collect <job> with multiInstance type | Default: [ id ]
      executionOrder: # <string-array> | Optional
      sources: # <object>
    simple: # <object>
      executionOrder: # <string-array> | Optional
      sources: # <object>
```

Each source is defined in the [Sources](sources.md) page.

## Mapping

This page describes how to map metrics and attributes in a connector file.

### Format

```yaml
connector:
  # ...

monitors:
  <monitorType>: # <object>
    <job>: # <object>
      # ...
      mapping:
        source: $monitors.enclosure.discovery.sources.Source(7)
        attributes:
          <key>: # <string>
        metrics:
          <key>: # <string>
        conditionalCollection:
          <key>: # <string>
```

### Metric Categories

Metrics are separated in different categories:

* Attributes: They are the intrinsic values of your monitor, like its name, identifier number, serial number...
* Metrics: They are the performance data of your monitor at the time of collect.
* ConditionalCollection: The monitor will be collected only if all its conditional collections have value.

### Mapping Functions

* fakeCounter: Execute a fake counter operation based on the value which is expressed as a rate
* rate: Calculate a rate from counter values

## Metrics

This page shows how to defines in a connector the OpenTelemetry metrics that this connector will collect and report.

### Format

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
