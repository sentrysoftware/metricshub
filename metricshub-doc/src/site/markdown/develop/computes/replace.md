keywords: string, replace, trim
description: The "replace" operation replaces a string with another into a source in MetricsHub.

# Replace

The `Replace` compute allows to replace a specific value in a column by another value.

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
          - type: replace
            column: # <number>
            existingValue: # <string>
            newValue: # <string>
```
