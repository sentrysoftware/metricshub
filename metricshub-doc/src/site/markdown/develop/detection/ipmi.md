keywords: develop, connector, criteria
description: This page defines the detection’s criteria that are defined in a connector.

## IPMI (Detection)

The goal of this part is to see how to define IPMI criteria.

```yaml
connector:
  # ...
  detection: # <object>
    # ...
    criteria: # <object-array>
   - type: ipmi
```

### Input Properties

None, the product will run an IPMI command to determine if IPMI is available.

### Example

```yaml
connector:
  detection:
    criteria:
    - type: ipmi
```
