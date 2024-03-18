keywords: math, multiply, divide, times, ratio
description: Use the "divide" compute operation in MetricsHub to divide a value.

# `divide`

The `divide` compute allows to perform an division operation to the values of a column. The column values must be numerics (integer, float, double, ...).
The `value` can be a number other than '0', or a reference to another column of the same table, using the '$' character followed by the column number.

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
          - type: divide
            column: # <number>
            value: # <String>
```
