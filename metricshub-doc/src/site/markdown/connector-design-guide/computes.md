keywords: guide, connector, computes
description: This page describes all the compute formats support by the connector

# Computes

This page describes all the compute formats support by the connector.

* [Format](#format)
* [Add](#add)
* [And](#and)
    * [Example](#and-example)
* [ArrayTranslate](#arraytranslate)
    * [Example](#arraytranslate-example)
* [Awk](#awk)
    * [Example](#awk-example)
* [Convert](#convert)
* [Divide](#divide)
* [DuplicateColumn](#duplicatecolumn)
* [ExcludeMatchingLines](#excludematchinglines)
* [Extract](#extract)
    * [Example](#extract-example)
* [ExtractPropertyFromWbemPath](#extractpropertyfromwbempath)
* [Json2Csv](#json2csv)
* [KeepColumns](#keepcolumns)
* [KeepOnlyMatchingLines](#keeponlymatchinglines)
* [LeftConcat](#leftconcat)
* [Multiply](#multiply)
* [PerBitTranslation](#perbittranslation)
* [Replace](#replace)
* [RightConcat](#rightconcat)
* [Subtract](#subtract)
* [Substring](#substring)
* [Translate](#translate)
* [Xml2Csv](#xml2csv)

## Format

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
```
Note: All column numbers start at 1, not 0.

## Add

The `Add` compute allows to perform an addition operation to the values of a column. The column values must be numerics (integer, float, double, ...).<br />
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

## And

The `And` compute allows make a bitwise `and` on the values of a column.<br />
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

### <a id="and-example" />Example

In this example, we will remove the high level bits of the column 1 of our source using a bitwise `and` with the value 1023. 

```yaml
          - type: and
            column: 1
            value: 1023
```

## ArrayTranslate

The `ArrayTranslate` compute allows make to translate a column containing an array of values using a translation table. See the [Translation Table Section](translation-tables.md) for more details on how to implement them.<br />
The `arraySeparator` value can be used to define how are separated the values in the array to translate. Default value: "|".<br />
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

### <a id="arraytranslate-example" />Example

In this example, we will use the `ArrayTranslate` compute to tranlate an operational status, which in this case is a number, to a more understandable status using the OperationStatusTranslationTable translation table at the end of the connector file.

```yaml
          - type: arrayTranslate
            column: 2
            translationTable: "${translation::OperationStatusTranslationTable}"
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

## Awk

The `Awk` compute allows to process the table through an awk script.<br />
The script to execute can be put in an external embedded file for more readability and be called in the script value.<br />
Eg: ```yaml script: ${file::embeddedFile-1}``` for an embedded file named embeddedFile-1 placed in the connector directory of your connector.<br />
`keep` and `exclude` can be used to trim your result values with regular expressions.<br />
`separators` and `selectColumns` can be used to separate your result value into multiple columns and keep only those of your choice.

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
          - type: awk
            script: # <string>
            exclude:  # <string>
            keep: # <string>
            separators: # <string>
            selectColumns: # <string> | comma separated values
```

### <a id="awk-example" />Example

In this example, we will process our source through an Awk script.<br />
Each line that results from the execution of this script will then be filtered using the regular expression and separated into multiple columns using the ';' character as a separator and keep columns only from 2 to 12.

```yaml
          - type: awk
            script: "${file::embeddedFile-1}"
            keep: ^MSHW;
            separators: ;
            selectColumns: "2,3,4,5,6,7,8,9,10,11,12"
```

## Convert

The `Convert` compute allows make to convert a column from a hexadecimal value to a decimal value or an array of status into a simple status.

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
          - type: convert
            column: # <number>
            conversion: # <enum> | possible values: [ hex2Dec, array2SimpleStatus ]
```

## Divide

The `Divide` compute allows to perform an division operation to the values of a column. The column values must be numerics (integer, float, double, ...).<br />
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

## DuplicateColumn

The `DuplicateColumn` compute allows to duplicate a column. The added column will be set next to the duplicated column and move all 

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
          - type: duplicateColumn
            column: # <number>
```

## ExcludeMatchingLines

The `ExcludeMatchingLines` compute allows to remove the lines of your table where the selected column correspond to the `regExp` values or is equal to one of the values in the `valueList`.<br />

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
          - type: excludeMatchingLines
            column: # <number>
            regExp: # <string>
            valueList: # <string> | comma separated values
```

## Extract

The `Extract` compute allows to extract a part of a column and replace this column by this part.<br />
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

### <a id="extract-example" />Example

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


## ExtractPropertyFromWbemPath

The `ExtractPropertyFromWbemPath` compute allows to extract the specified property from a WBEM ObjectPath in the specified column.

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
          - type: extractPropertyFromWbemPath
            property: # <string>
            column: # <number>
```

## Json2Csv

The `Json2Csv` compute allows to convert a JSON value to a CSV value.<br />
The `entryKey` value is the key in the JSON data that will be shown as a new entry in the resulting CSV (i.e. a new line).<br />
The `properties` value is the list of strings specifying the properties of the entry key to be added to the CSV as new fields.

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
          - type: json2Csv
            entryKey: # <string>
            properties: # <string> | comma separated values
            separator: # <string> | default: ";"
```

## KeepColumns

The `KeepColumns` compute allows to keep only the selected columns from a table.

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
          - type: keepColumns
            columnNumbers: # <string> | comma separated values
```

## KeepOnlyMatchingLines

The `KeepOnlyMatchingLines` compute allows to keep only the lines of your table where the selected column correspond to the `regExp` values or is equal to one of the values in the `valueList`.

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
          - type: keepOnlyMatchingLines
            column: # <number>
            regExp: # <string>
            valueList: # <string> | comma separated values
```

## LeftConcat

The `LeftConcat` compute allow to concatenate a value at the beginning of all the lines in a selected column.<br />
The `value` can be a number, or a reference to another column of the same table, using the '$' character followed by the column number.<br />
Since the data is converted from a CSV to a Table, you can add columns by adding ';' characters using the rightConcat.

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
          - type: leftConcat
            column: # <number>
            value: # <string>
```

## Multiply

The `Multiply` compute allows to perform an multiplication operation to the values of a column. The column values must be numerics (integer, float, double, ...).<br />
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

## PerBitTranslation

The `PerBitTranslation` allows to translate a numeric value into a string based on its bits, each bit having a different meaning. See the [Translation Table Section](translation-tables.md) for more details on how to implement them.<br />
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

### <a id="perbittranslation-example" />Example

Here is an example of a `PerBitTranslation` compute.

```yaml

          - type: perBitTranslation
            column: 2
            bitList: "0,2,3,6,7,8,9,10,11,12,13,14"
            translationTable: "${translation::StatusInformationTranslationTable}"
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

## Replace

The `Replace` compute allows to replace a specific value in a column by another value.

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
          - type: replace
            column: # <number>
            existingValue: # <string>
            newValue: # <string>
```

## RightConcat

The `RightConcat` compute allow to concatenate a value at the end of all the lines in a selected column.<br />
The `value` can be a number, or a reference to another column of the same table, using the '$' character followed by the column number.<br />
Since the data is converted from a CSV to a Table, you can add columns by adding ';' characters using the rightConcat.

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
          - type: rightConcat
            column: # <number>
            value: # <string>
```

## Subtract

The `Subtract` compute allows to perform an subtraction operation to the values of a column. The column values must be numerics (integer, float, double, ...).<br />
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

## Substring

The `Substring` compute allows to extract a String from a column.

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
          - type: substring
            column: # <number>
            start: # <string>
            length: # <string>
```

## Translate

The `Translate` compute allows to translate values from specified column following a specified translation table.

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
          - type: translate
            column: # <number>
            translationTable:  # <string>
```

## Xml2Csv

The `Xml2Csv` compute allows to convert a XML source to a character-separated table.

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
          - type: xml2Csv
            recordTag: # <string>
            properties:  # <string> | comma separated values
```
 