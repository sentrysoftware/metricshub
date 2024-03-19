keywords: math, plus, minus, add, subtract, substract
description: Use "add" in MetricsHub to perform a mathematical addition with a specified value on a source.

# `add`

The `add` compute allows to perform an addition operation to the values of a column. The column values must be numerics (integer, float, double, ...).<br />
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
          - type: add
            column: # <number>
            value:  # <string>
```
