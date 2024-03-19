keywords: wmi, wql, cim, win32, windows
description: The "WMI" source type queries the WINMGMT service on a Windows system with a WQL query in MetricsHub.

# WMI (Source)

```yaml
connector:
  # ...
pre: # <object>
  <sourceKey>: # <source-object>

monitors:
  <monitorType>: # <object>
    <job>: # <object>
      sources: # <object>
        <sourceKey>:
          type: wmi
          query: # <string>
          namespace: # <string>
          forceSerialization: <boolean>
          executeForEachEntryOf: # <object>
            source: # <string>
            concatMethod: # oneOf [ <enum>, <object> ] | possible values for <enum> : [ list, json_array, json_array_extended ]
              concatStart: # <string>
              concatEnd: # <string>
          computes: <compute-object-array>
```
