keywords: clone, duplicate, copy, field
description: The "duplicateColumn" compute operation is used to duplicate a specified column in a table source in MetricsHub.

# `duplicateColumn`

The `duplicateColumn` compute allows to duplicate a column. The added column will be set next to the duplicated column and move all

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
          - type: duplicateColumn
            column: # <number>
```
