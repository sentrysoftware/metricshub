keywords: math, minus, subtract, substract, add
description: Use "subtract" in MetricsHub to perform a subtraction operation on a source.

# `subtract`

The `subtract` compute allows to perform an subtraction operation to the values of a column. The column values must be numerics (integer, float, double, ...).<br />
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
          - type: subtract
            column: # <number>
            value: # <string>
```
