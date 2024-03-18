keywords: snmp, mib, oid
description: The "snmpTable" source type polls an entire table from an SNMP agent to process it with MetricsHub.

# SNMP Table (Source)

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
          type: snmpTable
          oid: # <string>
          selectColumns: # <string> | comma separated values
          forceSerialization: <boolean>
          executeForEachEntryOf: # <object>
            source: # <string>
            concatMethod: # onOf [ <enum>, <object> ] | possible values for <enum> : [ list, json_array, json_array_extended ]
              concatStart: # <string>
              concatEnd: # <string>
          computes: # <compute-object-array>
```

