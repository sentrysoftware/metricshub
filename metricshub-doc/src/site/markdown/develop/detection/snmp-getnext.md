keywords: develop, connector, criteria
description: This page defines the detection’s criteria that are defined in a connector.

# SNMP GetNext (Detection)

The goal of this part is to see how to define SNMP GetNext criteria.

```yaml
connector:
  # ...
  detection: # <object>
    # ...
    criteria: # <object-array>
    - type: snmpGetNext
      oid: # <string>
      expectedResult: # <string>
```

## Input Properties

| Input Property | Description |
| -------------- | ----------- |
| `oid` | Object Identifier (OID) used to perform the SNMP get next request. The value returned is the content of the variable that is lexicographically next in the MIB. The request must be successful and the result OID must be a child of the provided OID. |
| `expectedResult` | Regular expression that is expected to match the result of the SNMP request . If not specified, a successful SNMP request will be sufficient for the criteria to be met |

### Example

```yaml
connector:
  detection:
    criteria:
    - type: snmpGetNext
      oid: 1.3.6.1.4.1.674.10892.5.5.1.20.130.4
```
