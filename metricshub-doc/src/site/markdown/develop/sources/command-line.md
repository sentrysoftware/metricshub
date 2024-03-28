keywords: ssh, bash, shell, command, cli, batch, powershell
description: The "commandLine" source executes the specified command and allows the processing of its output.

# Command Line (Source)

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
          type: commandLine
          commandLine: # <string>
          timeout: # <number>
          executeLocally: # <boolean>
          exclude: # <string>
          keep: # <string>
          beginAtLineNumber: # <number>
          endAtLineNumber: # <number>
          separators: # <string>
          selectColumns: # <string> | comma separated values
          forceSerialization: <boolean>
          executeForEachEntryOf: # <object>
            source: # <string>
            concatMethod: # onOf [ <enum>, <object> ] | possible values for <enum> : [ list, json_array, json_array_extended ]
              concatStart: # <string>
              concatEnd: # <string>
          computes: # <compute-object-array>
```
