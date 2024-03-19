keywords: develop, connector, criteria
description: This page defines the detection’s criteria that are defined in a connector.

## Process (Detection)

The goal of this part is to see how to define process criteria.

```yaml
connector:
  # ...
  detection: # <object>
    # ...
    criteria: # <object-array>
    - type: process
      commandLine: # <string>
```

### Input Properties

| Input Property | Description |
| -------------- | ----------- |
| `commandLine` | Regular expression that should match the command line of a process currently running on the monitored system |

### Example

```yaml
connector:
  detection:
    criteria:
    - type: process
      processCommandLine: naviseccli -help
```
