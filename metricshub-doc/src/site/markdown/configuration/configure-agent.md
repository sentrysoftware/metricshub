keywords: agent, configuration, protocols, snmp, wbem, wmi, ipmi, ssh, http, os command, winrm, sites
description: How to configure MetricsHub Agent to scrape hosts with various protocols.

# Configure the MetricsHub Agent

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

The **MetricsHub Agent** collects the hardware health of the monitored systems and pushes the collected data to the OTLP receiver. **${solutionName}** then processes the hardware observability and sustainability metrics and exposes them in the backend platform of your choice (Datadog, BMC Helix, Prometheus, Grafana, etc.).

To ensure this process runs smoothly, you need to configure a few settings in the `config/metricshub-config.yaml` file to allow **${solutionName}** to:

* identify which site is monitored with this agent
* calculate the electricity costs and the carbon footprint of this site
* monitor the systems in this site.

Note that all changes made to the  `config/metricshub.yaml` file are taken into account immediately. There is therefore no need to restart **${solutionName}**.

> **Important**: We recommend using an editor supporting the [Schemastore](https://www.schemastore.org/json#editors) to edit **${solutionName}**'s configuration YAML files (Example: [Visual Studio Code](https://code.visualstudio.com/download) and [vscode.dev](https://vscode.dev), with [RedHat's YAML extension](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-yaml)).

## Configure a site

A site represents the data center or the server room in which all the systems to be monitored are located. Configure your site in the `extraLabels` section of the `config/metricshub.yaml` file as shown in the example below:

```yaml
extraLabels:
  site: boston 
```

## Configure the sustainability settings

To obtain the electricity costs and carbon footprint of your site, configure the `extraMetrics` section of the `config/metricshub-config.yaml` file as follows:

```yaml 
extraMetrics:
  hw.site.carbon_intensity: 350 # in g/kWh
  hw.site.electricity_cost: 0.12 # in $/kWh
  hw.site.pue: 1.8
```

where:
* `hw.site.carbon_intensity` is the **carbon intensity in grams per kiloWatthour**. This information is required to calculate the carbon emissions of your site. The carbon intensity corresponds to the amount of CO₂ emissions produced per kWh of electricity and varies depending on the country and the region where the data center is located. See the [electricityMap Web site](https://app.electricitymap.org/map) for reference. 
* `hw.site.electricity_cost` is the **electricity price in the currency of your choice per kiloWattHour**. This information is required to calculate the energy cost of your site. Refer to your energy contract to know the tariff by kilowatt per hour charged by your supplier or refer to the [GlobalPetrolPrices Web site](https://www.globalpetrolprices.com/electricity_prices/). Make sure to always use the same currency for all instances of MetricsHub on all sites to allow cost aggregation in your dashboards that cover multiple sites.
* `hw.site.pue` is the **Power Usage Effectiveness (PUE)** of your site. By default, sites are set with a PUE of 1.8, which is the average value for typical data centers.

## Configure the monitored hosts

To collect metrics from your hosts, you must provide the following information in the `config/metricshub-config.yaml` file:

* the hostname of the host to be monitored
* its type
* the protocol to be used.

You can either configure your hosts individually or several at a times if they share the same characteristics (device type, protocols, credentials, etc.).

### Monitored hosts

#### Single host

Systems to monitor are defined under `hosts` with the below syntax:

```yaml
hosts:

- host:
    hostname: <hostname>
    type: <host-type>
  <protocol-configuration>
```

where:

* `<hostname>` is the name of the host, or its IP address.
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
  
* `<protocol-configuration>` is the protocol(s) **${solutionName}** will use to communicate with the hosts: `http`, `ipmi`, `oscommand`, `ssh`, `snmp`, `wmi`, `wbem` or `winrm`. Refer to [Protocols and credentials](#protocol) for more details.

#### Multiple hosts

You can group hosts that share the same characteristics (device type, protocols, credentials, etc.) using one of the below syntax:

```yaml
hosts:

- hostGroup:
    hostnames: [ <hostname1>,<hostname2>, etc.]
    type: <host-type>
  <protocol-configuration>

```

where:

* `<hostname1>,<hostname2>, etc.` is a comma-delimited list of hosts to be monitored. Provide their hostname or IP address.
* `<host-type>` is the type of the host to be monitored.
* `<protocol-configuration>` is the protocol(s) **${solutionName}** will use to communicate with the hosts: `http`, `ipmi`, `oscommand`, `ssh`, `snmp`, `wmi`, `wbem` or `winrm`. Refer to [Protocols and credentials](#protocol) for more details.

or

```yaml
hosts:
- hostGroup:
    type: <host-type>
    hostnames:
      <hostname>:
        extraLabels:
          host.name: server-01.local.net
          host.id: my-server-01-id
    # <hostname>:
    #   extraLabels:
  <protocol-configuration>
```

where:

* `<hostname>` is the name of the host, or its IP address. Using this format, you can provide `extraLabels` for each configured host.
* `<host-type>` is the type of the host to be monitored.
* `<protocol-configuration>` is the protocol(s) **${solutionName}** will use to communicate with the hosts: `http`, `ipmi`, `oscommand`, `ssh`, `snmp`, `wmi`, `wbem` or `winrm`. Refer to [Protocols and credentials](#protocol) for more details.

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
| timeout  | How long until the HTTP request times out (Default: 60s).  |

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
      timeout: 60
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
| vcenter   | vCenter hostname providing the authentication ticket, if applicable.                           |

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
      authentications: [ntlm]
```

## Additional settings (Optional)

### Alert settings

#### Disabling alerts (Not Recommended)

To disable **${solutionName}**'s alerts:

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

#### Hardware problem template

When detecting a hardware problem, **${solutionName}** triggers an alert as OpenTelemetry log. The alert body is built from the following template:

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


### Authentication settings

#### Basic authentication header

The **MetricsHub Agent**'s internal `OTLP Exporter` authenticates itself with the _OpenTelemetry Collector_'s [OTLP gRPC Receiver](configure-otel.md#OTLP_gRPC) by including the HTTP `Authorization` request header with the credentials. A predefined *Basic Authentication Header* value is stored internally and included in each request when sending telemetry data.

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

The **MetricsHub Agent**'s internal `OTLP Exporter` pushes telemetry [signals](https://opentelemetry.io/docs/concepts/signals/) to the [`OTLP Receiver`](https://github.com/open-telemetry/opentelemetry-collector/tree/main/receiver/otlpreceiver) through [gRPC](https://grpc.io/) on port **TCP/4317**.

By default, the internal `OTLP Exporter` is configured to push data to the `OTLP Receiver` endpoint `https://localhost:4317`.

To override the OTLP endpoint, configure the `endpoint` property under the `exporter:otlp` section:

```yaml
exporter:
  otlp:
    endpoint: https://my-host:4317

hosts: #...
```

### Monitoring settings

#### Collect period

By default, **${solutionName}** collects metrics from the monitored hosts every minute. To change the default collect period:

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

**${solutionName}** comes with the *Hardware Connector Library*, a library that consists of hundreds of hardware connectors that describe how to discover hardware components and detect failures. When running **${solutionName}**, the connectors are automatically selected based on the device type provided and the enabled protocols. You can however indicate to **${solutionName}** which connectors should be used or excluded.

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
      type: oob
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
$ metricshub -l
```

For more information about the `metricshub` command, refer to [MetricsHub CLI (metricshub)](../troubleshooting/cli.md)

#### Discovery cycle

**${solutionName}** periodically performs discoveries to detect new components in your monitored environment. By default, **${solutionName}** runs a discovery after 30 collects. To change this default discovery cycle:

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

Add labels in the `extraLabels` section to override the data collected by the **MetricsHub Agent** or add additional attributes to the [Host Resource](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/host.md). These attributes are added to each metric of that *Resource* when exported to time series platforms like Prometheus. 

In the example below, we override the `host.name` attribute resolved by **${solutionName}** with `host01.internal.domain.net` and indicate that it is the `Jenkins` app:

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

In the example below, we configure several hosts which share the same characteristics (`hostGroup`) and override the `host.name` attributes resolved by **${solutionName}** with `server-01.local.net` and `server-02.local.net` and indicate that these hosts are `Jenkins` apps:

```yaml
hosts:
- hostGroup:
    type: <host-type>
    hostnames:
      server-01:
        extraLabels:
          host.name: server-01.local.net
          host.id: my-server-01-id
          app: Jenkins
      server-02:
        extraLabels:
          host.name: server-02.local.net
          host.id: my-server-02-id
          app: Jenkins
```

#### Hostname resolution

By default, **${solutionName}** resolves the `hostname` of the host to a Fully Qualified Domain Name (FQDN) and displays this value in the [Host Resource](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/host.md) attribute `host.name`. To display the configured hostname instead, set `resolveHostnameToFqdn` to `false`:

```yaml
resolveHostnameToFqdn: false

hosts:

- host:
    hostname: host01
    type: Linux
```

#### Job pool size

By default, **${solutionName}** runs up to 20 discovery and collect jobs in parallel. To increase or decrease the number of jobs **${solutionName}** can run simultaneously,  add the `jobPoolSize` parameter just before the `hosts` section:

```yaml
jobPoolSize: 20

hosts: # ...
```

and indicate a number of jobs.

> **Warning**: Running too many jobs in parallel can lead to an OutOfMemory error.

#### Sequential mode

By default, **${solutionName}** sends the queries to the host in parallel. Although the parallel mode is faster than the sequential one, too many requests at the same time can lead to the failure of the targeted system.

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

#### Timeout, duration and period format

Timeouts, durations and periods are specified with the below format:

| Unit | Description                     | Examples         |
| ---- | ------------------------------- | ---------------- |
| s    | seconds                         | 120s             |
| m    | minutes                         | 90m, 1m15s       |
| h    | hours                           | 1h, 1h30m        |
| d    | days (based on a 24-hour day)   | 1d               |

### OpenTelemetry Collector process settings

> **Note**: These settings should not be changed unless specifically required.

The **MetricsHub Agent** launches the _OpenTelemetry Collector_ as a child process by running the `otel/otelcol-contrib` executable which reads the `otel/otel-config.yaml` file to start its internal components.

To customize the way the _OpenTelemetry Collector_ process is started, update the `otelCollector` section in `config/metricshub-config.yaml`:

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

#### Command line

By default, the **MetricsHub Agent** launches the _OpenTelemetry Collector_ process using the following command line: `otel/otelcol-contrib --config otel/otel-config.yaml --feature-gates=pkg.translator.prometheus.NormalizeName`.

If you want to run your own distribution of the _OpenTelemetry Collector_ or update the default program's arguments such as the `--feature-gates` flag, you need to override the _OpenTelemetry Collector_ default command line by setting the `commandLine` property under the `otelCollector` section:

```yaml
otelCollector:
  commandLine: 
    - /opt/metricshub/otel/my-otelcol
    - --config
    - /opt/metricshub/otel/my-otel-config.yaml
    - --feature-gates=pkg.translator.prometheus.NormalizeName

hosts: # ...
```

#### Disabling the collector (Not recommended)

In some cases, you might want the **MetricsHub Agent** to send OpenTelemetry signals directly to an existing _OpenTelemetry Collector_ running the `gRPC OTLP Receiver`, in which case, running a local _OpenTelemetry Collector_ is unnecessary.

To disable the _OpenTelemetry Collector_, set the `disabled` property to `true` under the `otelCollector` section:

```yaml
otelCollector:
  disabled: true

hosts: # ...
```

#### Environment

When **${solutionName}** is installed as a Windows service, the _OpenTelemetry Collector_ may fail to start if it cannot connect to the Windows service controller. To address this issue, you can set the `NO_WINDOWS_SERVICE` environment variable to `1` to force the _OpenTelemetry Collector_ to be started as if it were running in an interactive terminal.

You can set additional [environment variables](https://opentelemetry.io/docs/collector/configuration/#configuration-environment-variables) to be used by the _OpenTelemetry Collector_ in the `otelCollector:environment` section (e.g.: HTTPS_PROXY):

```yaml
otelCollector:
  environment:
    HTTPS_PROXY: https://my-proxy.domain.internal.net
    NO_WINDOWS_SERVICE: 1

hosts: # ...
```

#### Process output

By default, the **MetricsHub Agent** listens to the _OpenTelemetry Collector_ standard output (STDOUT) and standard error (STDERR) and streams each output line to the `logs/otelcol-<timestamp>.log` file when the logger is enabled.

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

By default, the _OpenTelemetry Collector_ working directory is set to `metricshub/otel`. If your working directory is different (typically in heavily customized setups), add the `workingDir` attribute under the `otelCollector` section in `config/metricshub-config.yaml`:

```yaml
otelCollector:
  workingDir: /opt/metricshub/otel

hosts: # ...
```

> **Important**: The _OpenTelemetry Collector_ might not start if the value set for the `workingDir` attribute is not correct, more especially if the `otel/otel-config.yaml` file uses relative paths.

### Security settings

#### Trusted certificates file

A TLS handshake takes place when the **MetricsHub Agent**'s `OTLP Exporter` instantiates a communication with the `OTLP gRPC Receiver`. By default, the internal `OTLP Exporter` client is configured to trust the `OTLP gRPC Receiver`'s certificate `security/otel.crt`.

If you generate a new server's certificate for the [OTLP gRPC Receiver](configure-otel.md#OTLP_gRPC), you must configure the `trustedCertificatesFile` parameter under the `exporter:otlp` section:

```yaml
exporter:
  otlp:
    trustedCertificatesFile: /opt/metricshub/security/new-server-cert.crt

hosts: # ...
```

The file should be stored in the `security` folder and should contain one or more X.509 certificates in PEM format.