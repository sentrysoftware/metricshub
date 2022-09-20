keywords: agent, configuration, protocols, snmp, wbem, wmi, ipmi, ssh, http, os command, winrm, sites
description: How to configure Hardware Sentry Agent to scrape hosts with various protocols.

# Configure the Hardware Sentry Agent

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

The **Hardware Sentry Agent** collects the hardware health of the monitored systems and pushes the collected data to the OTLP receiver. **${project.name}** then processes the hardware observability and sustainability metrics and exposes them in the backend platform of your choice (Datadog, BMC Helix, Prometheus, Grafana, etc.).

To ensure this process runs smoothly, you need to configure a few settings in the `config/hws-config.yaml` file to allow **${project.name}** to:

* identify which site is monitored with this agent
* calculate the electricity costs and the carbon footprint of this site
* monitor the systems in this site.

Note that all changes made to the  `config/hws-config.yaml` file are taken into account immediately. There is therefore no need to restart **${project.name}**.

## Configure a site

A site represents the data center or the server room in which all the systems to be monitored are located. Configure your site in the `extraLabels` section of the `config/hws-config.yaml` file as shown in the example below:

```yaml
extraLabels:
  site: boston 
```

## Configure the sustainability settings

To obtain the electricity costs and carbon footprint of your site, configure the `extraMetrics` section of the `config/hws-config.yaml` file as follows:

```yaml 
extraMetrics:
  hw.site.carbon_density_grams: 350 # in g/kWh
  hw.site.electricity_cost_dollars: 0.12 # in $/kWh
  hw.site.pue_ratio: 1.8
```

