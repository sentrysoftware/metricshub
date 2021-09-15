keywords: configuration, protocols, snmp, wbem, wmi, ipmi, ssh, http
description: How to configure Hardware Sentry Prometheus Exporter to scrape targets with various protocols.

# Configuration

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

Create the configuration file ```hardware-sentry-config.yml``` to monitor one or several targets.

The ```hardware-sentry-config.yml``` configuration file must be located in the same directory as the ```hardware-sentry-prometheus-<version>.jar``` file.

The format, indentation and syntax of the configuration file must be strictly respected for **${project.name}** to operate properly. See examples below.

## Configurations per Protocols

Refer to the <a href="https://www.sentrysoftware.com/library/hc/24/platform-requirements.html" target="_blank">Supported Platforms</a> documentation to know more about the properties that you need to set for each specific supported platform.



### SNMP Configurations

```
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
    version: v3_md5 
    community: public
    port: 161
    timeout: 120
    privacy: DES   
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
|community|The SNMP Community string to use to perform SNMP v1 queries (Default: public).|
|port|The SNMP port number used to perform SNMP queries (Default: 161).|
|timeout|How long until the SNMP request times out (default: 120s).|
|privacy|_SNMP V3 only_ - The type of encryption protocol (NO_ENCRYPTION, AES, DES).|
|privacy password|_SNMP V3 only_ - Password associated to the privacy protocol.|
|username|_SNMP V3 only_ - Name to use for performing the SNMP query.|
|password|_SNMP V3 only_ - Password to use for performing the SNMP query.|


### WBEM Configuration

```
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
|port|The HTTPS port number used to perform WBEM queries (Default: 5989 for HTTPS or 5988 for HTTP).|
|timeout |How long until the WBEM request times out (default: 120s).|
|username|Name used to establish the connection with the target via the WBEM protocol.|
|password|Password used to establish the connection with the target via the WBEM protocol.|

### HTTP Configuration

```
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
|port|The HTTPS port number used to perform SNMP queries (Default: 443).|
|username|Name used to establish the connection with the target via the HTTP protocol.|
|password|Password used to establish the connection with the target via the HTTP protocol.|

### WMI Configuration

```
targets:

  - target:
      hostname: myhost-01
      type: WIN
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
targets:

- target:
    hostname: myhost-01
    type: OOB
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

By default, **${project.name}** automatically selects the appropriate connectors to scrape metrics from a target. However, you can manually specify or exclude connectors by setting the following parameters:

|Parameter | Description |
|---------|------|
|selectedConnectors| Enter the file name(s) of the collector(s) you want to use (.hdf or .hdfs file).|
|excludedConnectors| Enter the file name(s) of the collector you do NOT want to use (.hdf or .hdfs file).|

Connector file names must be comma-separated, as shown in the example below:

```
targets:

  - target:
      hostname: myhost-01
      type: WIN
    wmi:
      timeout: 120
      username: myusername
      password: mypwd
    selectedConnectors: [ VMwareESX4i, VMwareESXi ]
    excludedConnectors: [ VMwareESXiDisksIPMI, VMwareESXiDisksStorage ]
```

### Mapping Unknown Status

On rare occasions, **${project.name}** may collect an unexpected value from a Monitor and return an _Unknown Status_. You can configure the ```unknownStatus``` settings to trigger a specific action: 

  * **0** to set the Monitor's status to **OK**
  * **1** to trigger a **WARNING** on the Monitor's status (WARN)
  * **2** to trigger an **ALARM** on the Monitor's status (ALARM)

Default is **1** (WARN) as shown below:

```
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