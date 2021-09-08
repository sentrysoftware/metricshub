# Configuration

Create the configuration file ```hardware-sentry-config.yml``` to monitor one or several targets.

The ```hardware-sentry-config.yml``` configuration file must be located in the same directory as the ```hardware-sentry-prometheus-<version>.jar``` file.

The format, intentation and syntax of the configuration file must be strictly respected for **${project.name}** to operate properly. See examples below.

## Configurations per Protocols

Refer to the [Supported Platforms](https://www.sentrysoftware.com/library/hc/24/platform-requirements.html) documentation to know 

### SNMP Configurations

```
---
targets:

- target:
    hostname: myhost-01
    type: LINUX
  snmp:
    version: V1
    community: public
    port: 161
    timeout: 120

- target:
    hostname: myhost-01
    type: LINUX
  snmp:
    version: V2c
    community: public
    port: 161
    timeout: 120

- target:
    hostname: myhost-01
    type: LINUX
  snmp:
    version: V3
    username: myusername
    authentication protocol: None
    authentication password: authpwd
    privacy protocol: None
    privacy password: privatepwd
    context name: contexname
    port: 161
    timeout: 120
```

|Parameter | Description |
|---------|------|
|hostname|Hostname of the target.|
|type|Type of the system (OS or platform type).|
|snmp|Protocol and credentials used to access the target.|
|version|The version of the SNMP protocol (V1, V2c or V3)|
|community|The SNMP Community string to use to perform SNMP v1 queries (Default: public)|
|username|SNMP V3 only - Name to use for performing the SNMP query.|
|authentication protocol|_SNMP V3 only_ - Protocol used to authenticate the SNMP v3 messages (None, MD5 or SHA).|
|authentication password|_SNMP V3 only_ - Password used to authenticate the SNMP v3 messages|
|privacy protocol|_SNMP V3 only_ - Protocol used to authenticate the SNMP v3 messages (None, AES or DES).|
|privacy password|_SNMP V3 only_ - Password associated to the privacy protocol.|
|context name|_SNMP V3 only_ - Name accessible to the SNMP entity.|
|port|The SNMP port number used to perform SNMP queries (Default: 161).|
|timeout|How long until the SNMP request times out (default: 120s).|

### WBEM Configuration

```
---
targets:

  - target:
      hostname: myhost-01
      type: STORAGE
    wbem:
      protocol: HTTPS
      port: 5989
      timeout: 120
      username: myusername
      password: mypwd
```

|Parameter | Description |
|---------|------|
|hostname|Hostname of the target.|
|type|Type of the system (OS or platform type).|
|wbem |Protocol and credentials used to access the target.|
|protocol|The protocol used to access the target.|
|port|The https port number used to perform WBEM queries (Default: 5989 for https or 5988 for http).|
|timeout |How long until the WBEM request times out (default: 120s).|
|username|Name used to establish the connection with the target via the WBEM protocol.|
|password|Password used establish the connection with the target via the WBEM protocol.|

### HTTP Configuration

```
---
targets:

  - target:
      hostname: myhost-01
      type: STORAGE
    http:
      https: true
      port: 443
      username: myusername
      password: mypwd
```

|Parameter | Description |
|---------|------|
|hostname|Hostname of the target.|
|type|Type of the system (OS or platform type).|
|http |Protocol and credentials used to access the target.|
|port|The https port number used to perform SNMP queries (Default: 443).|
|username|Name used establish the connection with the target via the WBEM protocol.|
|password|Password used establish the connection with the target via the HTTP protocol.|

### WMI Configuration

```
---
targets:

  - target:
      hostname: myhost-01
      type: MS_WINDOWS
    wmi:
      timeout: 120
      username: myusername
      password: mypwd
```

|Parameter | Description |
|---------|------|
|hostname|Hostname of the target.|
|type|Type of the system (OS or platform type).|
|wmi |Protocol and credentials used to access the target.|
|timeout |How long until the WMI request times out (default: 120s).|
|username|Name used to establish the connection with the target via the WMI protocol.|
|password|Password used establish the connection with the target via the WMI protocol.|

### IPMI Configuration

```
---
targets:

- target:
    hostname: myhost-01
    type: MGMT_CARD_BLADE_ESXI
  ipmi:
    username: myusername
    password: mypwd
```

|Parameter | Description |
|---------|------|
|hostname|Hostname of the target.|
|type|Type of the system (OS or platform type).|
|ipmi |Protocol and credentials used to access the target.|
|username|Name used to establish the connection with the target via the IPMI protocol.|
|password|Password used establish the connection with the target via the IPMI protocol.|

### SSH Configuration

COMING SOON!