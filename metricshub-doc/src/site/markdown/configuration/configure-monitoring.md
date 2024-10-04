keywords: agent, configuration, protocols, snmp, wbem, wmi, ping, ipmi, ssh, http, os command, winrm, sites
description: How to configure the MetricsHub Agent to collect metrics from a variety of resources with various protocols.

# Monitoring Configuration

<!-- MACRO{toc|fromDepth=1|toDepth=1|id=toc} -->

**MetricsHub** extracts metrics from any resource configured in the `config/metricshub.yaml` file located under:

> * `C:\ProgramData\MetricsHub\config` on Windows systems
> * `./metricshub/lib/config` on Linux systems.

> **Important**: We recommend using an editor supporting the [Schemastore](https://www.schemastore.org/json#editors) to edit **MetricsHub**'s configuration YAML files (Example: [Visual Studio Code](https://code.visualstudio.com/download) and [vscode.dev](https://vscode.dev), with [RedHat's YAML extension](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-yaml)).

## Configure resources

The structure of the `config/metricshub.yaml` file allows you to organize and manage your resources methodically. Refer to the sections below to learn how to configure your resources effectively.

### General structure

The `config/metricshub.yaml` file is organized in a hierarchical manner to facilitate the management of various resources:

```yaml
resourceGroups:
  <resource-group-name>:
    attributes:
      site: <site-name>
    resources:
      <resource-id>:
        attributes:
          host.name: <hostname>
          host.type: <type>
        <protocol-configuration>
```

where

* `resourceGroups` is the top-level grouping for all resource groups
  * `<resource-group-name>` is a container that holds the site to be monitored
    * `site` is an attribute to specify where resources are hosted. Replace  `<site-name>`with a unique site name. It can either be a logical or a physical location (a data center or server room).
  * `resources` is a container that holds the resources to be monitored within the resource group
    * `<resource-id>` is the unique ID of your resource. It can for example be the ID of a host, an application, or a service
      * `host.name` is an attribute to specify the hostname or IP address of the resource. Replace `<hostname>` with the actual hostname or IP address of the resource. Use a comma-delimited list to specify several resources (`<hostname1>,<hostname2>, etc.`).
      * `host.type`  is an attribute to specify the type of resource to be monitored. Replace `<type>` with one of the possible values:
        * `win` for Microsoft Windows systems
        * `linux` for Linux systems
        * `network` for network devices
        * `oob` for Out-of-band management cards
        * `storage` for storage systems
        * `aix` for IBM AIX systems
        * `hpux` for HP UX systems
        * `solaris` for Oracle Solaris systems
        * `tru64` for HP Tru64 systems
        * `vms` for HP Open VMS systems.

    * `<protocol-configuration>` is the protocol(s) **MetricsHub** will use to communicate with the resources: `http`, `ipmi`, `oscommand`, `ping`, `ssh`, `snmp`, `wmi`, `wbem` or `winrm`. Refer to [Protocols and Credentials](./configure-monitoring.html#protocols-and-credentials) for more details.

> Note: You can use the `${esc.d}{env::ENV_VARIABLE_NAME}` syntax in the `config/metricshub.yaml` file to call your environment variables.


### Highly distributed infrastructure

For infrastructures with multiple distributed locations, each site can be configured as a separate `resource group` containing the different `resources` to be monitored as follows:

```yaml
resourceGroups:
  <resource-group-name>:
    attributes:
      site: <site-name>
    resources:
      <resource-id>:
        attributes:
          host.name: <hostname>
          host.type: <type>
        <protocol-configuration>
```

**Example:**

```yaml
resourceGroups:
  boston:
    attributes:
      site: boston
    resources:
      myBostonHost1:
        attributes:
          host.name: my-boston-host-01
          host.type: storage
        <protocol-configuration>
      myBostonHost2:
        attributes:
          host.name: my-boston-host-02
          host.type: storage
        <protocol-configuration>
  chicago:
    attributes:
      site: chicago
    resources:
      myChicagoHost1:
        attributes:
          host.name: my-chicago-host-01
          host.type: storage
        <protocol-configuration>
      myChicagoHost2:
        attributes:
          host.name: my-chicago-host-02
          host.type: storage
        <protocol-configuration>
```

> Note: Refer to the [sustainability metrics page](../guides/configure-sustainability-metrics.md#example-for-distributed-infrastructure) to configure MetricsHub for sustainability metrics reporting.

### Centralized infrastructure

For centralized infrastructures, resources can be configured directly under the `resources` section located at the top of the `config/metricshub.yaml` file, without `resourceGroups`:

```yaml
attribute:
  site: <central-site>

resources:
  <resource-id>:
    attributes:
      host.name: <hostname>
      host.type: <type>
    <protocol-configuration>
```

> Note: Refer to the [sustainability metrics page](../guides/configure-sustainability-metrics.md#example-for-centralized-infrastructure) to configure MetricsHub for sustainability metrics reporting.

### Unique vs. shared characteristics

#### Unique characteristics

If each resource has unique characteristics, use the following syntax for individual configuration:

```yaml
resources:
  <resource-id>:
    attributes:
      host.name: <hostname>
      host.type: <type>
    <protocol-configuration>
```

#### Shared characteristics

If multiple resources share the same characteristics, such as device type, protocols, and credentials, they can be grouped together under a single configuration:

```yaml
resources:
  <resource-id>:
    attributes:
      host.names: [<hostname1>, <hostname2>, etc.]
      host.type: <type>
    <protocol-configuration>
```

### Protocols and credentials

#### HTTP

Use the parameters below to configure the HTTP protocol:

| Parameter  | Description                                                                                       |
|------------|-------------------------------------------------------------------------------------------------- |
| http       | Protocol used to access the host.                                                                 |
| hostname   | The name or IP address of the resource. If not specified, the `host.name` attribute will be used. |
| port       | The HTTPS port number used to perform HTTP requests (Default: 443).                               |
| username   | Name used to establish the connection with the host via the HTTP protocol.                        |
| password   | Password used to establish the connection with the host via the HTTP protocol.                    |
| timeout    | How long until the HTTP request times out (Default: 60s).                                         |

**Example**

```yaml
resourceGroups:
  boston:
    attributes:
      site: boston
    resources:
      myHost1:
        attributes:
          host.name: my-host-01
          host.type: storage
        protocols:
          http:
            https: true
            port: 443
            username: myusername
            password: mypwd
            timeout: 60
```

#### ICMP Ping 

Use the parameters below to configure the ICMP ping protocol:

| Parameter       | Description                                                                                       |
| --------------- | ------------------------------------------------------------------------------------------------- |
| hostname        | The name or IP address of the resource. If not specified, the `host.name` attribute will be used. |
| ping            | Protocol used to test the host reachability through ICMP.                                         |
| timeout         | How long until the ping command times out (Default: 5s).                                          |

**Example**

```yaml
resourceGroups:
  boston:
    attributes:
      site: boston
    resources:
      myHost1:
        attributes:
          host.name: my-host-01
          host.type: linux
        protocols:
          ping:
            timeout: 10s
```

#### IPMI

Use the parameters below to configure the IPMI protocol:

| Parameter | Description                                                                                       |
| --------- | ------------------------------------------------------------------------------------------------- |
| ipmi      | Protocol used to access the host.                                                                 |
| hostname  | The name or IP address of the resource. If not specified, the `host.name` attribute will be used. |
| username  | Name used to establish the connection with the host via the IPMI protocol.                        |
| password  | Password used to establish the connection with the host via the IPMI protocol.                    |

**Example**

```yaml
resourceGroups:
  boston:
    attributes:
      site: boston
    resources:
      myHost1:
        attributes:
          host.name: my-host-01
          host.type: oob
        protocols:
          ipmi:
            username: myusername
            password: mypwd
```

#### OS commands

Use the parameters below to configure OS Commands that are executed locally:

| Parameter       | Description                                                                                       |
| --------------- | ------------------------------------------------------------------------------------------------- |
| osCommand       | Protocol used to access the host.                                                                 |
| hostname        | The name or IP address of the resource. If not specified, the `host.name` attribute will be used. |
| timeout         | How long until the local OS Commands time out (Default: 120s).                                    |
| useSudo         | Whether sudo is used or not for the local OS Command: true or false (Default: false).             |
| useSudoCommands | List of commands for which sudo is required.                                                      |
| sudoCommand     | Sudo command to be used (Default: sudo).                                                          |

**Example**

```yaml
resourceGroups:
  boston:
    attributes:
      site: boston
    resources:
      myHost1:
        attributes:
          host.name: my-host-01
          host.type: linux
        protocols:
          osCommand:
            timeout: 120
            useSudo: true
            useSudoCommands: [ cmd1, cmd2 ]
            sudoCommand: sudo
```

#### SSH

Use the parameters below to configure the SSH protocol:

| Parameter       | Description                                                                                       |
| --------------- | ------------------------------------------------------------------------------------------------- |
| ssh             | Protocol used to access the host.                                                                 |
| hostname        | The name or IP address of the resource. If not specified, the `host.name` attribute will be used. |
| timeout         | How long until the command times out (Default: 120s).                                             |
| port            | The SSH port number to use for the SSH connection (Default: 22).                                  |
| useSudo         | Whether sudo is used or not for the SSH Command (true or false).                                  |
| useSudoCommands | List of commands for which sudo is required.                                                      |
| sudoCommand     | Sudo command to be used (Default: sudo).                                                          |
| username        | Name to use for performing the SSH query.                                                         |
| password        | Password to use for performing the SSH query.                                                     |
| privateKey      | Private Key File to use to establish the connection to the host through the SSH protocol.         |

**Example**

```yaml
resourceGroups:
  boston:
    attributes:
      site: boston
    resources:
      myHost1:
        attributes:
          host.name: my-host-01
          host.type: linux
        protocols:
          ssh:
            timeout: 120
            port: 22
            useSudo: true
            useSudoCommands: [ cmd1, cmd2 ]
            sudoCommand: sudo
            username: myusername
            password: mypwd
            privateKey: /tmp/ssh-key.txt

```

#### SNMP

Use the parameters below to configure the SNMP protocol:

| Parameter        | Description                                                                                       |
| ---------------- | ------------------------------------------------------------------------------------------------- |
| snmp             | Protocol used to access the host.                                                                 |
| hostname         | The name or IP address of the resource. If not specified, the `host.name` attribute will be used. |
| version          | The version of the SNMP protocol (v1, v2c).                                                       |
| community        | The SNMP Community string to use to perform SNMP v1 queries (Default: public).                    |
| port             | The SNMP port number used to perform SNMP queries (Default: 161).                                 |
| timeout          | How long until the SNMP request times out (Default: 120s).                                        |

**Example**

```yaml
resourceGroups:
  boston:
    attributes:
      site: boston
    resources:
      myHost1:
        attributes:
          host.name: my-host-01
          host.type: linux
        protocols:
          snmp:
            version: v1
            community: public
            port: 161
            timeout: 120s

      myHost2:
        attributes:
          host.name: my-host-02
          host.type: linux
        protocols:
          snmp:
            version: v2c
            community: public
            port: 161
            timeout: 120s
```

#### SNMP version 3

Use the parameters below to configure the SNMP version 3 protocol:

| Parameter        | Description                                                                                          |
| ---------------- | ---------------------------------------------------------------------------------------------------- |
| snmpv3           | Protocol used to access the host using SNMP version 3.                                               |
| hostname         | The name or IP address of the resource. If not specified, the `host.name` attribute will be used.    |
| timeout          | How long until the SNMP request times out (Default: 120s).                                           |
| port             | The SNMP port number used to perform SNMP version 3 queries (Default: 161).                          |
| contextName      | The name of the SNMP version 3 context, used to identify the collection of management information.   |
| authType         | The SNMP version 3 authentication protocol (MD5, SHA or NoAuth) to ensure message authenticity.      |
| privacy          | The SNMP version 3 privacy protocol (DES, AES or NONE) used to encrypt messages for confidentiality. |
| username         | The username used for SNMP version 3 authentication.                                                 |
| privacyPassword  | The password used to encrypt SNMP version 3 messages for confidentiality.                            |
| password         | The password used for SNMP version 3 authentication.                                                 |
| retryIntervals   | The intervals (in milliseconds) between SNMP request retries.                                        |

**Example**

```yaml
resourceGroups:
  boston:
    attributes:
      site: boston
    resources:
      myHost3:
        attributes:
          host.name: my-host-03
          host.type: linux
        protocols:
          snmpv3:
            version: 3
            port: 161
            timeout: 120s
            contextName: myContext
            authType: SHA
            privacy: AES
            username: myUser
            privacyPassword: myPrivacyPassword
            password: myAuthPassword 
```

#### WBEM

Use the parameters below to configure the WBEM protocol:

| Parameter | Description                                                                                       |
| --------- | ------------------------------------------------------------------------------------------------- |
| wbem      | Protocol used to access the host.                                                                 |
| hostname  | The name or IP address of the resource. If not specified, the `host.name` attribute will be used. |
| protocol  | The protocol used to access the host.                                                             |
| port      | The HTTPS port number used to perform WBEM queries (Default: 5989 for HTTPS or 5988 for HTTP).    |
| timeout   | How long until the WBEM request times out (Default: 120s).                                        |
| username  | Name used to establish the connection with the host via the WBEM protocol.                        |
| password  | Password used to establish the connection with the host via the WBEM protocol.                    |
| vcenter   | vCenter hostname providing the authentication ticket, if applicable.                              |

**Example**

```yaml
resourceGroups:
  boston:
    attributes:
      site: boston
    resources:
      myHost1:
        attributes:
          host.name: my-host-01
          host.type: storage
        protocols:
          wbem:
            protocol: https
            port: 5989
            timeout: 120s
            username: myusername
            password: mypwd
```

#### WMI

Use the parameters below to configure the WMI protocol:

| Parameter | Description                                                                                       |
| --------- | ------------------------------------------------------------------------------------------------- |
| wmi       | Protocol used to access the host.                                                                 |
| hostname  | The name or IP address of the resource. If not specified, the `host.name` attribute will be used. |
| timeout   | How long until the WMI request times out (Default: 120s).                                         |
| username  | Name used to establish the connection with the host via the WMI protocol.                         |
| password  | Password used to establish the connection with the host via the WMI protocol.                     |

**Example**

```yaml
resourceGroups:
  boston:
    attributes:
      site: boston
    resources:
      myHost1:
        attributes:
          host.name: my-host-01
          host.type: win
        protocols:
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
| hostname        | The name or IP address of the resource. If not specified, the `host.name` attribute will be used.    |
| timeout         | How long until the WinRM request times out (Default: 120s).                                          |
| username        | Name used to establish the connection with the host via the WinRM protocol.                          |
| password        | Password used to establish the connection with the host via the WinRM protocol.                      |
| protocol        | The protocol used to access the host: HTTP or HTTPS (Default: HTTP).                                 |
| port            | The port number used to perform WQL queries and commands (Default: 5985 for HTTP or 5986 for HTTPS). |
| authentications | Ordered list of authentication schemes: NTLM, KERBEROS (Default: NTLM).                              |

**Example**

```yaml
resourceGroups:
  boston:
    attributes:
      site: boston
    resources:
      myHost1:
        attributes:
          host.name: my-host-01
          host.type: win
        protocols:
          winrm:
            protocol: http
            port: 5985
            username: myusername
            password: mypwd
            timeout: 120s
            authentications: [ntlm]
```

## (Optional) Customize the hostname

By default, the `host.name` attribute is used for both the hostname or IP address of the resource and as the hostname of each OpenTelemetry metric attached to the host resource.

If the `hostname` parameter is specified in the protocol configuration, it overrides the `host.name` attribute for client requests. In this case, the `host.name` will only be used as a metric attribute.

### Example
```yaml
resources:
  myHost1:
    attributes:
      # `custom-hostname` will be the hostname value in the collected metrics.
      host.name: custom-hostname
      host.type: linux
    protocols:
      snmp:
        # my-host-01 will be used to send requests to the host.
        hostname: my-host-01
        version: v1
        community: public
        port: 161
        timeout: 1m
```
In the example above:
* `my-host-01` will be used to send requests to the host
* `custom-hostname` will be used as the hostname in the metrics.

## (Optional) Customize resource monitoring

If the connectors included in **MetricsHub** do not collect the metrics you need, you can configure one or several monitors to obtain this data from your resource and specify its corresponding attributes and metrics in **MetricsHub**.

A monitor defines how **MetricsHub** collects and processes data for the resource. For each monitor, you must provide the following information:

* its name
* the type of job it performs (e.g., `simple` for straightforward monitoring tasks)
* the data sources from which metrics are collected
* how the collected metrics are mapped to **MetricsHub**'s monitoring model.

### Configuration

Follow the structure below to declare your monitor:

```yaml
<resource-group>:
  <resource-key>:
    attributes:
      # <attributes...>
    protocols:
      # <credentials...>
    monitors:
      <monitor-name>:
        <job>: # Job type, e.g., "simple"
          sources:
            <source-name>:
              # <source-content>
          mapping:
            source: <mapping-source-reference>
            attributes:
              # <attributes-mapping...>
            metrics:
              # <metrics-mapping...>
```

Refer to [Monitors](https://sentrysoftware.org/metricshub-community-connectors/develop/monitors.html) for more information on how to configure custom resource monitoring.

### Example: Monitoring a Grafana Service

In the example below, we configured a monitor for a Grafana service. This monitor collects data from the Grafana health API and maps the response to the most relevant attributes and metrics in **MetricsHub**.

```yaml
service-group:  
  grafana-service:
    attributes:
      service.name: Grafana
      host.name: hws-demo.sentrysoftware.com
    protocols:
      http:
        https: true
        port: 443
    monitors:
      grafana:
        simple: # "simple" job type. Creates monitors and collects associated metrics. 
          sources:
            grafanaHealth:
              type: http
              path: /api/health
              method: get
              header: "Accept: application/json"
              computes:
              - type: json2Csv
                entryKey: /
                properties: commit;database;version
                separator: ;
              - type: translate
                column: 3
                translationTable:
                  ok: 1
                  default: 0
          mapping:
            source: ${esc.d}{source::grafanaHealth}
            attributes:
              id: $2
              service.instance.id: $2
              service.version: $4
            metrics:
              grafana.db.state: $3
```

## (Optional) Additional settings

### Basic Authentication settings

#### Enterprise Edition authentication

In the Enterprise Edition, the **MetricsHub**'s internal `OTLP Exporter` authenticates itself with the _OpenTelemetry Collector_'s [OTLP gRPC Receiver](send-telemetry.md#otlp-grpc) by including the HTTP `Authorization` request header with the credentials. 

These settings are already configured in the `config/metricshub.yaml` file of **MetricsHub Enterprise Edition**. Changing them is **not recommended** unless you are familiar with managing communication between the **MetricsHub** `OTLP Exporter` and the _OpenTelemetry Collector_'s `OTLP Receiver`.

To override the default value of the *Basic Authentication Header*, configure the `otel.exporter.otlp.metrics.headers` and `otel.exporter.otlp.logs.headers` parameters under the `otel` section:

```yaml
# Internal OpenTelemetry SDK configuration
otel:
  # OpenTelemetry SDK Autoconfigure properties
  # https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure
  # MetricsHub Default configuration
  otel.metrics.exporter: otlp
  otel.exporter.otlp.metrics.endpoint: https://localhost:4317
  otel.exporter.otlp.metrics.protocol: grpc
  otel.exporter.otlp.metrics.headers: Authorization=Basic <base64-username-password>
  otel.exporter.otlp.logs.headers: Authorization=Basic <base64-username-password>
resourceGroups: # ...
```

where `<base64-username-password>` credentials are built by first joining your username and password with a colon (`myUsername:myPassword`) and then encoding the value in `base64`.

> **Warning**: If you update the *Basic Authentication Header*, you must generate a new `.htpasswd` file for the [OpenTelemetry Collector Basic Authenticator](send-telemetry.md#basic-authenticator).

#### Community Edition authentication

If your `OTLP Receiver` requires authentication headers, configure the `otel.exporter.otlp.metrics.headers` and `otel.exporter.otlp.logs.headers` parameters under the `otel` section:

```yaml
otel:
  otel.exporter.otlp.metrics.headers: <custom-header1>
  otel.exporter.otlp.logs.headers: <custom-header2>

resourceGroups: # ...
```

### Monitoring settings

#### Collect period

By default, **MetricsHub** collects metrics from the monitored resources every minute. To change the default collect period:

* For all your resources, add the `collectPeriod` parameter just before the `resourceGroups` section:

    ```yaml
    collectPeriod: 2m

    resourceGroups: # ...
    ```

* For a specific resource, add the `collectPeriod` parameter at the resource level. In the example below, we set the `collectPeriod` to `1m30s` for `myHost1`:

    ```yaml
    resourceGroups:
      boston:
        attributes:
          site: boston
        resources:
          myHost1:
            attributes:
              host.name: my-host-01
              host.type: linux
            protocols:
              snmp:
                version: v1
                community: public
                port: 161
                timeout: 120s
            collectPeriod: 1m30s # Customized
    ```

> **Warning**: Collecting metrics too frequently can cause CPU-intensive workloads.

#### Connectors

When running **MetricsHub**, the connectors are automatically selected based on the device type provided and the enabled protocols. However, you have the flexibility to specify which connectors should be utilized or omitted.

The `connectors` parameter allows you to force, select, or exclude specific connectors. Connector names or category tags should be separated by commas, as illustrated in the example below:

```yaml
resourceGroups:
  boston:
    attributes:
      site: boston
    resources:
      myHost1:
        attributes:
          host.name: my-host-01
          host.type: win
        protocols:
          wmi:
            timeout: 120s
            username: myusername
            password: mypwd
        connectors: [ "#system" ]
```

* To force a connector, precede the connector identifier with a plus sign (`+`), as in `+MIB2`.
* To exclude a connector from automatic detection, precede the connector identifier with an exclamation mark (`!`), like `!MIB2`.
* To stage a connector for processing by automatic detection, configure the connector identifier, for instance, `MIB2`.
* To stage a category of connectors for processing by automatic detection, precede the category tag with a hash (`#`), such as `#hardware` or `#system`.
* To exclude a category of connectors from automatic detection, precede the category tag to be excluded with an exclamation mark and a hash sign (`!#`), such as `!#system`.

> **Notes**:
>
>* Any misspelled connector will be ignored.
>* Misspelling a category tag will prevent automatic detection from functioning due to an empty connectors staging.

##### Examples

* Example 1:

  ```yaml
  connectors: [ "#hardware" ]
  ```

 The core engine will automatically detect connectors categorized under `hardware`.

* Example 2:

  ```yaml
  connectors: [ "!#hardware", "#system" ]
  ```

  The core engine will perform automatic detection on connectors categorized under `system`, excluding those categorized under `hardware`.

* Example 3:

  ```yaml
  connectors: [ DiskPart, MIB2, "#system" ]
  ```

  The core engine will automatically detect connectors named `DiskPart`, `MIB2`, and all connectors under the `system` category.

* Example 4:

  ```yaml
  connectors: [ +DiskPart, MIB2, "#system" ]
  ```

  The core engine will force the execution of the `DiskPart` connector and then proceed with the automatic detection of `MIB2` and all connectors under the `system` category.

* Example 5:

  ```yaml
  connectors: [ DiskPart, "!#system" ]
  ```

  The core engine will perform automatic detection exclusively on the `DiskPart` connector.

* Example 6:

  ```yaml
  connectors: [ +Linux, MIB2 ]
  ```

  The core engine will force the execution of the `Linux` connector and subsequently perform automatic detection on the `MIB2` connector.

* Example 7:

  ```yaml
  connectors: [ "!Linux" ]
  ```

  The core engine will perform automatic detection on all connectors except the `Linux` connector.

* Example 8:

  ```yaml
  connectors: [ "#hardware", "!MIB2" ]
  ```

  The core engine will perform automatic detection on connectors categorized under `hardware`, excluding the `MIB2` connector.

To know which connectors are available, refer to [Connectors Directory](../metricshub-connectors-directory.html).

Otherwise, you can list the available connectors using the below command:

```shell-session
$ metricshub -l
```

For more information about the `metricshub` command, refer to [MetricsHub CLI (metricshub)](../guides/cli.md).

#### Patch Connectors

By default, **MetricsHub** loads connectors from the `connectors` subdirectory within its installation directory. However, you can extend this functionality by adding a custom directory for additional connectors. This can be done by specifying a patch directory in the `metricshub.yaml` configuration file.

To configure an additional connector directory, set the `patchDirectory` property to the path of your custom connectors directory, as shown in the example below:

```yaml

patchDirectory: /opt/patch/connectors # Replace with the path to your patch connectors directory.

loggerLevel: ...
```

#### Configure Connector Variables

In **MetricsHub**, connector variables are essential for customizing the behavior of data collection. The connector variables are configured in the `metricshub.yaml` file under the `variables` section of your configured resource. These variables are specified under the name of the connector to which they belong and contain key-value pairs. The key of each variable corresponds to a variable already configured in the connector.

* Example :

  Below is a configuration using the `WindowsProcess` connector. The `processName` variable, defined in the variables section, specifies a list of process names (msedge.exe and metricshub.exe) to monitor:

```yaml
resources:
  localhost:
    attributes:
      host.name: localhost
      host.type: windows
    protocols:
      wmi:
        timeout: 120
    variables:
      windowsProcess: # Connector ID
        processName: "('msedge.exe', 'metricshub.exe')"
```

#### Discovery cycle

**MetricsHub** periodically performs discoveries to detect new components in your monitored environment. By default, **MetricsHub** runs a discovery after 30 collects. To change this default discovery cycle:

* For all your resources, add the `discoveryCycle` just before the `resourceGroups` section:

    ```yaml
    discoveryCycle: 15

    resourceGroups: # ...
    ```

* For a specific host, add the `discoveryCycle` parameter at the resource level and indicate the number of collects after which a discovery will be performed. In the example below, we set the `discoveryCycle` to be performed after `5` collects for `myHost1`:

    ```yaml
    resourceGroups:
      boston:
        attributes:
          site: boston
        resources:
          myHost1:
            attributes:
              host.name: my-host-01
              host.type: linux
            protocols:
              snmp:
                version: v1
                community: public
                port: 161
                timeout: 120s
            discoveryCycle: 5 # Customized
    ```

> **Warning**: Running discoveries too frequently can cause CPU-intensive workloads.

#### Resource Attributes

Add labels in the `attributes` section to override the data collected by the **MetricsHub Agent** or add additional attributes to the [Host Resource](https://opentelemetry.io/docs/specs/semconv/resource/host/). These attributes are added to each metric of that *Resource* when exported to time series platforms like Prometheus.

In the example below, we added a new `app` attribute and indicated that this is the `Jenkins` app:

```yaml
resourceGroups:
  boston:
    attributes:
      site: boston
    resources:
      myHost1:
        attributes:
          host.name: my-host-01
          host.type: windows
          app: Jenkins
        protocols:
          http:
            https: true
            port: 443
            username: myusername
            password: mypwd
            timeout: 60
```

#### Hostname resolution

By default, **MetricsHub** uses the configured `host.name` value as-is to populate the [Host Resource](https://opentelemetry.io/docs/specs/semconv/resource/host/) attributes. This ensures that the `host.name` remains consistent with what is configured.

To resolve the `host.name` to its Fully Qualified Domain Name (FQDN), set the `resolveHostnameToFqdn` configuration property to `true` as shown below:

```yaml
resolveHostnameToFqdn: true

resourceGroups:
```

This ensures that each configured resource will resolve its `host.name` to FQDN.

To enable FQDN resolution for a specific resource group, set the `resolveHostnameToFqdn` property to `true` under the desired resource group configuration as shown below:

```yaml
resourceGroups:
  boston:
    resolveHostnameToFqdn: true
    attributes:
      site: boston
    resources:
      # ...
```

This ensures that all resources within the `boston` resource group will resolve their `host.name` to FQDN.

To enable FQDN resolution for an individual resource within a resource group, set the `resolveHostnameToFqdn` under the resource configuration as shown below:

```yaml
resourceGroups:
  boston:
    attributes:
      site: boston
    resources:
      my-host-01:
        resolveHostnameToFqdn: true
        attributes:
          host.name: my-host-01
          host.type: linux
```

In this case, only `my-host-01` will resolve its `host.name` to FQDN, while other resources in the `boston` group will retain their original `host.name` values.

> **Warning**: If there is an issue during the resolution, it may result in a different `host.name` value, potentially impacting metric identity.

#### Job pool size

By default, **MetricsHub** runs up to 20 discovery and collect jobs in parallel. To increase or decrease the number of jobs **MetricsHub** can run simultaneously, add the `jobPoolSize` parameter just before the `resourceGroups` section:

```yaml
jobPoolSize: 40 # Customized

resourceGroups: # ...
```

> **Warning**: Running too many jobs in parallel can lead to an OutOfMemory error.

#### Sequential mode

By default, **MetricsHub** sends the queries to the resource in parallel. Although the parallel mode is faster than the sequential one, too many requests at the same time can lead to the failure of the targeted system.

To force all the network calls to be executed in sequential order:

* For all your resources, add the `sequential` parameter before the `resourceGroups` section (**NOT RECOMMENDED**) and set it to `true`:

    ```yaml
    sequential: true

    resourceGroups: # ...
    ```

* For a specific resource, add the `sequential` parameter at the resource level and set it to `true`. In the example below, we enabled the `sequential` mode for `myHost1`

    ```yaml
    resourceGroups:
      boston:
        attributes:
          site: boston
        resources:
          myHost1:
            attributes:
              host.name: my-host-01
              host.type: linux
            protocols:
              snmp:
                version: v1
                community: public
                port: 161
                timeout: 120s
            sequential: true # Customized
    ```

> **Warning**: Sending requests in sequential mode slows down the monitoring significantly. Instead of using the sequential mode, you can increase the maximum number of allowed concurrent requests in the monitored system, if the manufacturer allows it.

#### StateSet metrics compression

By default, **MetricsHub** compresses StateSet metrics to reduce unnecessary reporting of zero values and to avoid high cardinality in time series databases. This compression can be configured at various levels: globally, per resource group, or for a specific resource.

##### Compression configuration `stateSetCompression`

This configuration controls how StateSet metrics are reported, specifically whether zero values should be suppressed or not.

- **Supported values:**
  - `none`: No compression is applied. All StateSet metrics, including zero values, are reported on every collection cycle.
  - `suppressZeros` (default): **MetricsHub** compresses StateSet metrics by reporting the zero value only the first time a state transitions to zero. Subsequent reports will include only the non-zero state values.

To configure the StateSet compression level, you can apply the `stateSetCompression` setting in the following scopes:

1. **Global configuration** (applies to all resources):

   Add `stateSetCompression` to the root of the `config/metricshub.yaml` file:

   ```yaml
   stateSetCompression: suppressZeros # set to "none" to disable the StateSet compression
   resourceGroups: ...
   ```

2. **Per resource group** (applies to all resources within a specific group):

   Add `stateSetCompression` within a specific `resourceGroup` in `config/metricshub.yaml`:

   ```yaml
   resourceGroups:
     <resource-group-name>:
       stateSetCompression: suppressZeros # set to "none" to disable the StateSet compression
       resources: ...
   ```

3. **Per resource** (applies to a specific resource):

   Add `stateSetCompression` for an individual resource in `config/metricshub.yaml`:

   ```yaml
   resourceGroups:
     <resource-group-name>:
       resources:
         <resource-id>:
           stateSetCompression: suppressZeros # set to "none" to disable the StateSet compression
   ```

##### How it works

By default, with `suppressZeros` enabled, **MetricsHub** optimizes metric reporting by suppressing repeated zero values after the initial transition. Only non-zero state metrics will continue to be reported.

**Example: Monitoring the health status of a resource**

Let’s say **MetricsHub** monitors the health status of a specific resource, which can be in one of three states: `ok`, `degraded`, or `failed`.

When compression is **disabled** (`stateSetCompression: none`), **MetricsHub** will report all states, including zeros, during each collection cycle. For example:

```yaml
hw.status{state="ok"} 0
hw.status{state="degraded"} 1
hw.status{state="failed"} 0
```

Here, the resource is in the `degraded` state, but the metrics for the `ok` and `failed` states are also reported with values of `0`. This leads to unnecessary data being sent.

When compression is **enabled** (`stateSetCompression: suppressZeros`), **MetricsHub** will only report the non-zero state, significantly reducing the amount of data collected. For the same scenario, the report would look like this:

```yaml
hw.status{state="degraded"} 1
```

In this case, only the `degraded` state is reported, and the zero values for `ok` and `failed` are suppressed after the initial state transition.

#### Timeout, duration and period format

Timeouts, durations and periods are specified with the below format:

| Unit | Description                     | Examples         |
| ---- | ------------------------------- | ---------------- |
| s    | seconds                         | 120s             |
| m    | minutes                         | 90m, 1m15s       |
| h    | hours                           | 1h, 1h30m        |
| d    | days (based on a 24-hour day)   | 1d               |