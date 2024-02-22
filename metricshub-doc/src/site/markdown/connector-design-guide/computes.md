keywords: guide, connector, computes
description: This page describes all the compute formats support by the connector

# Computes

This page describes all the compute formats support by the connector.

* [Format](#Format)
* [Add](#Add)
* [And](#And)
* [ArrayTranslate](#ArrayTranslate)
* [Awk](#Awk)
* [Convert](#Convert)
* [Divide](#Divide)
* [DuplicateColumn](#DuplicateColumn)
* [ExcludeMatchingLines](#ExcludeMatchingLines)
* [Extract](#Extract)
* [ExtractPropertyFromWbemPath](#ExtractPropertyFromWbemPath)
* [Json2Csv](#Json2csv)
* [KeepColumns](#KeepColumns)
* [KeepOnlyMatchingLines](#KeepOnlyMatchingLines)
* [LeftConcat](#LeftConcat)
* [Multiply](#Multiply)
* [PerBitTranslation](#PerBitTranslation)
* [Replace](#Replace)
* [RightConcat](#RightConcat)
* [Subtract](#Subtract)
* [Substring](#Substring)
* [Translate](#Translate)
* [Xml2Csv](#Xml2csv)

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

## Add

The `Add` compute allows to perform an addition operation to the values of a column. The column values must be numerics (integer, float, double, ...).
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

The `And` compute allows make a bitwise `and` on the values of a column.
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

## ArrayTranslate

The `ArrayTranslate` compute allows make to translate a column containing an array of values using a translation table. See the [Translation Table Section](translation-tables.md) for more details on how to implement them.
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

## Awk

The `Awk` compute allows to process the table through an awk script.
The script to execute can be put in an external embedded file for more readability and be called in the script value. 
Eg: ```yaml script: ${file::embeddedFile-1}``` for an embedded file named embeddedFile-1 placed in the connector directory of your connector.
`keep` and `exclude` can be used to trim your result values with regular expressions.
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

The `Divide` compute allows to perform an division operation to the values of a column. The column values must be numerics (integer, float, double, ...).
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

The `DuplicateColumn` compute allows to duplicate a column. The added column will be set next to the duplicated column.


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

The `ExcludeMatchingLines` compute allows to remove the lines of your table where the selected column correspond to the `regExp` values or is equal to one of the values in the `valueList`.

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

The `Extract` compute allows to extract a part of a column and replace this column by this part.
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

The `Json2Csv` compute allows to convert a JSON value to a CSV value.
The `entryKey` value is the key in the JSON data that will be shown as a new entry in the resulting CSV (i.e. a new line).
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

The `LeftConcat` compute allow to concatenate a value at the beginning of all the lines in a selected column.
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
          - type: leftConcat
            column: # <number>
            value: # <string>
```

## Multiply

The `Multiply` compute allows to perform an multiplication operation to the values of a column. The column values must be numerics (integer, float, double, ...).
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

The `PerBitTranslation` allows to translate a numeric value into a string based on its bits, each bit having a different meaning. See the [Translation Table Section](translation-tables.md) for more details on how to implement them.
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

The `RightConcat` compute allow to concatenate a value at the end of all the lines in a selected column.
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
          - type: rightConcat
            column: # <number>
            value: # <string>
```

## Subtract

The `Subtract` compute allows to perform an subtraction operation to the values of a column. The column values must be numerics (integer, float, double, ...).
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
 