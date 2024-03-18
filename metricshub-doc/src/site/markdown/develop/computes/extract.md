keywords: extract, csv, substr, string, field
description: Use the "extract" compute operation in MetricsHub to extract a field from a string value.

# `extract`

The `extract` compute allows to extract a part of a column and replace this column by this part.
The `subSeparators` value is used to split the original column into an array and the `subColumn` value is used to chose a single column from this array.

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
          - type: extract
            column: # <number>
            subColumn: # <number>
            subSeparators: # <string>
```

## Example

In this example, let's suppose the column 1 is the speed of a CPU in GHz where the value is separated from the unit by a space character:

| `2.5 GHz` |
| `1.35 GHz` |
| `3.4755 GHz` |

This extract will split this column on every space character and keep only the first value.

```yaml
          - type: extract
            column: 1
            subColumn: 1
            subSeparator: ' '
```

Then the value of the column 3 after the extract will be:

| `2.5` |
| `1.35` |
| `3.4755` |
