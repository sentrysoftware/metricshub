keywords: append, string, concat, right
description: Use the "append" compute operation to append a specified string to a source.

# `append` (Append)

The `Append` compute allow to concatenate a value at the end of all the lines in a selected column.
The `value` can be a number, or a reference to another column of the same table, using the '$' character followed by the column number.
Since the data is converted from a CSV to a Table, you can add columns by adding ';' characters using the append.

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
          - type: append
            column: # <number>
            value: # <string>
```
