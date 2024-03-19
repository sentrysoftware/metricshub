keywords: develop, connector, compute
description: This page describes all the compute formats support by the connector

# Computes

<div class="alert alert-warning"><span class="fa-solid fa-person-digging"></span> Documentation under construction...</div>

This page describes all the compute formats support by the connector.

## Format

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
```

Note: All column numbers start at 1, not 0.
