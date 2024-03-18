keywords: convert, hex, merge, array, status
description: The "convert" operation performs the specified conversion on a source in MetricsHub.

# `convert`

The `convert` compute allows make to convert a column from a hexadecimal value to a decimal value or an array of status into a simple status.

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
          - type: convert
            column: # <number>
            conversion: # <enum> | possible values: [ hex2Dec, array2SimpleStatus ]
```
