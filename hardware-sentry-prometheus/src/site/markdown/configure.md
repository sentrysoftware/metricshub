keywords: configuration, protocols, snmp, wbem, wmi, ipmi, ssh, http
description: How to configure Hardware Sentry Prometheus Exporter to scrape targets with various protocols.

# Configuration

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

Create the configuration file ```hardware-sentry-config.yml``` to monitor one or several targets.

The ```hardware-sentry-config.yml``` configuration file must be located in the same directory as the ```hardware-sentry-prometheus-<version>.jar``` file.

The format, indentation and syntax of the configuration file must be strictly respected for **${project.name}** to operate properly. See examples below.

## Configurations per Protocols

Refer to the [Supported Platforms](https://www.sentrysoftware.com/library/hc/24/platform-requirements.html) documentation to know more about the properties that you need to set for each specific supported platform.

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

target:
  hostname: myhost-01
  type: LINUX
snmp:
  version: v3_md5  # possible values: v1, v2c, v3_no_auth, v3_md5, v3_sha;
  community: public
  port: 161
  timeout: 120
  privacy: DES  # possible values: 
  privacyPassword: myprivacypwd
  username: myusername
  password: mypwd
```

|Parameter | Description |
|---------|------|
|hostname|Hostname of the target.|
|type|Type of the system (OS or platform type).|
|snmp|Protocol and credentials used to access the target.|
|version|The version of the SNMP protocol (V1, V2c, V3_no_auth, V3_md5, V3_sha).|
|community|The SNMP Community string to use to perform SNMP v1 queries (Default: public)|
|port|The SNMP port number used to perform SNMP queries (Default: 161).|
|timeout|How long until the SNMP request times out (default: 120s).|
|username|_SNMP V3 only_ - Name to use for performing the SNMP query.|
|password|_SNMP V3 only_ - Password to use for performing the SNMP query.|
|privacy|_SNMP V3 only_ - The type of encryption protocol (NO_ENCRYPTION, AES, DES).|
|privacy password|_SNMP V3 only_ - Password associated to the privacy protocol.|

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
|password|Password used to establish the connection with the target via the WBEM protocol.|

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
|password|Password used to establish the connection with the target via the HTTP protocol.|

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
|password|Password used to establish the connection with the target via the WMI protocol.|

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
|password|Password used to establish the connection with the target via the IPMI protocol.|

### SSH Configuration

COMING SOON!

## Other Configurations

### Auto-detection

By default, **${project.name}** selects the appropriate connector to scrape metrics from a target. However, you can manually select or exclude a connector by setting the following parameters:

|Parameter | Description |
|---------|------|
|selectedConnectors| Enter the name of the collector you want to use (.hdf or .hdfs file).|
|excludedConnectors| Enter the name of the collector you do NOT want to use (.hdf or .hdfs file).|

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
    selectedConnectors:
    - MS_HW_Director61NT.hdfs
    excludedConnectors: []
```

### Mapping Unknown Status

On rare occasions, **${project.name}** collects an unexpected value from a target and returns an _Unknown Status_. You can set the ```unknownStatus``` parameter to OK, WARN or ALARM when the target' status is unknown and cannot be translated. Default is WARN. See the example below:

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
    unknownStatus: WARN
```