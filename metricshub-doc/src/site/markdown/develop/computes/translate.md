keywords: translate, map, table, status
description: This page describes how to define translation tables in a connector file.

# Translate

The `Translate` compute allows to translate values from specified column following a specified translation table.

```yaml
connector:
  # ...
pre: # <object>
  <sourceKey>: # <source-object>

monitors:
  <monitorType>: # <object>
    <job>: # <object>
      sources: # <object>
        <sourceKey>: # <source-object>
          computes: # <compute-object-array>
          - type: translate
            column: # <number>
            translationTable:  # <string>
```

## Translation Tables

This page describes how to define translation tables in a connector file.

### Format

```yaml
connector:
  # ...

monitors:
  # ...

translations: # <object>
  <table-key>: # <object>
    <key>: # <string>
```

### Example

Note here that you must define target states that match the states defined in the [Metrics Section](metrics.md), that’s why `WARN` and `ALARM` are becoming `degraded` and `failed`.

```yaml
translations:
  physicalDiskStatuses:
    3: ok
    4: degraded
    5: failed
    6: failed
    7: ok
    8: ok
    9: ok
    10: ok
    default: UNKNOWN
```
