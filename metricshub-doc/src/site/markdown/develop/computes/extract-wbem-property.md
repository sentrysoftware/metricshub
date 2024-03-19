keywords: wbem, path, cim
description: The "extractPropertyFromWbemPath" is a very WBEM specific compute operation in MetricsHub to extract the value of a specified property from a CIMPath.

# `extractPropertyFromWbemPath`

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
