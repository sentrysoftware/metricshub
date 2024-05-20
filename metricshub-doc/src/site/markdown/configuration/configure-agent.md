keywords: agent, configuration, protocols, snmp, wbem, wmi, ipmi, ssh, http, os command, winrm, sites
description: How to configure the MetricsHub Agent to collect metrics from a variety of resources with various protocols.

# Configure the Agent

<!-- MACRO{toc|fromDepth=1|toDepth=1|id=toc} -->

**MetricsHub** extracts metrics from any configured resource and pushes the collected data to an OTLP receiver (a.k.a OTLP endpoint).

To ensure this process runs smoothly, you need to configure a few settings in the `config/metricshub.yaml`, which is stored under:

> * `C:\ProgramData\MetricsHub\config` on Windows systems
> * `./metricshub/lib/config` on Linux systems.


> **Important**: We recommend using an editor supporting the [Schemastore](https://www.schemastore.org/json#editors) to edit **MetricsHub**'s configuration YAML files (Example: [Visual Studio Code](https://code.visualstudio.com/download) and [vscode.dev](https://vscode.dev), with [RedHat's YAML extension](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-yaml)).

## Configure resources

The structure of the `config/metricshub.yaml` file allows you to organize and manage your resources in a methodical manner:

```yaml
resourceGroups:
  <resource-group-name>:
    attributes:
      site: <site-name>
```

* `resourceGroups` is the highest hierarchical level grouping of all the different resource groups
* a `resource group` is a container that holds the site to be monitored
* a `site` is the data center, the server room, or any other location hosting the resources to be monitored.

If you have:

* **a highly distributed infrastructure**, you can set each of your `sites` as a `resource group` containing the different `resources` to be monitored as follows:

    ```yaml
    resourceGroups:
      <resource-group-name>:
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

* **a centralized infrastructure**, you can configure your resource directly under the `resources` section located at the top of the `config/metricshub.yaml` file. In that case, the `resourceGroups` attribute is not required:

    ```yaml
    resources:
      <resource-id>:
        attributes:
          host.name: <hostname>
          host.type: <type>
        <protocol-configuration>
    # resourceGroups: #
    ```

If your resources have:

* **unique characteristics**, use the syntax below for each resource:

  ```yaml
  resources:
    <resource-id>:
      attributes:
        host.name: <hostname>
        host.type: <type>
      <protocol-configuration>
    ```

* share the **same characteristics** (device type, protocols, credentials, etc.), use the syntax below:

  ```yaml
  resourceGroups:
    <resource-group-name>:
      resources:
        <resource-id>:
          attributes:
            host.names: [<hostname1>,<hostname2>, etc.]
            host.type: <type>
          <protocol-configuration>
  ```

where

* `<resource-id>` is the unique id of your resource. It can for example be the id of a host, an application, or a service
* `<hostname>` is the name or IP address of the resource; `<hostname1>,<hostname2>, etc.` is a comma-delimited list of resources to be monitored. Provide their hostname or IP address.
* `<type>` is the type of the resource to be monitored. Possible values are:
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

* `<protocol-configuration>` is the protocol(s) **MetricsHub** will use to communicate with the resources: `http`, `ipmi`, `oscommand`, `ssh`, `snmp`, `wmi`, `wbem` or `winrm`. Refer to [Protocols and credentials](./configure-agent.html#protocols-and-credentials) for more details.

### Protocols and credentials

#### HTTP

Use the parameters below to configure the HTTP protocol:

| Parameter  | Description                                                                    |
|------------|--------------------------------------------------------------------------------|
| http       | Protocol used to access the host.                                              |
| port       | The HTTPS port number used to perform HTTP requests (Default: 443).            |
| username   | Name used to establish the connection with the host via the HTTP protocol.     |
| password   | Password used to establish the connection with the host via the HTTP protocol. |
| timeout    | How long until the HTTP request times out (Default: 60s).                      |

**Example**

```yaml
resourceGroups:
  boston:
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

#### IPMI

Use the parameters below to configure the IPMI protocol:

| Parameter | Description                                                                    |
| --------- | ------------------------------------------------------------------------------ |
| ipmi      | Protocol used to access the host.                                              |
| username  | Name used to establish the connection with the host via the IPMI protocol.     |
| password  | Password used to establish the connection with the host via the IPMI protocol. |

**Example**

```yaml
resourceGroups:
  boston:
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

| Parameter       | Description                                                                           |
| --------------- | ------------------------------------------------------------------------------------- |
| osCommand       | Protocol used to access the host.                                                     |
| timeout         | How long until the local OS Commands time out (Default: 120s).                        |
| useSudo         | Whether sudo is used or not for the local OS Command: true or false (Default: false). |
| useSudoCommands | List of commands for which sudo is required.                                          |
| sudoCommand     | Sudo command to be used (Default: sudo).                                              |

**Example**

```yaml
resourceGroups:
  boston:
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
resourceGroups:
  boston:
    resources:
      myHost1:
        attributes:
          host.name: my-host-01
          host.type: linux
        protocols:
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
| version          | The version of the SNMP protocol (v1, v2c).                                    |
| community        | The SNMP Community string to use to perform SNMP v1 queries (Default: public). |
| port             | The SNMP port number used to perform SNMP queries (Default: 161).              |
| timeout          | How long until the SNMP request times out (Default: 120s).                     |

**Example**

```yaml
resourceGroups:
  boston:
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

| Parameter        | Description                                                                    					|
| ---------------- | -------------------------------------------------------------------------------------------------- |
| snmpv3           | Protocol used to access the host using SNMP version 3.                               		    	|
| timeout          | How long until the SNMP request times out (Default: 120s).                     					|
| version          | The version of the SNMP protocol (version 3).                                 	   		    		|
| community        | The SNMP Community string (Default: public). 													    |
| port             | The SNMP port number used to perform SNMP version 3 queries (Default: 161).           				|
| contextName      | The name of the SNMP version 3 context, used to identify the collection of management information. |                                              
| authType         | The SNMP version 3 authentication protocol (MD5, SHA or NoAuth) to ensure message authenticity.    |
| privacy          | The SNMP v3 privacy protocol (DES, AES or NONE) used to encrypt messages for confidentiality.      | 
| username         | The username used for SNMP version 3 authentication.                     	  						|
| privacyPassword  | The password used to encrypt SNMP version 3 messages for confidentiality.						    |
| password         | The password used for SNMP version 3 authentication.                 								|
| retryIntervals   | The intervals (in seconds) between SNMP request retries.                						    |

**Example**

```yaml
resourceGroups:
  boston:
    resources:
      myHost3:
        attributes:
          host.name: my-host-03
          host.type: linux
        protocols:
          snmpv3:
            version: 3
            community: public
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
resourceGroups:
  boston:
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

| Parameter | Description                                                                   |
| --------- | ----------------------------------------------------------------------------- |
| wmi       | Protocol used to access the host.                                             |
| timeout   | How long until the WMI request times out (Default: 120s).                     |
| username  | Name used to establish the connection with the host via the WMI protocol.     |
| password  | Password used to establish the connection with the host via the WMI protocol. |

**Example**

```yaml
resourceGroups:
  boston:
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

Refer to [Monitors](../develop/monitors.md) for more information on how to configure custom resource monitoring.

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

## Configure the OTLP Receiver

By default, the **MetricsHub Agent** pushes the collected metrics to the [`OTLP Receiver`](https://github.com/open-telemetry/opentelemetry-collector/tree/main/receiver/otlpreceiver) through gRPC on port **TCP/4317**. To push data to the OTLP receiver of your choice:

* locate the `otel` section in your configuration file
* configure the `otel.exporter.otlp.metrics.endpoint` and `otel.exporter.otlp.logs.endpoint` parameters as follows:

```yaml
otel:
  otel.exporter.otlp.metrics.endpoint: https://<my-host>:4317
  otel.exporter.otlp.logs.endpoint: https://<my-host>:4317

resourceGroups: #...
```

where `<my-host>` should be replaced with the hostname or IP address of the server where the OTLP receiver is installed.

Use the below syntax if you wish to push metrics to the Prometheus OTLP Receiver:

```yaml
otel:
  otel.metrics.exporter: otlp
  otel.exporter.otlp.metrics.endpoint: http://<prom-server-host>:9090/api/v1/otlp/v1/metrics
  otel.exporter.otlp.metrics.protocol: http/protobuf
```

where `<prom-server-host>` should be replaced with the hostname or IP address of the server where *Prometheus* is running.

> **Note:**
> For specific configuration details, refer to the [OpenTelemetry Auto-Configure documentation](https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure). This resource provides information about the properties to be configured depending on your deployment requirements.

#### Trusted certificates file

If an `OTLP Receiver` certificate is required, configure the `otel.exporter.otlp.metrics.certificate` and `otel.exporter.otlp.logs.certificate` parameters under the `otel` section:

```yaml
otel:
  otel.exporter.otlp.metrics.certificate: /opt/metricshub/security/new-server-cert.crt
  otel.exporter.otlp.logs.certificate: /opt/metricshub/security/new-server-cert.crt

resourceGroups: # ...
```

The file should contain one or more X.509 certificates in PEM format.

## (Optional) Additional settings

### Authentication settings

#### Basic authentication header

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
        connectors: [ +VMwareESX4i, +VMwareESXi, "#system" ]
```

* To force a connector, precede the connector identifier with a plus sign (`+`), as in `+MIB2`.
* To exclude a connector from automatic detection, precede the connector identifier with a minus sign (`-`), like `-MIB2`.
* To stage a connector for processing by automatic detection, configure the connector identifier, for instance, `MIB2`.
* To stage a category of connectors for processing by automatic detection, precede the category tag with a hash (`#`), such as `#hardware` or `#system`.
* To exclude a category of connectors from automatic detection, precede the category tag to be excluded with a minus and a hash sign (`-#`), such as `-#system`.

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
  connectors: [ "-#hardware", "#system" ]
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
  connectors: [ DiskPart, "-#system" ]
  ```

  The core engine will perform automatic detection exclusively on the `DiskPart` connector.

* Example 6:

  ```yaml
  connectors: [ +Linux, MIB2 ]
  ```

  The core engine will force the execution of the `Linux` connector and subsequently perform automatic detection on the `MIB2` connector.

* Example 7:

  ```yaml
  connectors: [ -Linux ]
  ```

  The core engine will perform automatic detection on all connectors except the `Linux` connector.

* Example 8:

  ```yaml
  connectors: [ "#hardware", -MIB2 ]
  ```

  The core engine will perform automatic detection on connectors categorized under `hardware`, excluding the `MIB2` connector.

To know which connectors are available, refer to [Community Connector Platforms](../platform-requirements.html#!).

Otherwise, you can list the available connectors using the below command:

```shell-session
$ metricshub -l
```

For more information about the `metricshub` command, refer to [MetricsHub CLI (metricshub)](../troubleshooting/cli.md).

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
    resources:
      myHost1:
        attributes:
          host.name: my-host-01
          host.type: other
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

By default, **MetricsHub** resolves the `hostname` of the resource to a Fully Qualified Domain Name (FQDN) and displays this value in the [Host Resource](https://opentelemetry.io/docs/specs/semconv/resource/host/) attribute `host.name`. To display the configured hostname instead, set `resolveHostnameToFqdn` to `false`:

```yaml
resolveHostnameToFqdn: false

resourceGroups:
```

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

#### Timeout, duration and period format

Timeouts, durations and periods are specified with the below format:

| Unit | Description                     | Examples         |
| ---- | ------------------------------- | ---------------- |
| s    | seconds                         | 120s             |
| m    | minutes                         | 90m, 1m15s       |
| h    | hours                           | 1h, 1h30m        |
| d    | days (based on a 24-hour day)   | 1d               |
