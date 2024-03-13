keywords: develop, connector, mapping
description: This page describes how to map metrics and attribute in a connector file

# Mapping

This page describes how to map metrics and attributes in a connector file.

## Format

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

## Metric Categories

Metrics are separated in different categories:

* Attributes: They are the intrinsic values of your monitor, like its name, identifier number, serial number...
* Metrics: They are the performance data of your monitor at the time of collect.
* ConditionalCollection: The monitor will be collected only if all its conditional collections have value.

## Mapping Functions

* fakeCounter: Execute a fake counter operation based on the value which is expressed as a rate
* rate: Calculate a rate from counter values
