keywords: sql, h2, database
description: The SQL source allows to perform SQL queries on other Sources.

# SQL Source

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
          type: sql
          tables: <sqltable-object-array>
          - source: <string>
            alias: <string>
            columns: <sqlcolumn-object-array>
            - name: <string>
              number: <integer>
              type: <string>
          query: <string>
          computes: <compute-object-array>
```

