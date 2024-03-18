keywords: filter, keep, exclude, matching, regex, grep
description: The "excludeMatchingLines" compute operation filters a source in MetricsHub with the specified criteria.

# `excludeMatchingLines`

The `excludeMatchingLines` compute allows to remove the lines of your table where the selected column correspond to the `regExp` values or is equal to one of the values in the `valueList`.

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
          - type: excludeMatchingLines
            column: # <number>
            regExp: # <string>
            valueList: # <string> | comma separated values
```
