keywords: develop, connector, criteria
description: This page defines the detection’s criteria that are defined in a connector.

# SNMP Get (Detection)

The goal of this part is to see how to define SNMP Get criteria.

```yaml
connector:
  # ...
  detection: # <object>
    # ...
    criteria: # <object-array>
    - type: snmpGet
      oid: # <string>
      expectedResult: # <string>
```

### Input Properties

| Input Property | Description |
| -------------- | ----------- |
| `oid` | Object Identifier (OID) used to perform the SNMP request. The request must be successful |
| `expectedResult` | Regular expression that is expected to match the result of the SNMP request. If not specified, a successful SNMP request will be sufficient for the criteria to be met |

### Example

```yaml
connector:
  detection:
    criteria:
    - type: snmpGet
      oid: 1.3.6.1.4.1.318.1.1.26.2.1.3.1
      expectedResult: PDU
```
