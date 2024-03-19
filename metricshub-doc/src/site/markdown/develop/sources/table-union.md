keywords: union, join, table, concat, concatenate
description: Several tables can be concatenated together with the "tableUnion" source type in MetricsHub.

# Table Union (Source)

```yaml
connector:
  # ...
pre: # <object>
  <sourceKey>: # <source-object>

monitors:
  <monitorType>: # <object>
    <job>: # <object>
      sources: # <object>
        <sourceKey>:
          type: tableUnion
          tables: # <string-array>
          forceSerialization: <boolean>
          computes: <compute-object-array>
```
