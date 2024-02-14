keywords: guide, connector, computes
description: This page describes all the compute formats support by the connector

# Computes

* [Format](#format)
* [Add](#add)
* [And](#and)
* [ArrayTranslate](#array-translate)
* [Awk](#awk)
* [Convert](#convert)
* [Divide](#divide)
* [DuplicateColumn](#duplicate-column)
* [ExcludeMatchingLines](#exclude-matching-lines)
* [Extract](#extract)
* [ExtractPropertyFromWbemPath](#extract-property-from-wbem-path)
* [Json2Csv](#json2csv)
* [KeepColumns](#keep-columns)
* [KeepOnlyMatchingLines](#keep-only-matching-lines)
* [LeftConcat](#left-concat)
* [Multiply](#multiply)
* [PerBitTranslation](#per-bit-translation)
* [Replace](#replace)
* [RightConcat](#right-concat)
* [Subtract](#subtract)
* [Substring](#substring)
* [Translate](#translate)
* [Xml2Csv](#xml2csv)

## <a id="format" />Format

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

## <a id="add" />Add

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

## <a id="and" />And

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

## <a id="array-translate" />ArrayTranslate

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

## <a id="awk" />Awk

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

## <a id="convert" />Convert

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

## <a id="divide" />Divide

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

## <a id="duplicate-column" />DuplicateColumn

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

## <a id="exclude-matching-lines" />ExcludeMatchingLines

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

## <a id="extract" />Extract

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

## <a id="extract-property-from-wbem-path" />ExtractPropertyFromWbemPath

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

## <a id="json2csv" />Json2Csv

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

## <a id="keep-columns" />KeepColumns

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

## <a id="keep-only-matching-lines" />KeepOnlyMatchingLines

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

## <a id="left-concat" />LeftConcat

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

## <a id="multiply" />Multiply

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

## <a id="per-bit-translation" />PerBitTranslation

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

## <a id="replace" />Replace

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

## <a id="right-concat" />RightConcat

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

## <a id="subtract" />Subtract

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

## <a id="substring" />Substring

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

## <a id="translate" />Translate

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

## <a id="xml2csv" />Xml2Csv

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
 