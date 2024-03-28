keywords: develop, connector, criteria
description: This page defines the detection’s criteria that are defined in a connector.

## Command Line (Detection)

The goal of this part is to see how to define OS commands criteria.

```yaml
connector:
  # ...
  detection: # <object>
    # ...
    criteria: # <object-array>
    - type: commandLine
      commandLine: # <string>
      errorMessage: # <string>
      expectedResult: # <string>
      executeLocally: # <boolean>
      timeout: # <number>
```

### Input Properties

| Input Property | Description |
| -------------- | ----------- |
| `commandLine` | Command-line to be executed. Macros such as `%{USERNAME}`, `%{PASSWORD}` or `%{HOSTNAME}` may be used |
| `timeout` | Time in seconds after which the command is stopped is considered failed. If not provided, the default OS command timeout will  be used |
| `errorMessage` | The message to display if the detection criteria fails |
| `executeLocally` | Specifies if the command must be executed locally even when monitoring a remote system (`0`, `false`, `1`, `true`) |
| `expectedResult` | Regular expression that is expected to match the result of the OS command |

### Example

```yaml
connector:
  detection:
    criteria:
    - type: commandLine
      commandLine: naviseccli -help
      expectedResult: Navisphere
      executeLocally: true
      errorMessage: Not a Navisphere system
```
