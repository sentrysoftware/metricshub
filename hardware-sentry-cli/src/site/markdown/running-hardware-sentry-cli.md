keywords: Hardware Sentry CLI, commands, options
description: How to run the Hardware Sentry CLI: commands and options available.

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

# Running ${project.name}

The **Hardware Monitoring** solutions developed by Sentry Software come with the **Hardware Connector Library**, a library which consists of hundreds of hardware connectors (*.hdf files) that describe how to discover hardware components and detect failures.

When running the **${project.name}**, the connectors are automatically selected based on the device type provided and the protocol enabled [(automatic mode)](#automatic). You can however indicate to the **${project.name}** which connectors should be used or excluded [(manual mode)](#manual).

## Running ${project.name} in automatic mode

To run the **${project.name}**, use one of these commands:

```batch
   $ java -jar hardware-sentry-cli-<version>.jar -host <hostname> -dt <device-type> <protocol-configuration>
```
  
```batch
   $ java -jar hardware-sentry-cli-<version>.jar --hostname <hostname> --device-type <device-type> <protocol-configuration>
```

where:

* `<version>` corresponds to the version of the **${project.name}**
* `<hostname>` corresponds to the name of the device, or its IP address
* `<device-type>` corresponds to the operating system or the type of the device to be monitored. Possible values are:

    * `HP_OPEN_VMS` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#hp-openvms" target="_blank">HP Open VMS systems</a>
    * `HP_TRU64_UNIX` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#hp-tru64" target="_blank">HP Tru64 systems</a>
    * `HP_UX` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#hp-ux" target="_blank">HP UX systems</a>
    * `IBM_AIX` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#ibm-aix" target="_blank">IBM AIX systems</a>
    * `LINUX` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#linux" target="_blank">Linux systems</a>
    * `MGMT_CARD_BLADE_ESXI` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#out-of-band" target="_blank">Out-of-band</a>, <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#blade-chassis" target="_blank">blade chassis</a>, and <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#vmware-esx" target="_blank">VMware ESX systems</a> 
    * `MS_WINDOWS` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#microsoft-windows" target="_blank">Microsoft Windows systems</a>
    * `NETWORK_SWITCH` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#network-device" target="_blank">network devices</a>
    * `STORAGE` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#storage-system" target="_blank">storage systems</a>
    * `SUN_SOLARIS` for these <a href="https://www.sentrysoftware.com/library/hc/24/platform-requirements.html#oracle-solaris" target="_blank">Oracle solaris systems</a>.

* `<protocol-configuration>` corresponds to the protocol the **${project.name}** will use to communicate with the hardware instrumentation layers to retrieve hardware information. To know how to set these different options, refer to:

    * [Using the HTTP Protocol](#http)
    * [Using the SNMP Protocol](#snmp)
    * [Using the WBEM Protocol](#wbem)
    * [Using the WMI Protocol](#wmi)

<a name="http"></a>

### Using the HTTP Protocol

Use the options below to configure the HTTP protocol:

| Option            | Description                                                           | Available Values | Default Value |
| ----------------- | --------------------------------------------------------------------- | ---------------- | ------------- |
| `--http`          | This option takes no argument. <br> Use http instead of https.</br>   |                  | false         |
| `--http-port`     | Port to be used to perform HTTP requests.                             |                  | 443           |
| `--http-timeout`  | Number of seconds **${project.name}** will wait for an HTTP response. |                  | 120           |
| `--http-username` | Username to be used to perform HTTP requests.                         |                  |               |
| `--http-password` | Password to be used to perform HTTP requests.                         |                  |               |

#### Example

In the example below, we use the HTTP protocol to establish the connection to the **purem-san** device:
   
   ```batch
   $ java -jar hardware-sentry-cli-0.9.jar --hostname purem-san  --device-type STORAGE --http-port 443 --http-username pureuser --http-password ****** -hdfs MS_HW_PureStorageREST.hdf
   ```

<a name="snmp"></a>

### Using the SNMP Protocol

Use the options below to configure the SNMP protocol:

| Option                   | Description                                                                                                 | Available Values                                                                   | Default Value |
| ------------------------ | ----------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------- | ------------- |
| `--snmp-version`         | Version of the SNMP protocol that **${project.name}** must use to retrieve information from the SNMP agent. | <ul><li>V1</li><li>V2C</li><li>V3_NO_AUTH</li><li>V3_MD5</li><li>V3_SHA</li> </ul> | V1            |
| `--snmp-port`            | SNMP port to be used to perform SNMP queries .                                                              |                                                                                    | 161           |
| `--snmp-community`       | SNMP community to be used to perform SNMP queries.                                                          |                                                                                    | public        |
| `--snmp-timeout`         | Number of seconds **${project.name}** will wait for an SNMP response.                                       |                                                                                    | 120           |
| `--snmp-username`        | _(When SNMP Version is V3_MD5 or V3-SHA)_ Username to be used to perform the SNMP queries.                  |                                                                                    |               |
| `--snmp-password`        | _(When SNMP Version is V3_MD5 or V3-SHA)_ Password to be used to perform the SNMP queries.                  |                                                                                    |               |
| `--snmp-privacy`         | _(When SNMP Version is V3_MD5 or V3-SHA)_ Encryption algorithm to be used to perform the SNMP queries.      | <ul><li>AES</li><li>DES</li><li>NO_ENCRYPTION </li> </ul>                          |               |
| `--snmp-privacyPassword` | _(When SNMP Version is V3_MD5 or V3-SHA)_ Privacy password to be used to perform the SNMP queries.          |

#### Examples

##### SNMP v1

In the example below, we use the SNMP protocol v1 to establish the connection to the **ecs1-01** device running on Linux:
   
   ```batch
   $ java -jar hardware-sentry-cli-0.9.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1
   ```

##### SNMP v3

In the example below, we use the SNMP protocol v3 to establish the connection to the **10.0.11.80** device:

   ```batch
   $ java -jar hardware-sentry-cli-0.9.jar --hostname 10.0.11.80 --device-type MGMT_CARD_BLADE_ESXI --snmp-version V3_NO_AUTH --snmp-port 161 --snmp-community public --snmp-timeout 120
    ```

<a name="wbem"></a>

### Using the WBEM Protocol

Use the options below to configure the WBEM protocol:

| Option             | Description                                                                                | Available Values                                                                            | Default Value |
| ------------------ | ------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------- | ------------- |
| `--wbem-protocol`  | Protocol to be used to establish the connection with the device through the WBEM protocol. | <ul><li>https</li><li>http</li></ul>                                                        | https         |
| `--wbem-port`      | Port to be used to perform WBEM queries.                                                   | <ul><li>5989 for encrypted connections</li><li>5988 for non-encrypted connections</li></ul> | 5989          |
| `--wbem-namespace` | WBEM namespace. <br> Leave blank to let the solution detect the proper namespace.</br>     |                                                                                             | root/cimv2    |
| `--wbem-timeout`   | Number of seconds **${project.name}** will wait for a WBEM response.                       |                                                                                             | 120           |
| `--wbem-username`  | Username to be used to establish the connection with the device through the WBEM protocol. |                                                                                             |               |
| `--wbem-password`  | Password to be used to establish the connection with the device through the WBEM protocol. |                                                                                             |               |

#### Example

In the example below, we use the WBEM protocol to establish the connection to the **dev-hv-01** device:
   
   ```batch
   $ java -jar hardware-sentry-cli-0.9.jar --hostname dev-hv-01  --device-type STORAGE --wbem-protocol https --wbem-namespace root/emc --wbem-username admin --wbem-password ****** -hdfs MS_HW_EMCDiskArray.connector
   ```

<a name="wmi"></a>

### Using the WMI Protocol

Use the options below to configure the WMI protocol:

| Option            | Description                                                                               | Available Values | Default Value |
| ----------------- | ----------------------------------------------------------------------------------------- | ---------------- | ------------- |
| `--wmi-namespace` | WMI namespace. <br> Leave blank to let the solution detect the proper namespace. </br>    |                  | root/cimv2    |
| `--wmi-timeout`   | Number of seconds **${project.name}** will wait for a WMI response.                       |                  | 120           |
| `--wmi-username`  | Username to be used to establish the connection with the device through the WMI protocol. |                  |               |
| `--wmi-password`  | Password to be used to establish the connection with the device through the WMI protocol. |

#### Example

In the example below, we use the WMI protocol to establish the connection to the **hpb7k-l1** device:

   ```batch
   $ java -jar hardware-sentry-cli-0.9.jar --hostname hpb7k-l1 --device-type MS_WINDOWS --wmi-namespace root\wmi --wmi-username hpb7k-l1\administrator  --wmi-timeout 180
  ```

<a name="manual"></a>

## Running ${project.name} in manual mode

### Specifying a list of connectors
  
Use the `-hdfs` (or ``--connectors``) option to specify the connector(s) to be used. Separate each connector by a comma.

The exhaustive list of connectors is available in the <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html" target="_blank">Hardware Connector Library User Documentation</a>.
  
###### Example:

```batch
$ java -jar hardware-sentry-cli-0.0.1-SNAPSHOT.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1 -hdfs MS_HW_DellOpenManage.connector,MS_HW_DellStorageManager.connector
 ```

   or

```batch
$ java -jar hardware-sentry-cli-0.0.1-SNAPSHOT.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1 --connectors MS_HW_DellOpenManage.connector,MS_HW_DellStorageManager.connector
 ```

###### Result:

```json
   {
     "total" : 3,
     "monitors" : [ {
       "id" : "ecs1-01@MS_HW_DellOpenManage.connector",
       "name" : "MS_HW_DellOpenManage.connector",
       "monitorType" : "CONNECTOR",
       "parentId" : "ecs1-01",
       "targetId" : "ecs1-01",
       "extendedType" : null,
       "parameters" : {
         "status" : {
           "name" : "status",
           "collectTime" : null,
           "threshold" : null,
           "state" : "OK",
           "unit" : "{0 = OK ; 1 = Degraded ; 2 = Failed}",
           "status" : null,
           "statusInformation" : null
         },
         "testReport" : {
           "name" : "testReport",
           "collectTime" : null,
           "threshold" : null,
           "state" : "OK",
           "unit" : null,
           "value" : null
         }
       },
       "metadata" : { }
     }, {
       "id" : "ecs1-01@MS_HW_DellStorageManager.connector",
       "name" : "MS_HW_DellStorageManager.connector",
       "monitorType" : "CONNECTOR",
       "parentId" : "ecs1-01",
       "targetId" : "ecs1-01",
       "extendedType" : null,
       "parameters" : {
         "status" : {
           "name" : "status",
           "collectTime" : null,
           "threshold" : null,
           "state" : "OK",
           "unit" : "{0 = OK ; 1 = Degraded ; 2 = Failed}",
           "status" : null,
           "statusInformation" : null
         },
         "testReport" : {
           "name" : "testReport",
           "collectTime" : null,
           "threshold" : null,
           "state" : "OK",
           "unit" : null,
           "value" : null
         }
       },
       "metadata" : { }
     }, {
       "id" : "ecs1-01",
       "name" : "ecs1-01",
       "monitorType" : "TARGET",
       "parentId" : null,
       "targetId" : "ecs1-01",
       "extendedType" : null,
       "parameters" : { },
       "metadata" : {
         "location" : "remote",
         "operatingSystemType" : "LINUX"
       }
     } ]
   }
   ```


### Excluding a list of connectors

Use `-hdfsExcluded` (or `--connectorsExcluded`) option to exclude connectors. Separate each connector to be excluded by a comma.

The exhaustive list of connectors is available in the <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html" target="_blank">Hardware Connector Library User Documentation</a>.

###### Example:

```batch
$ java -jar hardware-sentry-cli-0.0.1-SNAPSHOT.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1 -hdfsExcluded MS_HW_DellOpenManage.connector,MS_HW_DellStorageManager.connector
```

   or

```batch
$ java -jar hardware-sentry-cli-0.0.1-SNAPSHOT.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1 --connectorsExcluded MS_HW_DellOpenManage.connector,MS_HW_DellStorageManager.connector
```

###### Result:

All connectors will be run, except **MS_HW_DellOpenManage.connector** and **MS_HW_DellStorageManager.connector**, against **ecs1-01**.