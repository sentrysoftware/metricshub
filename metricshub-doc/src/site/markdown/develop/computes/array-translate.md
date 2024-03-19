keywords: translate, map, table, status, array, list
description: Use the "arrayTranslate" compute operation to map an array of values following a specified translation table.

# `arrayTranslate`

The `arrayTranslate` compute allows make to translate a column containing an array of values using a translation table.

The `arraySeparator` value can be used to define how are separated the values in the array to translate. Default value: "|".

The `resultSeparator` value can be used to define how to separate the values after the translation. Default value: "|".

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
          - type: arrayTranslate
            column: # <number>
            translationTable:  # <string>
            arraySeparator: # <string>
            resultSeparator: # <string>
```

## Example

In this example, we will use the `arrayTranslate` compute to tranlate an operational status, which in this case is a number, to a more understandable status using the OperationStatusTranslationTable translation table at the end of the connector file.

```yaml
          - type: arrayTranslate
            column: 2
            translationTable: "${esc.d}{translation::OperationStatusTranslationTable}"
```

Here is the translation table we will use in our case.

```yaml
  OperationStatusTranslationTable:
    "11": degraded
    "12": failed
    "13": failed
    "14": failed
    "15": ok
    "16": degraded
    "17": ok
    "18": ok
    Default: UNKNOWN
    "1": ok
    "2": ok
    "3": degraded
    "4": degraded
    "5": degraded
    "6": failed
    "7": failed
    "8": degraded
    "9": degraded
    "10": failed
```
