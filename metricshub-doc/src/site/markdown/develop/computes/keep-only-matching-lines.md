keywords: filter, keep, exclude, matching, regex, grep
description: The "keepOnlyMatchingLines" compute operation filters a source in MetricsHub with the specified criteria.

## `keepOnlyMatchingLines`

The `KeepOnlyMatchingLines` compute allows to keep only the lines of your table where the selected column correspond to the `regExp` values or is equal to one of the values in the `valueList`.

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
          - type: keepOnlyMatchingLines
            column: # <number>
            regExp: # <string>
            valueList: # <string> | comma separated values
```
