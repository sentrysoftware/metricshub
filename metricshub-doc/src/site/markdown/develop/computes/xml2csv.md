keywords: xml, translate, map, csv, flat
description: "xml2Csv" transforms an XML source into a CSV table in MetricsHub.

# `xml2Csv`

The `xml2Csv` compute allows to convert a XML source to a character-separated table.

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
