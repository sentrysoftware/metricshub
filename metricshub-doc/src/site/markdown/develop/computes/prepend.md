keywords: string, prepend, insert, concat
description: Use the "prepend" operation to prepend (insert at the beginning) a specified string into a source in MetricsHub.

# `prepend`

The `Prepend` compute allow to concatenate a value at the beginning of all the lines in a selected column.
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
          - type: prepend
            column: # <number>
            value: # <string>
```
