keywords: develop, connector, monitors
description: This page describes how to specify the monitor to discovery and collect in a connector file.

# Monitors

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
