keywords: configuration, protocols, snmp, wbem, wmi, ipmi, ssh, http, os command
description: How to configure Hardware Sentry Prometheus Exporter to scrape targets with various protocols.

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

# Configuration

To collect metrics from your targets, you need to provide the following information to **${project.name}**:

- the hostname of the target to be monitored
- its type
- the protocol to be used.

This information must be provided in a `hardware-sentry-config.yml` file, which should be stored in the directory from where you launch the `${project.artifactId}-${project.version}.jar` file, unless you want to [specify a relative path to this file](./operate.html) while running ${project.description}.

The format, indentation and syntax of the configuration file must be strictly respected for **${project.name}** to operate correctly.

## Specifying the target to be monitored

Copy the following lines in the `hardware-sentry-config.yml` file:

```
targets:

- target:
    hostname: <hostname>
    type: <target-type>
  <protocol-configuration>
```

where:

- `<hostname>` corresponds to the name of the target, or its IP address
- `<target-type>` corresponds to the operating system or the type of the target to be monitored. Possible values are:

    - `win` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#microsoft-windows" target="_blank">Microsoft Windows systems</a>
    - `linux` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#linux" target="_blank">Linux systems</a>
    - `network` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#network-device" target="_blank">network devices</a>
    - `oob` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#out-of-band" target="_blank">Out-of-band</a>, <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#blade-chassis" target="_blank">blade chassis</a>, and <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#vmware-esx" target="_blank">VMware ESX systems</a>
    - `storage` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#storage-system" target="_blank">storage systems</a>
    - `tru64` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#hp-tru64" target="_blank">HP Tru64 systems</a>
    - `hpux` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#hp-ux" target="_blank">HP UX systems</a>
    - `aix` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#ibm-aix" target="_blank">IBM AIX systems</a>
    - `solaris` for these <a href="https://www.sentrysoftware.com/library/hc/24/platform-requirements.html#oracle-solaris" target="_blank">Oracle solaris systems</a>
    - `vms` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#hp-openvms" target="_blank">HP Open VMS systems</a>

