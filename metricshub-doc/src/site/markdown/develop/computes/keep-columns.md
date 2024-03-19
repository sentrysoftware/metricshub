keywords: filter, columns, reduce
description: Use the "keepColumns" operation to remove unnecessary columns in a source table in MetricsHub.

# `keepColumns`

The `keepColumns` compute allows to keep only the selected columns from a table.

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
          - type: keepColumns
            columnNumbers: # <string> | comma separated values
```
