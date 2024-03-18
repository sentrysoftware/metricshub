keywords: translate, map, bit, status
description: Use "perBitTranslation" to map set bits in an integer value to a specific meaning in MetricsHub.

# `perBitTranslation`

The `PerBitTranslation` allows to translate a numeric value into a string based on its bits, each bit having a different meaning.
The `BitList` is the list of bits to be taken into account.

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
          - type: perBitTranslation
            column: # <number>
            bitList: # <string> | comma separated values
            translationTable: # <string>
```

## Example

Here is an example of a `PerBitTranslation` compute.

```yaml

          - type: perBitTranslation
            column: 2
            bitList: "0,2,3,6,7,8,9,10,11,12,13,14"
            translationTable: "${esc.d}{translation::StatusInformationTranslationTable}"
```

And here is the associated translation table:

```yaml
  PowerSupplyStatusInformationTranslationTable:
    "13,1": AC Out-of-range
    "12,1": AC Lost or Out-of-range
    "10,1": Predicted Failure
    "11,1": AC Lost
    "0,1": ""
    "2,1": Not Ready
    "3,1": Fan Failure
    "6,1": AC Switch On
    "7,1": AC Power On
    "8,1": ""
    "9,1": Failed
    "14,1": Configuration Error
```

In this example, the column of the current source is decomposed into its bits and is replaced by the first match in the translation table.<br />
