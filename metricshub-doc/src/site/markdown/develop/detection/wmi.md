keywords: develop, connector, criteria
description: This page defines the detection’s criteria that are defined in a connector.

# WMI (Detection)

The goal of this part is to see how to define WMI criteria.

```yaml
connector:
  # ...
  detection: # <object>
    # ...
    criteria: # <object-array>
    - type: wmi
      query: # <string>
      namespace: # <string>
      expectedResult: # <string>
```

## Input Properties

| Input Property | Description |
| -------------- | ----------- |
| `query` | WMI query to be executed |
| `namespace` | WMI namespace providing the context for the WMI query. Use `automatic` so that the namespace is automatically determined |
| `expectedResult` | Regular expression that is expected to match the result of the WMI query |

### Example

```yaml
connector:
  detection:
    criteria:
    - type: wmi
      query: SELECT Name FROM WMINET_InstrumentedAssembly
      namespace: root\LibreHardwareMonitor
```
