keywords: develop, connector, criteria
description: This page defines the detection’s criteria that are defined in a connector.

# Product Requirements (Detection)

The goal of this part is to see how to define Product Requirements criteria.

```yaml
connector:
  # ...
  detection: # <object>
    # ...
    criteria: # <object-array>
    - type: productRequirements
      engineVersion: # <string>
      kmVersion: # <string>
```

### Input Properties

| Input Property | Description |
| -------------- | ----------- |
| `kmVersion` | Minimum required KM version, using this format: x.y.z (ex: 3.1.01) |
| `engineVersion` | Minimum required engine version, using this format: x.y.z (ex: 3.1.01) |

### Example

```yaml
connector:
  detection:
    criteria:
    - type: productRequirements
      engineVersion: 4.1.00
```
