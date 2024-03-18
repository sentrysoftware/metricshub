keywords: develop, connector, criteria
description: This page defines the detection’s criteria that are defined in a connector.

# WBEM (Detection)

The goal of this part is to see how to define WBEM criteria.

```yaml
connector:
  # ...
  detection: # <object>
    # ...
    criteria: # <object-array>
    - type: wbem
      query: # <string>
      namespace: # <string>
      expectedResult: # <string>
```

### Input Properties

| Input Property | Description |
| -------------- | ----------- |
| `query` | WBEM query to be executed |
| `namespace` | WBEM namespace providing the context for the WBEM query. Use `automatic` so that the namespace automatically determined |
| `expectedResult` | Regular expression that is expected to match the result of the WBEM Query |

### Example

```yaml
connector:
  detection:
    criteria:
    - type: wbem
      query: SELECT Name,Dedicated FROM EMC_StorageSystem
      namespace: root/emc
      expectedResult: [;|]3|[0-9|]*;$
```
