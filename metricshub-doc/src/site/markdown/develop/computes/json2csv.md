keywords: json, translate, map, csv, flat
description: "json2Csv" transforms a JSON source into a CSV table in MetricsHub.

# `json2Csv`

The `json2Csv` compute allows to convert a JSON value to a CSV value.
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
