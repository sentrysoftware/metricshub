keywords: guide, connector, criteria
description: This page defines the detection’s criteria that are defined in a connector.

# Criteria

In order for a connector to match a system, some criterion must be met. They are defined in the [Detection Section](detection.md) of the connector and checked during the discovery. If all criterion are met, the connector matches and will be used to monitor the system. If a criteria is not met, MetricsHub stops processing the detection and as far as the target system is concerned, the connector will not be used.

A maximum number of 99 detection criterion may be defined in a connector.

* [HTTP](#http)
    * [Input Properties](#http-input-properties)
    * [Example](#http-example)
* [IPMI](#ipmi)
    * [Input Properties](#ipmi-input-properties)
    * [Example](#ipmi-example)
* [Product Requirements](#product-requirements)
    * [Input Properties](#product-requirements-input-properties)
    * [Example](#product-requirements-example)
* [Device Type](#device-type)
    * [Input Properties](#device-type-input-properties)
    * [Supported OS List](#device-type-os-list)
    * [Example](#device-type-example)
* [OS Command](#os-command)
    * [Input Properties](#os-command-input-properties)
    * [Example](#os-command-example)
* [Process](#process)
    * [Input Properties](#process-input-properties)
    * [Example](#process-example)
* [Service](#service)
    * [Input Properties](#service-input-properties)
    * [Example](#service-example)
* [SNMP Get](#snmp-get)
    * [Input Properties](#snmp-get-input-properties)
    * [Example](#snmp-get-example)
* [SNMP GetNext](#snmp-getnext)
    * [Input Properties](#snmp-getnext-input-properties)
    * [Example](#snmp-getnext-example)
* [WBEM](#wbem)
    * [Input Properties](#wbem-input-properties)
    * [Example](#wbem-example)
* [WMI](#wmi)
    * [Input Properties](#wmi-input-properties)
    * [Example](#wmi-example)

## <a id="http" /> HTTP

The goal of this part is to see how to define HTTP criteria.

```yaml
connector:
  # ...
  detection: # <object>
    # ...
    criteria: # <object-array>
    - type: http
      method: # <enum> | possible values: [ get, post, delete, put ]
      url: # <string>
      path: # <string>
      header: # <string>
      body: # <string>
      expectedResult: # <string>
      resultContent: # <enum> | possible values: [ httpStatus, header, body, all ]
      authenticationToken: # <string>
      errorMessage: # <string>
```

### <a id="http-input-properties" /> Input Properties

| Input Property        | Description       |
| --------------------- | ----------------- |
| `method` | The HTTP request method type: `get`, `post`, `delete`, put (default: `get`) |
| `url` | The url to connect to.<br />Macros such as `%{AUTHENTICATIONTOKEN}`, `%{USERNAME}`, `%{PASSWORD}`, `%{PASSWORD_BASE64}`, `%{BASIC_AUTH_BASE64}` or `%{AUTHENTICATIONTOKEN}` may be used. |
| `path` | The path to connect to.<br />Macros such as `%{AUTHENTICATIONTOKEN}`, `%{USERNAME}`, `%{PASSWORD}`, `%{PASSWORD_BASE64}`, `%{BASIC_AUTH_BASE64}` or `%{AUTHENTICATIONTOKEN}` may be used. |
| `header` | The HTTP request’s header.<br />Macros such as `%{AUTHENTICATIONTOKEN}`, `%{USERNAME}`, `%{PASSWORD}`, `%{PASSWORD_BASE64}`, `%{BASIC_AUTH_BASE64}` or `%{AUTHENTICATIONTOKEN}` may be used. |
| `body` | The HTTP request’s body.<br />Macros such as `%{AUTHENTICATIONTOKEN}`, `%{USERNAME}`, `%{PASSWORD}`, `%{PASSWORD_BASE64}`, `%{BASIC_AUTH_BASE64}` or `%{AUTHENTICATIONTOKEN}` may be used. |
| `expectedResult` | Regular expression that is expected to match the result of the `HTTP` request. |
| `resultContent` | Extracts the specified content from the HTTP request’s result (default: `body`). |
| `authenticationToken` | The authentication token (typically a reference to another source). |
| `errorMessage` | The error message to display if the expectedResult regular expression evaluates to false. |

### <a id="http-example" /> Example

```yaml
connector:
  detection:
    criteria:
    - type: http
      method: GET
      path: /api/DeviceService/Devices
      header: ${file::http-header}
      expectedResult: api
      errorMessage: Failed to get response from API
```

## <a id="ipmi" /> IPMI

The goal of this part is to see how to define IPMI criteria.

```yaml
connector:
  # ...
  detection: # <object>
    # ...
    criteria: # <object-array>
   - type: ipmi
```

### <a id="ipmi-input-properties" /> Input Properties

None, the product will run an IPMI command to determine if IPMI is available.

### <a id="ipmi-example" /> Example
```yaml
connector:
  detection:
    criteria:
    - type: ipmi
```

## <a id="product-requirements" /> Product Requirements

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

### <a id="product-requirements-input-properties" />Input Properties
| Input Property | Description |
| -------------- | ----------- |
| `kmVersion` | Minimum required KM version, using this format: x.y.z (ex: 3.1.01) |
| `engineVersion` | Minimum required engine version, using this format: x.y.z (ex: 3.1.01) |

### <a id="product-requirements-example" /> Example
```yaml
connector:
  detection:
    criteria:
    - type: productRequirements
      engineVersion: 4.1.00
```

## <a id="device-type" /> Device Type

The goal of this part is to see how to define Device Type criteria.

```yaml
connector:
  # ...
  detection: # <object>
    # ...
    criteria: # <object-array>
    - type: deviceType
      keep: # <enum-array> | possible values: [ vms, osf1, hp, rs6000, linux, oob, nt, network, storage, solaris, sunos ]
      exclude: # <enum-array> | possible values: [ vms, osf1, hp, rs6000, linux, oob, nt, network, storage, solaris, sunos ]
```

### <a id="device-type-input-properties" /> Input Properties
| Input Property | Description |
| -------------- | ----------- |
| `exclude` | List of operating systems, separated by commas, that should not match the monitored system |
| `keep` | List of operating systems, separated by commas. The monitored system's OS must match one of the listed item |

### <a id="device-type-os-list" /> Supported OS List
| Operating System | OS Type |
| -------------- | ------------------ |
| HP OpenVMS | `VMS` |
| HP Tru64 UNIX	| `OSF1`, `tru64` |
| HP-UX | `HP`, `hpux` |
| IBM AIX | `RS600` |
| Linux (RedHat, SuSe, ESX) | `Linux` |
| Management Card/Chip, Blade Chassis, ESXi | `OOB` (Out-of Band) |
| Network | `Network` |
| Storage | `Storage` |
| Sun Solaris | `Solaris` |
| Microsoft Windows | `NT`, `win` |

### <a id="device-type-example" /> Example
```yaml
connector:
  detection:
    criteria:
    - type: deviceTYpe
      keep: [ vms, osf1, hp, rs6000, linux, oob, nt, network, storage, solaris, sunos ]
```

## <a id="os-command" /> OS Command

The goal of this part is to see how to define OS commands criteria.

```yaml
connector:
  # ...
  detection: # <object>
    # ...
    criteria: # <object-array>
    - type: osCommand
      commandLine: # <string>
      errorMessage: # <string>
      expectedResult: # <string>
      executeLocally: # <boolean>
      timeout: # <number>
```

### <a id="os-command-input-properties" /> Input Properties
| Input Property | Description |
| -------------- | ----------- |
| `commandLine` | Command-line to be executed. Macros such as `%{USERNAME}`, `%{PASSWORD}` or `%{HOSTNAME}` may be used |
| `timeout` | Time in seconds after which the command is stopped is considered failed. If not provided, the default OS command timeout will  be used |
| `errorMessage` | The message to display if the detection criteria fails |
| `executeLocally` | Specifies if the command must be executed locally even when monitoring a remote system (`0`, `false`, `1`, `true`) |
| `expectedResult` | Regular expression that is expected to match the result of the OS command |

### <a id="os-command-example" /> Example
```yaml
connector:
  detection:
    criteria:
    - type: osCommand
      commandLine: naviseccli -help
      expectedResult: Navisphere
      executeLocally: true
      errorMessage: Not a Navisphere system
```

## <a id="process" /> Process

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

### <a id="process-input-properties" /> Input Properties
| Input Property | Description |
| -------------- | ----------- |
| `commandLine` | Regular expression that should match the command line of a process currently running on the monitored system |

### <a id="process-example" /> Example
```yaml
connector:
  detection:
    criteria:
    - type: process
      processCommandLine: naviseccli -help
```

## <a id="service" /> Service

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

### <a id="service-input-properties" /> Input Properties
| Input Property | Description |
| -------------- | ----------- |
| `name` | Regular expression that must match the name of a service currently running on the monitored system |

### <a id="service-example" /> Example
```yaml
connector:
  detection:
    criteria:
    - type: service
      name: TWGIPC
```

## <a id="snmp-get" /> SNMP Get

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

### <a id="snmp-get-input-properties" /> Input Properties
| Input Property | Description |
| -------------- | ----------- |
| `oid` | Object Identifier (OID) used to perform the SNMP request. The request must be successful |
| `expectedResult` | Regular expression that is expected to match the result of the SNMP request. If not specified, a successful SNMP request will be sufficient for the criteria to be met |

### <a id="snmp-get-example" /> Example
```yaml
connector:
  detection:
    criteria:
    - type: snmpGet
      oid: 1.3.6.1.4.1.318.1.1.26.2.1.3.1
      expectedResult: PDU
```

## <a id="snmp-getnext" /> SNMP GetNext

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

### <a id="snmp-getnext-input-properties" /> Input Properties
| Input Property | Description |
| -------------- | ----------- |
| `oid` | Object Identifier (OID) used to perform the SNMP get next request. The value returned is the content of the variable that is lexicographically next in the MIB. The request must be successful and the result OID must be a child of the provided OID. |
| `expectedResult` | Regular expression that is expected to match the result of the SNMP request . If not specified, a successful SNMP request will be sufficient for the criteria to be met |

### <a id="snmp-getnext-example" /> Example
```yaml
connector:
  detection:
    criteria:
    - type: snmpGetNext
      oid: 1.3.6.1.4.1.674.10892.5.5.1.20.130.4
```

## <a id="wbem" /> WBEM

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

### <a id="wbem-input-properties" /> Input Properties
| Input Property | Description |
| -------------- | ----------- |
| `query` | WBEM query to be executed |
| `namespace` | WBEM namespace providing the context for the WBEM query. Use `automatic` so that the namespace automatically determined |
| `expectedResult` | Regular expression that is expected to match the result of the WBEM Query |

### <a id="wbem-example" /> Example
```yaml
connector:
  detection:
    criteria:
    - type: wbem
      query: SELECT Name,Dedicated FROM EMC_StorageSystem
      namespace: root/emc
      expectedResult: [;|]3|[0-9|]*;$
```

## <a id="wmi" /> WMI

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

### <a id="wmi-input-properties" /> Input Properties
| Input Property | Description |
| -------------- | ----------- |
| `query` | WMI query to be executed |
| `namespace` | WMI namespace providing the context for the WMI query. Use `automatic` so that the namespace is automatically determined |
| `expectedResult` | Regular expression that is expected to match the result of the WMI query |

### <a id="wmi-example" /> Example
```yaml
connector:
  detection:
    criteria:
    - type: wmi
      query: SELECT Name FROM WMINET_InstrumentedAssembly
      namespace: root\LibreHardwareMonitor
```