where:
* `hw.site.carbon_density_grams` is the **carbon density in grams per kiloWatthour**. This information is required to calculate the carbon emissions of your site. The carbon density corresponds to the amount of CO₂ emissions produced per kWh of electricity and varies depending on the country and the region where the data center is located. See the [electricityMap Web site](https://app.electricitymap.org/map) for reference. 
* `hw.site.electricity_cost_dollars` is the **electricity price in dollars per kiloWattHour**. This information is required to calculate the energy cost of your site. Refer to your energy contract to know the tariff by kilowatt per hour charged by your supplier or refer to the [GlobalPetrolPrices Web site](https://www.globalpetrolprices.com/electricity_prices/).
* `hw.site.pue_ratio` is the **Power Usage Effectiveness (PUE)** of your site. By default, sites are set with a PUE of 1.8, which is the average value for typical data centers.

## Configure the monitored hosts

To collect metrics from your hosts, you must provide the following information in the `config/hws-config.yaml` file:

* the hostname of the host to be monitored
* its type
* the protocol to be used.

> **Important**: Because a typo or incorrect indentation in the `hws-config.yaml` file could cause your hardware monitoring to fail, it is highly recommended to install the [vscode-yaml](https://github.com/redhat-developer/vscode-yaml) extension in your editor to benefit from tooltips and autocompletion suggested by the [Hardware Sentry Configuration](https://json.schemastore.org/hws-config.json) JSON Schema.

### Monitored hosts

Systems to monitor are defined under `hosts` with the below syntax:

```yaml
hosts:

- host:
    hostname: <hostname>
    type: <host-type>
  <protocol-configuration>
```

where:

* `<hostname>` is the name of the host, or its IP address
* `<host-type>` is the type of the host to be monitored. Possible values are:

    * `win` for Microsoft Windows systems
    * `linux` for Linux systems
    * `network` for network devices
    * `oob` for Out-of-band management cards
    * `storage` for storage systems
    * `aix` for IBM AIX systems
    * `hpux` for HP UX systems
    * `solaris` for Oracle Solaris systems
    * `tru64` for HP Tru64 systems
    * `vms` for HP Open VMS systems
  Refer to [Monitored Systems](../platform-requirements.html) for more details.
  
* `<protocol-configuration>` is the protocol(s) **${project.name}** will use to communicate with the hosts: `http`, `ipmi`, `oscommand`, `ssh`, `snmp`, `wmi`, `wbem` or `winrm`. Refer to [Protocols and credentials](#protocol) for more details.

<a name="protocol"></a>

### Protocols and credentials

#### HTTP

Use the parameters below to configure the HTTP protocol:

| Parameter | Description                                                                      |
| --------- | -------------------------------------------------------------------------------- |
| http      | Protocol used to access the host.                                                |
| port      | The HTTPS port number used to perform HTTP requests (Default: 443).              |
| username  | Name used to establish the connection with the host via the HTTP protocol.       |
| password  | Password used to establish the connection with the host via the HTTP protocol.   |

**Example**

```yaml
hosts:

  - host:
      hostname: myhost-01
      type: storage
    http:
      https: true
      port: 443
      username: myusername
      password: mypwd
```

#### IPMI

Use the parameters below to configure the IPMI protocol:

| Parameter | Description                                                                    |
| --------- | ------------------------------------------------------------------------------ |
| ipmi      | Protocol used to access the host.                                              |
| username  | Name used to establish the connection with the host via the IPMI protocol.     |
| password  | Password used to establish the connection with the host via the IPMI protocol. |

**Example**

```yaml
hosts:

- host:
    hostname: myhost-01
    type: oob
  ipmi:
    username: myusername
    password: mypwd
```

#### OS commands

Use the parameters below to configure OS Commands that are executed locally:

| Parameter       | Description                                                                           |
| --------------- | ------------------------------------------------------------------------------------- |
| osCommand       | Protocol used to access the host.                                                     |
| timeout         | How long until the local OS Commands time out (Default: 120s).                        |
| useSudo         | Whether sudo is used or not for the local OS Command: true or false (Default: false). |
| useSudoCommands | List of commands for which sudo is required.                                          |
| sudoCommand     | Sudo command to be used (Default: sudo).                                              |

**Example**

```yaml
hosts:
  - host:
      hostname: myhost-01
      type: linux
    osCommand:
      timeout: 120
      useSudo: true
      useSudoCommands: [ cmd1, cmd2 ]
      sudoCommand: sudo
```

#### SSH

Use the parameters below to configure the SSH protocol:

| Parameter       | Description                                                                               |
| --------------- | ----------------------------------------------------------------------------------------- |
| ssh             | Protocol used to access the host.                                                         |
| timeout         | How long until the command times out (Default: 120s).                                     |
| useSudo         | Whether sudo is used or not for the SSH Command (true or false).                          |
| useSudoCommands | List of commands for which sudo is required.                                              |
| sudoCommand     | Sudo command to be used (Default: sudo).                                                  |
| username        | Name to use for performing the SSH query.                                                 |
| password        | Password to use for performing the SSH query.                                             |
| privateKey      | Private Key File to use to establish the connection to the host through the SSH protocol. |

**Example**

```yaml
hosts:
  - host:
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

#### SNMP

Use the parameters below to configure the SNMP protocol:

| Parameter        | Description                                                                    |
| ---------------- | ------------------------------------------------------------------------------ |
| snmp             | Protocol used to access the host.                                              |
| version          | The version of the SNMP protocol (v1, v2c, v3-no-auth, v3-md5, v3-sha).        |
| community        | The SNMP Community string to use to perform SNMP v1 queries (Default: public). |
| port             | The SNMP port number used to perform SNMP queries (Default: 161).              |
| timeout          | How long until the SNMP request times out (Default: 120s).                     |
| privacy          | _SNMP v3 only_ - The type of encryption protocol (none, aes, des).             |
| privacy password | _SNMP v3 only_ - Password associated to the privacy protocol.                  |
| username         | _SNMP v3 only_ - Name to use for performing the SNMP query.                    |
| password         | _SNMP v3 only_ - Password to use for performing the SNMP query.                |

**Example**

```yaml
hosts:

- host:
    hostname: myhost-01
    type: linux
  snmp:
    version: v1
    community: public
    port: 161
    timeout: 120s

- host:
    hostname: myhost-01
    type: linux
  snmp:
    version: v2c
    community: public
    port: 161
    timeout: 120s

- host:
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

#### WBEM

Use the parameters below to configure the WBEM protocol:

| Parameter | Description                                                                                    |
| --------- | ---------------------------------------------------------------------------------------------- |
| wbem      | Protocol used to access the host.                                                              |
| protocol  | The protocol used to access the host.                                                          |
| port      | The HTTPS port number used to perform WBEM queries (Default: 5989 for HTTPS or 5988 for HTTP). |
| timeout   | How long until the WBEM request times out (Default: 120s).                                     |
| username  | Name used to establish the connection with the host via the WBEM protocol.                     |
| password  | Password used to establish the connection with the host via the WBEM protocol.                 |

**Example**

```yaml
hosts:

  - host:
      hostname: myhost-01
      type: storage
    wbem:
      protocol: https
      port: 5989
      timeout: 120s
      username: myusername
      password: mypwd
```

#### WMI

Use the parameters below to configure the WMI protocol:

| Parameter | Description                                                                   |
| --------- | ----------------------------------------------------------------------------- |
| wmi       | Protocol used to access the host.                                             |
| timeout   | How long until the WMI request times out (Default: 120s).                     |
| username  | Name used to establish the connection with the host via the WMI protocol.     |
| password  | Password used to establish the connection with the host via the WMI protocol. |

**Example**

```yaml
hosts:

  - host:
      hostname: myhost-01
      type: win
    wmi:
      timeout: 120s
      username: myusername
      password: mypwd
```

#### WinRM

Use the parameters below to configure the WinRM protocol:

| Parameter       | Description                                                                                          |
| --------------- | ---------------------------------------------------------------------------------------------------- |
| winrm           | Protocol used to access the host.                                                                    |
| timeout         | How long until the WinRM request times out (Default: 120s).                                          |
| username        | Name used to establish the connection with the host via the WinRM protocol.                          |
| password        | Password used to establish the connection with the host via the WinRM protocol.                      |
| protocol        | The protocol used to access the host: HTTP or HTTPS (Default: HTTP).                                 |
| port            | The port number used to perform WQL queries and commands (Default: 5985 for HTTP or 5986 for HTTPS). |
| authentications | Ordered list of authentication schemes: NTLM, KERBEROS (Default: NTLM).                              |

**Example**

```yaml
hosts:

  - host:
      hostname: server-11
      type: win
    winrm:
      protocol: http
      port: 5985
      username: myusername
      password: mypwd
      timeout: 120s
      authentications: [ntml]
```

## Additional settings (Optional)

### Alert Settings

#### Disabling Alerts (Not Recommended)

To disable **${project.name}**'s alerts:

* for all your hosts, set the `disableAlerts` parameter to `true` just before the `hosts` section:

    ```yaml
    disableAlerts: true

    hosts: # ...
    ```

* for a specific host, set the `disableAlerts` parameter to `true` in the relevant `host` section:

    ```yaml
    hosts:

    - host:
        hostname: myhost
        type: linux
      snmp:
        version: v1
        community: public
        port: 161
        timeout: 120s
      disableAlerts: true
    ```

#### Hardware Problem template

When detecting a hardware problem, **${project.name}** triggers an alert as OpenTelemetry log. The alert body is built from the following template:

```
Hardware problem on ${FQDN} with ${MONITOR_NAME}.${NEWLINE}${NEWLINE}${ALERT_DETAILS}${NEWLINE}${NEWLINE}${FULLREPORT}
```

To change this default hardware problem template:

* for all your hosts, configure the `hardwareProblemTemplate` parameter just before the `hosts` section:

    ```yaml
    hardwareProblemTemplate: Custom hardware problem on ${FQDN} with ${MONITOR_NAME}.

    hosts: # ...
    ```

* for a specific host, configure the `hardwareProblemTemplate` parameter in the relevant `host` section:

    ```yaml
    hosts:

    - host:
        hostname: myhost
        type: linux
      snmp:
        version: v1
        community: public
        port: 161
        timeout: 120s
      hardwareProblemTemplate: Custom hardware problem on myhost with ${MONITOR_NAME}.
    ```

and indicate the template to use when building alert messages.

For more information about the alert mechanism and the macros to use, refer to the [Alerts](../alerts.md) page.


### Authentication Settings

#### Basic authentication header

The **Hardware Sentry Agent**'s internal `OTLP Exporter` authenticates itself with the _OpenTelemetry Collector_'s [OTLP gRPC Receiver](configure-otel.md#OTLP_gRPC) by including the HTTP `Authorization` request header with the credentials. A predefined *Basic Authentication Header* value is stored internally and included in each request when sending telemetry data.

To override the default value of the *Basic Authentication Header*, add a new `Authorization` header under the `exporter:otlp:headers` section:

```yaml
exporter:
  otlp:
    headers:
      Authorization: Basic <credentials>

hosts: # ...
```

where `<credentials>` are built by first joining your username and password with a colon (`myUsername:myPassword`) and then encoding the value in `base64`.

For more security, encrypt the `Basic <credentials>` value. See [Encrypting Passwords](../security/passwords.md#Encrypting_Passwords) for more details.

> **Warning**: If you update the *Basic Authentication Header*, you must generate a new `.htpasswd` file for the [OpenTelemetry Collector Basic Authenticator](configure-otel.md#Basic_Authenticator).

#### OTLP endpoint

**Hardware Sentry Agent**'s internal `OTLP Exporter` pushes telemetry [signals](https://opentelemetry.io/docs/concepts/signals/) to the [`OTLP Receiver`](https://github.com/open-telemetry/opentelemetry-collector/tree/main/receiver/otlpreceiver) through [gRPC](https://grpc.io/) on port **TCP/4317**. By default, the internal `OTLP Exporter` is configured to push data to the `OTLP Receiver` endpoint `https://localhost:4317`.

To override the OTLP endpoint, configure the `endpoint` property under the `exporter:otlp` section:
```yaml
exporter:
  otlp:
    endpoint: https://my-host:4317

hosts: #...
```

### Monitoring Settings

#### Collect period

By default, **${project.name}** collects metrics from the monitored hosts every minute. To change the default collect period:

* for all your hosts, add the `collectPeriod` parameter just before the `hosts` section:

    ```yaml
    collectPeriod: 2m

    hosts: # ...
    ```

* for a specific host, add the `collectPeriod` parameter in the relevant `host` section:

    ```yaml
    hosts:

    - host:
        hostname: myhost
        type: linux
      snmp:
        version: v1
        community: public
        port: 161
        timeout: 120s
      collectPeriod: 1m30s # Customized
    ```

> **Warning**: Collecting metrics too frequently can cause CPU-intensive workloads.

#### Connectors

The **${project.name}** comes with the *Hardware Connector Library*, a library that consists of hundreds of hardware connectors that describe how to discover hardware components and detect failures. When running **${project.name}**, the connectors are automatically selected based on the device type provided and the enabled protocols. You can however indicate to **${project.name}** which connectors should be used or excluded.

Use the parameters below to select or exclude connectors:

| Parameter          | Description                                                                          |
| ------------------ | ------------------------------------------------------------------------------------ |
| selectedConnectors | Connector(s) to use to monitor the host. No automatic detection will be performed.   |
| excludedConnectors | Connector(s) that must be excluded from the automatic detection.                     |

Connector names must be comma-separated, as shown in the example below:

```yaml
hosts:

  - host:
      hostname: myhost-01
      type: win
    wmi:
      timeout: 120s
      username: myusername
      password: mypwd
    selectedConnectors: [ VMwareESX4i, VMwareESXi ]
    excludedConnectors: [ VMwareESXiDisksStorage ]
```

> **Note**: Any mispelled connector will be ignored.

To know which connectors are available, refer to [Monitored Systems](../platform-requirements.html#!) or run the below command:

```shell-session
$ hws -l
```

For more information about the `hws` command, refer to [Hardware Sentry CLI (hws)](../troubleshooting/cli.md)

#### Discovery cycle

**${project.name}** periodically performs discoveries to detect new components in your monitored environment. By default, **${project.name}** runs a discovery after 30 collects. To change this default discovery cycle:

* for all your hosts, add the `discoveryCycle` just before the `hosts` section:

    ```yaml
    discoveryCycle: 15

    hosts: # ...
    ```

* for a specific host, add the `discoveryCycle` parameter in the relevant `host` section:

    ```yaml
    hosts:

    - host:
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

> **Warning**: Running discoveries too frequently can cause CPU-intensive workloads.

#### Extra labels

Add labels in the `extraLabels` section to override the data collected by the **Hardware Sentry Agent** or add additional attributes to the [Host Resource](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/host.md). These attributes are added to each metric of that *Resource* when exported to time series platforms like Prometheus. 

In the example below, we override the `host.name` attribute resolved by **${project.name}** with `host01.internal.domain.net` and indicate that it is the `Jenkins` app:

```yaml
hosts:

- host:
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

#### Hostname resolution

By default, **${project.name}** resolves the `hostname` of the host to a Fully Qualified Domain Name (FQDN) and displays this value in the [Host Resource](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/host.md) attribute `host.name`. To display the configured hostname instead, set `resolveHostnameToFqdn` to `false`:

```yaml
resolveHostnameToFqdn: false

hosts:

- host:
    hostname: host01
    type: Linux
```

#### Job pool size

By default, **${project.name}** runs up to 20 discovery and collect jobs in parallel. To increase or decrease the number of jobs **${project.name}** can run simultaneously,  add the `jobPoolSize` parameter just before the `hosts` section:

```yaml
jobPoolSize: 20

hosts: # ...
```

and indicate a number of jobs.

> **Warning**: Running too many jobs in parallel can lead to an OutOfMemory error.

#### Sequential mode

By default, **${project.name}** sends the queries to the host in parallel. Although the parallel mode is faster than the sequential one, too many requests at the same time can lead to the failure of the targeted system.

To force all the network calls to be executed in sequential order:

* for all your hosts, enable the `sequential` option just before the `hosts` section (**NOT RECOMMENDED**):

    ```yaml
    sequential: true

    hosts: # ...
    ```

* for a specific host, enable the `sequential` option in the relevant `host` section:

    ```yaml
    hosts:

    - host:
        hostname: myhost
        type: linux
      snmp:
        version: v1
        community: public
        port: 161
        timeout: 120s
      sequential: true # Customized
    ```

> **Warning**: Sending requests in sequential mode slows down the monitoring significantly. Instead of using the sequential mode, you could increase the maximum number of allowed concurrent requests in the monitored system, if the manufacturer allows it.

#### Timeout, Duration and Period Format

Timeouts, durations and periods are specified with the below format:

| Unit | Description                     | Examples         |
| ---- | ------------------------------- | ---------------- |
| s    | seconds                         | 120s             |
| m    | minutes                         | 90m, 1m15s       |
| h    | hours                           | 1h, 1h30m        |
| d    | days (based on a 24-hour day)   | 1d               |

### OpenTelemetry Collector process settings

**Hardware Sentry Agent** launches the _OpenTelemetry Collector_ as a child process by running the `otel/otelcol-contrib` executable which reads the `otel/otel-config.yaml` file to start its internal components.

To customize the way the _OpenTelemetry Collector_ process is started, update the `otelCollector` section in `config/hws-config.yaml`:

```yaml
otelCollector:
  commandLine: [<otelcol-contrib>, <arguments...>]
  environment:
    <ENV_KEY1>: <ENV_VALUE1>
    <ENV_KEY2>: <ENV_VALUE2>
    # ...
  output: log|console|silent
  workingDir: <PATH>
  disabled: false

hosts: # ...
```

> **Note**: It is not recommended to change these settings unless you have a specific need.

#### Command line

By default, **Hardware Sentry Agent** launches the _OpenTelemetry Collector_ process using the following command line: `otel/otelcol-contrib --config otel/otel-config.yaml --feature-gates=pkg.translator.prometheus.NormalizeName`.

If you want to run your own distribution of the _OpenTelemetry Collector_ or update the default program's arguments such as the `--feature-gates` options, you need to override the _OpenTelemetry Collector_ default command line by setting the `commandLine` property under the `otelCollector` section:

```yaml
otelCollector:
  commandLine: 
    - /opt/hws-otel-collector/otel/my-otelcol
    - --config
    - /opt/hws-otel-collector/otel/my-otel-config.yaml
    - --feature-gates=pkg.translator.prometheus.NormalizeName

hosts: # ...
```

#### Disabling the collector (Not recommended)

In some cases, you might want the **Hardware Sentry Agent** to send OpenTelemetry signals directly to an existing _OpenTelemetry Collector_ running the `gRPC OTLP Receiver`, in which case, running a local _OpenTelemetry Collector_ is unnecessary.

To disable the _OpenTelemetry Collector_, set the `disabled` property to `true` under the `otelCollector` section:

```yaml
otelCollector:
  disabled: true

hosts: # ...
```

#### Environment

When **${project.name}** is installed as a Windows service, the _OpenTelemetry Collector_ attempts to run as a Windows service and may fail to start if it cannot connect to the Windows service controller. To address this potential problem, the **Hardware Sentry Agent** forces the _OpenTelemetry Collector_ to be started as if it were running in an interactive terminal by setting the `NO_WINDOWS_SERVICE` environment variable to `1`.

To define new [environment variables](https://opentelemetry.io/docs/collector/configuration/#configuration-environment-variables) to be used by the _OpenTelemetry Collector_, such as `HTTPS_PROXY`, add new entries to the `otelCollector:environment` section:

```yaml
otelCollector:
  environment:
    HTTPS_PROXY: https://my-proxy.domain.internal.net
    NO_WINDOWS_SERVICE: 1

hosts: # ...
```

#### Process output

By default, the **Hardware Sentry Agent** listens to the _OpenTelemetry Collector_ standard output (STDOUT) and standard error (STDERR) and streams every output line to the `logs/otelcol-\${timestamp}.log` file when the logger is enabled.

To print the _OpenTelemetry Collector_ output to the console, set the `output` property to `console` under the `otelCollector` section:

```yaml
otelCollector:
  output: console  # Default: log

hosts: # ...
```

To disable the _OpenTelemetry Collector_ output processor, set the `output` property to `silent` under the `otelCollector` section:

```yaml
otelCollector:
  output: silent   # Default: log

hosts: # ...
```

#### Working directory

You may use relative paths in the `otel/otel-config.yaml` file and these paths are relative to the working directory of the OpenTelemetry Collector. If the working directory is incorrect, it can prevent the _OpenTelemetry Collector_ from starting.

By default, the working directory is set to `hws-otel-collector/otel`. In heavily customized setups, you may need to set the right working directory for the _OpenTelemetry Collector_ process.

**`otel/otel-config.yaml` example:**

```yaml
receivers:
  otlp:
    protocols:
      grpc:
        tls:
          cert_file: ../security/otel.crt  # This is the relative path to the certificate file, assuming that the working directory is 'hws-otel-collector/otel'

```

To configure the working directory of the _OpenTelemetry Collector_, update the `workingDir` attribute under the `otelCollector` section in `config/hws-config.yaml`:

```yaml
otelCollector:
  workingDir: /opt/hws-otel-collector

hosts: # ...
```

### Security settings

#### Trusted certificates file

A TLS handshake takes place when the **Hardware Sentry Agent**'s `OTLP Exporter` instantiates a communication with the `OTLP gRPC Receiver`. By default, the internal `OTLP Exporter` client is configured to trust the `OTLP gRPC Receiver`'s certificate `security/otel.crt`.

If you generate a new server's certificate for the [OTLP gRPC Receiver](configure-otel.md#OTLP_gRPC), you must configure the `trustedCertificatesFile` parameter under the `exporter:otlp` section:

```yaml
exporter:
  otlp:
    trustedCertificatesFile: security/new-server-cert.crt

hosts: # ...
```

The file should be stored in the `security` folder and should contain one or more X.509 certificates in PEM format.