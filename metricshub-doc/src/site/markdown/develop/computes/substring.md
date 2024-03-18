keywords: string, substr, extract
description: "substring" is a compute operation in MetricsHub that extracts a string from the specified input.

# Substring

The `Substring` compute allows to extract a String from a column.

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
          - type: substring
            column: # <number>
            start: # <string>
            length: # <string>
```
