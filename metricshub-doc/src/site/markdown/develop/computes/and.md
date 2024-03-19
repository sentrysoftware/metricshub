keywords: bitwise, and, or, xor, logic, binary, math
description: The "and" compute operation is used to calculate the result of the bitwise "and" operator with the specified value on a source in MetricsHub.

# `and`

The `and` compute allows make a bitwise `and` on the values of a column.

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
          - type: and
            column: # <number>
            value:  # <string>
```

## Example

In this example, we will remove the high level bits of the column 1 of our source using a bitwise `and` with the value 1023.

```yaml
          - type: and
            column: 1
            value: 1023
```
