keywords: develop, connector, criteria
description: This page defines the detection’s criteria that are defined in a connector.

# Service (Detection)

The goal of this part is to see how to define service criteria.

```yaml
connector:
  # ...
  detection: # <object>
    # ...
    criteria: # <object-array>
    - type: service
      name: # <string>
```

### Input Properties

| Input Property | Description |
| -------------- | ----------- |
| `name` | Regular expression that must match the name of a service currently running on the monitored system |

### Example

```yaml
connector:
  detection:
    criteria:
    - type: service
      name: TWGIPC
```
