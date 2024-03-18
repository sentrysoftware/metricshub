keywords: math, multiply, times, divide, ratio
description: Use the "multiply" compute operation in MetricsHub to multiply a value.

# `multiply`

The `multiply` compute allows to perform an multiplication operation to the values of a column. The column values must be numerics (integer, float, double, ...).
The `value` can be a number, or a reference to another column of the same table, using the '$' character followed by the column number.

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
          - type: multiply
            column: # <number>
            value: # <string>
```
