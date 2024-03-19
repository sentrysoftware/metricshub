keywords: join, table, wbem, sql
description: Use the "tableJoin" source type to mix and match 2 sources like a JOIN in SQL, or Pivot in Excel.

# Table Join (Source)

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
          type: tableJoin
          leftTable: # <string>
          rightTable: # <string>
          leftKeyColumn: # <number>
          rightKeyColumn: # <number>
          defaultRightLine: # <string> | comma separated values
          isWbemKey: # <boolean>
          forceSerialization: <boolean>
          computes: <compute-object-array>
```
