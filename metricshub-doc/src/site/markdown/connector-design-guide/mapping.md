keywords: guide, connector, mapping
description: This page describes how to map metrics and attribute in a connector file

# Mapping

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
        conditionalCollection:
          <key>: # <string>
        metrics:
          <key>: # <string>
```

## Mapping Functions
* buildId
* lookupParentId
* buildName
* FakeCounter
* Rate