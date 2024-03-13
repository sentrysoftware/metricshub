keywords: develop, connector, translation, table, map
description: This page describes how to define translation tables in a connector file.

# Translation Tables

This page describes how to define translation tables in a connector file.

## Format

```yaml
connector:
  # ...

monitors:
  # ...

translations: # <object>
  <table-key>: # <object>
    <key>: # <string>
```

## Example

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
