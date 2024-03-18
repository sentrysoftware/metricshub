keywords: source
description: In a MetricsHub connector, the sources describe how to query the monitored system to retrieve the required data and metrics.

# Sources

<div class="alert alert-warning"><span class="fa-solid fa-person-digging"></span> Documentation under construction...</div>

In a MetricsHub connector, the sources describe how to query the monitored system to retrieve the required data and metrics.

## Format

Sources can be specified either under the `pre` section or within the monitoring *\<job\>* section (e.g., `discovery`, `collect`, or `simple`):

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
```

Under each source we can define a set of computes. Refer to the [Computes Section](develop/computes.md) page for more details.
