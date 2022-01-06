keywords: configuration, protocols, snmp, wbem, wmi, ipmi, ssh, http, os command
description: How to configure Hardware Sentry Agent to scrape targets with various protocols.

# Monitoring Configuration

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

To collect metrics from your targets, you need to provide the following information to **${project.name}**:

* the hostname of the target to be monitored
* its type
* the protocol to be used.

This information must be provided in the **config/hws-config.yaml** file (an alternate path can be specified in [otel-config.yaml](configure-otel.md)).

The [YAML syntax](https://yaml.org/) of the configuration file must be strictly respected for **${project.name}** to operate correctly (notably the indentation). As changes in this file are taken into account immediately, there is no need to restart the *OpenTelemetry Collector*.

## Monitored Targets

Systems to monitor are defined under `targets` with the below syntax:

```yaml
targets:

- target:
    hostname: <hostname>
    type: <target-type>
  <protocol-configuration>
```

where:

* `<hostname>` is the name of the target, or its IP address
* `<target-type>` is the type of the target to be monitored. Possible values are:

    * `win` for [Microsoft Windows systems](https://www.sentrysoftware.com/docs/hardware-connectors/latest/platform-requirements.html#microsoft-windows)
    * `linux` for [Linux systems](https://www.sentrysoftware.com/docs/hardware-connectors/latest/platform-requirements.html#linux)
    * `network` for [network devices](https://www.sentrysoftware.com/docs/hardware-connectors/latest/platform-requirements.html#network-device)
    * `oob` for [Out-of-band management cards](https://www.sentrysoftware.com/docs/hardware-connectors/latest/platform-requirements.html#out-of-band), [VMware ESX systems](https://www.sentrysoftware.com/docs/hardware-connectors/latest/platform-requirements.html#vmware-esx), and [blade chassis](https://www.sentrysoftware.com/docs/hardware-connectors/latest/platform-requirements.html#blade-chassis")
    * `storage` for [storage systems](https://www.sentrysoftware.com/docs/hardware-connectors/latest/platform-requirements.html#storage-system)
    * `aix` for [IBM AIX systems](https://www.sentrysoftware.com/docs/hardware-connectors/latest/platform-requirements.html#ibm-aix)
    * `hpux` for [HP UX systems](https://www.sentrysoftware.com/docs/hardware-connectors/latest/platform-requirements.html#hp-ux)
    * `solaris` for [Oracle Solaris systems](https://www.sentrysoftware.com/docs/hardware-connectors/latest/platform-requirements.html#oracle-solaris)
    * `tru64` for [HP Tru64 systems](https://www.sentrysoftware.com/docs/hardware-connectors/latest/platform-requirements.html#hp-tru64)
    * `vms` for [HP Open VMS systems](https://www.sentrysoftware.com/docs/hardware-connectors/latest/platform-requirements.html#hp-openvms)

* `<protocol-configuration>` is the protocol(s) **${project.name}** will use to communicate with the targets: `http`, `ipmi`, `oscommand`, `ssh`, `snmp`, `wmi`, or `wbem`. Refer to [Specifying the protocol to be used](#protocol) for more details.

<a name="protocol"></a>

## Protocols and Credentials

### HTTP

Use the parameters below to configure the HTTP protocol:

| Parameter | Description                                                                      |
| --------- | -------------------------------------------------------------------------------- |
| http      | Protocol used to access the target.                                              |
| port      | The HTTPS port number used to perform SNMP queries (Default: 443).               |
| username  | Name used to establish the connection with the target via the HTTP protocol.     |
| password  | Password used to establish the connection with the target via the HTTP protocol. |

#### Example

```yaml
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

```yaml
targets:

- target:
    hostname: myhost-01
    type: oob
  ipmi:
    username: myusername
    password: mypwd
```

### OS Commands

Use the parameters below to configure OS Commands:

| Parameter       | Description                                                                                                                                               |
| --------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| osCommand       | Protocol used to access the target.                                                                                                                       |
| timeout         | How long until the local OS Commands time out (default: 120s).|
| useSudo         | Whether sudo is used or not for the local OS Command (true or false).                                                                                     |
| useSudoCommands | List of commands for which sudo is required.                                                                                                              |
| sudoCommand     | Sudo command to be used (Default: sudo).                                                                                                                  |

#### Example

```yaml
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
| timeout         | How long until the command times out (default: 120s). |
| useSudo         | Whether sudo is used or not for the SSH Command (true or false).                                                                                      |
| useSudoCommands | List of commands for which sudo is required.                                                                                                          |
| sudoCommand     | Sudo command to be used (Default: sudo).                                                                                                              |
| username        | Name to use for performing the SSH query.                                                                                                             |
| password        | Password to use for performing the SSH query.                                                                                                         |
| privateKey      | Private Key File to use to establish the connection to the host through the SSH protocol                                                              |

#### Example

```yaml
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
| timeout          | How long until the SNMP request times out (default: 120s). |
| privacy          | _SNMP v3 only_ - The type of encryption protocol (none, aes, des).                                                                                    |
| privacy password | _SNMP v3 only_ - Password associated to the privacy protocol.                                                                                         |
| username         | _SNMP v3 only_ - Name to use for performing the SNMP query.                                                                                           |
| password         | _SNMP v3 only_ - Password to use for performing the SNMP query.                                                                                       |

#### Example

```yaml
targets:

- target:
    hostname: myhost-01
    type: linux
  snmp:
    version: v1
    community: public
    port: 161
    timeout: 120s

- target:
    hostname: myhost-01
    type: linux
  snmp:
    version: v2c
    community: public
    port: 161
    timeout: 120s

- target:
    hostname: myhost-01
    type: linux
  snmp:
    version: v3-md5
    community: public
    port: 161
    timeout: 120s
    privacy: des
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
| timeout   | How long until the WBEM request times out (default: 120s). |
| username  | Name used to establish the connection with the target via the WBEM protocol.                                                                          |
| password  | Password used to establish the connection with the target via the WBEM protocol.                                                                      |

#### Example

```yaml
targets:

  - target:
      hostname: myhost-01
      type: storage
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
| timeout   | How long until the WMI request times out (default: 120s). |
| username  | Name used to establish the connection with the target via the WMI protocol.                                                                          |
| password  | Password used to establish the connection with the target via the WMI protocol.                                                                      |

#### Example

```yaml
targets:

  - target:
      hostname: myhost-01
      type: WIN
    wmi:
      timeout: 120s
      username: myusername
      password: mypwd
```

## Site and Sustainable IT Settings

You can specify additional labels to be added to each collected metric, with the `extraLabels` property:

```yaml
extraLabels:
  site: Datacenter 1 # Customize with your own site naming (dedicating 1 collector to 1 site is a good practice)
  label_name: "Label Value"
```

This is an easy way to add a label to all metrics collected by this instance of the **${project.name}**. We recommend that you run at least one separate instance of the *OpenTelemetry Collector* for each site, and specify a different `site` label value for each of them. This will be particularly useful to group monitored systems per their physical location.

Similarly, you can specify additional static metrics to be exposed with the `extraMetrics` property:

```yaml
extraMetrics:
  hw_carbon_density_grams: 350 # in g/kWh -- 350g/kWh is the average in Europe
  hw_electricity_cost_dollars: 0.12 # in $/kWh -- $0.12/kWh is the average for non-household in Europe
  hw_pue_ratio: 1.8
```

The above example configures the *OpenTelemetry Collector* to expose the carbon density and price per kWh of the electricity in the monitored site. These metrics can be leveraged in [Grafana dashboards](../integration/grafana.md) to calculate the carbon footprint, with different carbon densities for each monitored site, for example.

## Other Configuration Settings

### Timeout, Duration and Period Format

Timeouts, durations and periods are specified with the below format:

| Unit | Description                     | Examples         |
| ---- | ------------------------------- | ---------------- |
| s    | seconds                         | 120s             |
| m    | minutes                         | 90m, 1m15s       |
| h    | hours                           | 1h, 1h30m        |
| d    | days (based on a 24-hour day)   | 1d               |

### Collect Period

By default, **${project.name}** collects metrics from the monitored targets every minute. To change the default collect period:

* for all your targets, add the `collectPeriod` parameter just before the `targets` section:

    ```yaml
    collectPeriod: 2m

    targets: # ...
    ```

* for a specific target, add the `collectPeriod` parameter in the relevant `target` section:

    ```yaml
    targets:

    - target:
        hostname: myhost
        type: linux
      snmp:
        version: v1
        community: public
        port: 161
        timeout: 120s
      collectPeriod: 1m30s # Customized
    ```

There is a decorelation between the internal collect period and the scrape interval configured in [config/otel-config.yaml](configure-otel.md). **You need to make sure the internal collect period is shorter than the scrape interval** to avoid gaps or duplicate points, which would affect rate calculations.

<div class="alert alert-danger"><i class="icon-hand-up"></i>Collecting metrics too frequently can cause CPU-intensive workloads.</div>

### Discovery Cycle

**${project.name}** periodically performs discoveries to detect new components in your monitored environment. By default, **${project.name}** runs a discovery after 30 collects. To change this default discovery cycle:

* for all your targets, add the `discoveryCycle` just before the `targets` section:

    ```yaml
    discoveryCycle: 15

    targets: # ...
    ```

* for a specific target, add the `discoveryCycle` parameter in the relevant `target` section:

    ```yaml
    targets:

    - target:
        hostname: myhost
        type: linux
      snmp:
        version: v1
        community: public
        port: 161
        timeout: 120s
      discoveryCycle: 5 # Customized
    ```

and indicate the number of collects after which a discovery will be performed.

<div class="alert alert-danger"><i class="icon-hand-up"></i>Running discoveries too frequently can cause CPU-intensive workloads.</div>

### Job Pool Size

By default, **${project.name}** runs up to 20 discovery and collect jobs in parallel. To increase or decrease the number of jobs **${project.name}** can run simultaneously,  add the `jobPoolSize` parameter just before the `targets` section:

```yaml
jobPoolSize: 20

targets: # ...
```

and indicate a number of jobs.

<div class="alert alert-danger"><i class="icon-hand-up"></i>Running too many jobs in parallel can lead to an OutOfMemory error.</div>

### Force or Exclude Connectors

The **${project.name}** comes with the [Hardware Connector Library](https://www.sentrysoftware.com/docs/hardware-connectors/latest/), a library that consists of hundreds of hardware connectors that describe how to discover hardware components and detect failures. When running **${project.name}**, the connectors are automatically selected based on the device type provided and the enabled protocols. You can however indicate to **${project.name}** which connectors should be used or excluded.

Use the parameters below to select or exclude connectors:

| Parameter          | Description                                                                          |
| ------------------ | ------------------------------------------------------------------------------------ |
| selectedConnectors | Connector(s) to use to monitor the target. No automatic detection will be performed. |
| excludedConnectors | Connector(s) that must be excluded from the automatic detection.                     |

Connector names must be comma-separated, as shown in the example below:

```yaml
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

The exhaustive list of connectors is available in the [Hardware Connector Library User Documentation<](https://www.sentrysoftware.com/docs/hardware-connectors/latest/platform-requirements.html). The list of connectors in the current installation of **${project.name}** can be obtained with the below command:

```shell-session
$ hws -l
```

[More information on the `hws` command](../troubleshooting/cli.md)


### Hostname Resolution

By default, the *Hardware Sentry Agent* resolves the `hostname` value to a Fully Qualified Domain Name (FQDN) value which goes then in the [Host Resource](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/host.md) attribute `host.name`.
You can disable the hostname resolution by setting the `resolveHostnameToFqdn` property to `false`:

```yaml
resolveHostnameToFqdn: false

targets:

- target:
    hostname: host01
    type: Linux
```
This will skip resolving the hostname to a Fully Qualified Domain Name, then the [Host Resource](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/host.md) `host.name` attribute is set to the configured `hostname` value.

### Target Extra labels

You can specify additional labels to be reported on each metric collected from a specific target, using the `extraLabels` property:

```yaml
targets:

- target:
    hostname: host01
    type: Linux
  snmp:
    version: v1
    port: 161
    timeout: 120
  extraLabels:
    host.name: host01.internal.domain.net
    app: Jenkins
```

The example above configures the *Hardware Sentry Agent* to set the `host.name` attribute as `host01.internal.domain.net` for the [Host Resource](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/host.md) related to the configured target `host01`.
Let's say, we have a monitoring solution consuming OpenTelemetry data and already groups a set of metrics collected on the host `host01.internal.domain.net`, knowing that, this host is identified based on the attribute value `host.name`. To get the *Hardware Sentry Agent* metrics grouped under the same host (`host01.internal.domain.net`), the *Hardware Sentry Agent* can be configured to identify the target as `host01.internal.domain.net` using the same `host.name` attribute value through `extraLabels`.