keywords: mib, snmp
description: The "snmpGet" source polls the specified SNMP OID with MetricsHub.

# SNMP Get (Source)

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
          type: snmpGet
          oid: # <string>
          forceSerialization: <boolean>
          executeForEachEntryOf: # <object>
            source: # <string>
            concatMethod: # onOf [ <enum>, <object> ] | possible values for <enum> : [ list, json_array, json_array_extended ]
              concatStart: # <string>
              concatEnd: # <string>
          computes: # <compute-object-array>
```