* `<protocol-configuration>` corresponds to the protocol **${project.name}** will use to communicate with the targets. Refer to [Specifying the protocol to be used](#protocol) for more details.

<a name="protocol"></a>

## Specifying the protocol to be used

### HTTP

Use the parameters below to configure the HTTP protocol:

| Parameter | Description                                                                      |
| --------- | -------------------------------------------------------------------------------- |
| http      | Protocol used to access the target.                                              |
| port      | The HTTPS port number used to perform SNMP queries (Default: 443).               |
| username  | Name used to establish the connection with the target via the HTTP protocol.     |
| password  | Password used to establish the connection with the target via the HTTP protocol. |

#### Example

```
targets:

  - target:
      hostname: myhost-01
      type: storage
    http:
      https: true
      port: 443
      username: myusername
      password: mypwd
```

### IPMI

Use the parameters below to configure the IPMI protocol:

| Parameter | Description                                                                      |
| --------- | -------------------------------------------------------------------------------- |
| ipmi      | Protocol used to access the target.                                              |
| username  | Name used to establish the connection with the target via the IPMI protocol.     |
| password  | Password used to establish the connection with the target via the IPMI protocol. |

#### Example

```
targets:

- target:
    hostname: myhost-01
    type: OOB
  ipmi:
    username: myusername
    password: mypwd
```

### OS Commands

Use the parameters below to configure OS Commands:

| Parameter       | Description                                                                                                                                               |
| --------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| osCommand       | Protocol used to access the target.                                                                                                                       |
| timeout         | How long until the local OS Commands time out (default: 120s). See [Configuring Timeout Durations](#Configuring_Timeout_Durations) for available options. |
| useSudo         | Whether sudo is used or not for the local OS Command (true or false).                                                                                     |
| useSudoCommands | List of commands for which sudo is required.                                                                                                              |
| sudoCommand     | Sudo command to be used (Default: sudo).                                                                                                                  |

#### Example

```
targets:
  - target:
      hostname: myhost-01
      type: linux
    osCommand:
      timeout: 120
      useSudo: true
      useSudoCommands: [ cmd1, cmd2 ]
      sudoCommand: sudo
```

### SSH

Use the parameters below to configure the SSH protocol:

| Parameter       | Description                                                                                                                                           |
| --------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| ssh             | Protocol used to access the target.                                                                                                                   |
| timeout         | How long until the SNMP request times out (default: 120s). See [Configuring Timeout Durations](#Configuring_Timeout_Durations) for available options. |
| useSudo         | Whether sudo is used or not for the SSH Command (true or false).                                                                                      |
| useSudoCommands | List of commands for which sudo is required.                                                                                                          |
| sudoCommand     | Sudo command to be used (Default: sudo).                                                                                                              |
| username        | Name to use for performing the SSH query.                                                                                                             |
| password        | Password to use for performing the SSH query.                                                                                                         |
| privateKey      | Private Key File to use to establish the connection to the host through the SSH protocol                                                              |

#### Example

```
targets:
  - target:
      hostname: myhost-01
      type: linux
    ssh:
      timeout: 120
      useSudo: true
      useSudoCommands: [ cmd1, cmd2 ]
      sudoCommand: sudo
      username: myusername
      password: mypwd
      privateKey: /tmp/ssh-key.txt

```

### SNMP

Use the parameters below to configure the SNMP protocol:

| Parameter        | Description                                                                                                                                           |
| ---------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| snmp             | Protocol used to access the target.                                                                                                                   |
| version          | The version of the SNMP protocol (v1, v2c, v3-no-auth, v3-md5, v3-sha).                                                                               |
| community        | The SNMP Community string to use to perform SNMP v1 queries (Default: public).                                                                        |
| port             | The SNMP port number used to perform SNMP queries (Default: 161).                                                                                     |
| timeout          | How long until the SNMP request times out (default: 120s). See [Configuring Timeout Durations](#Configuring_Timeout_Durations) for available options. |
| privacy          | _SNMP v3 only_ - The type of encryption protocol (none, aes, des).                                                                                    |
| privacy password | _SNMP v3 only_ - Password associated to the privacy protocol.                                                                                         |
| username         | _SNMP v3 only_ - Name to use for performing the SNMP query.                                                                                           |
| password         | _SNMP v3 only_ - Password to use for performing the SNMP query.                                                                                       |

#### Example

```
targets:

- target:
    hostname: myhost-01
    type: LINUX
  snmp:
    version: v1
    community: public
    port: 161
    timeout: 120s

- target:
    hostname: myhost-01
    type: LINUX
  snmp:
    version: v2c
    community: public
    port: 161
    timeout: 120s

- target:
    hostname: myhost-01
    type: LINUX
  snmp:
    version: v3-md5
    community: public
    port: 161
    timeout: 120s
    privacy: DES
    privacyPassword: myprivacypwd
    username: myusername
    password: mypwd
```

### WBEM

Use the parameters below to configure the WBEM protocol:

| Parameter | Description                                                                                                                                           |
| --------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| wbem      | Protocol used to access the target.                                                                                                                   |
| protocol  | The protocol used to access the target.                                                                                                               |
| port      | The HTTPS port number used to perform WBEM queries (Default: 5989 for HTTPS or 5988 for HTTP).                                                        |
| timeout   | How long until the WBEM request times out (default: 120s). See [Configuring Timeout Durations](#Configuring_Timeout_Durations) for available options. |
| username  | Name used to establish the connection with the target via the WBEM protocol.                                                                          |
| password  | Password used to establish the connection with the target via the WBEM protocol.                                                                      |

#### Example

```
targets:

  - target:
      hostname: myhost-01
      type: STORAGE
    wbem:
      protocol: HTTPS
      port: 5989
      timeout: 120s
      username: myusername
      password: mypwd
```

### WMI

Use the parameters below to configure the WMI protocol:

| Parameter | Description                                                                                                                                          |
| --------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- |
| wmi       | Protocol used to access the target.                                                                                                                  |
| timeout   | How long until the WMI request times out (default: 120s). See [Configuring Timeout Durations](#Configuring_Timeout_Durations) for available options. |
| username  | Name used to establish the connection with the target via the WMI protocol.                                                                          |
| password  | Password used to establish the connection with the target via the WMI protocol.                                                                      |

#### Example

```
targets:

  - target:
      hostname: myhost-01
      type: WIN
    wmi:
      timeout: 120s
      username: myusername
      password: mypwd
```

## Other Configuration Settings

### Specifying the connectors to be used

The **${project.name}** comes with the **Hardware Connector Library**, a library which consists of hundreds of hardware connectors that describe how to discover hardware components and detect failures. When running **${project.name}**, the connectors are automatically selected based on the device type provided and the enabled protocols. You can however indicate to **${project.name}** which connectors should be used or excluded.

Use the parameters below to select or exclude connectors:

| Parameter          | Description                                                                            |
| ------------------ | -------------------------------------------------------------------------------------- |
| selectedConnectors | Enter the name of the connector(s) you want to use to collect hardware metrics.        |
| excludedConnectors | Enter the name of the connector(s) you do NOT want to use to collect hardware metrics. |

Connector names must be comma-separated, as shown in the example below:

```
targets:

  - target:
      hostname: myhost-01
      type: WIN
    wmi:
      timeout: 120s
      username: myusername
      password: mypwd
    selectedConnectors: [ VMwareESX4i, VMwareESXi ]
    excludedConnectors: [ VMwareESXiDisksStorage ]
```

The exhaustive list of connectors is available in the <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html" target="_blank">Hardware Connector Library User Documentation</a>.

### Configuring the Unknown Status

On rare occasions, **${project.name}** may collect an unexpected value from a metric and return an _Unknown Status_. You can configure the `unknownStatus` setting to indicate the value to be exposed in Prometheus:

- **0** to expose the value **0 (OK)**
- **1** to expose the value **1 (WARN)**
- **2** to expose the value **2 (ALARM)**
- **""** (empty) not to expose the metric. 

Default is **1** as shown below:

```
targets:

  - target:
      hostname: myhost-01
      type: STORAGE
    wbem:
      protocol: HTTPS
      port: 5989
      timeout: 120s
      username: myusername
      password: mypwd
    unknownStatus: 1
```

### Configuring Timeout Durations

**${project.name}** supports the Prometheus time duration formats. Timeout durations are specified as a number, immediately followed by one or a combination of the following units:

| Unit | Description                     | Examples         |
| ---- | ------------------------------- | ---------------- |
| s    | seconds                         | 120s (default)   |
| m    | minutes                         | 90m, 30m15s      |
| h    | hours                           | 1h, 1h30m        |
| d    | days (based on a 24-hour day)   | 1d, 2d5h45m15s   |
| w    | weeks (based on a 7-day week)   | 1w, 1w2d3h15m20s |
| y    | years (based on a 365-day year) | 1y, 1y1w1h30m10s |
