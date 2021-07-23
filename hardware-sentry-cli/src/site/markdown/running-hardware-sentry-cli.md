# Running ${project.name}

To run **${project.name}**, use one of these commands:

```shell script
   $ java -jar hardware-sentry-cli-<version>.jar -host <hostname> -dt <device-type> <protocol-configuration>
```
  
```shell script
   $ java -jar hardware-sentry-cli-<version>.jar --hostname <hostname> --device-type <device-type> <protocol-configuration>
```

where:

* `<version>` corresponds to the version of the **${project.name}**
* `<hostname>` corresponds to the name of the device, or its IP address
* `<device-type>` corresponds to the operating system or the type of the device to be monitored. Possible values are:
    * `HP` for these <a href="https://www.sentrysoftware.com/library/hc/24/platform-requirements.html#hp-openvms" target="_blank">HP OpenVMS</a>, and <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#hp-ux" target="_blank">HP-UX</a> systems
    * `LINUX` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#linux" target="_blank">Linux systems</a>
    * `NETWORK` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#network-device" target="_blank"> network devices</a> 
    * `NT` for <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#microsoft-windows" target="_blank">Microsoft Windows</a>
    * `OOB` for these <a href="https://www.sentrysoftware.com/library/hc/24/platform-requirements.html#out-of-band" target="_blank">Out-of-Band systems</a>
    * `OSF1` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#hp-tru64" target="_blank">HP Tru64 systems</a>
    * `RS6000` <a href="" target="_blank"></a>
    * `SOLARIS` these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#!#network-device" target="_blank">Oracle/Solaris systems</a>
    * `STORAGE` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#storage-system" target="_blank">storage systems</a>
    * `VMS` for <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#vmware-esx" target="_blank">VMware ESX</a>.

* `<protocol-configuration>` corresponds to the protocol **${project.name}** will use to communicate with the hardware instrumentation layers to retrieve hardware information. To know how to set these different options, refer to:
    * [SNMP configuration options](#snmp)
    * [HTTP configuration options](#http)
    * [WMI configuration options](#wmi)
    * [WBEM configuration options](#wbem)

##### Example:
   
   ```shell script
   $ java -jar hardware-sentry-cli-0.9.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1
   ```
   
###### Result:
   ```json
   {
     "total" : 4,
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
           "unit" : null,
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
           "unit" : null,
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
       "id" : "ecs1-01@MS_HW_MIB2Linux.connector",
       "name" : "MS_HW_MIB2Linux.connector",
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
           "unit" : null,
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

<a name="snmp"></a>

## SNMP configuration options

Here are the available options for the SNMP configuration:
  
  | Option                 | Description                                                          | Available Values                            | Default Value |
  |------------------------|----------------------------------------------------------------------|---------------------------------------------|---------------|
  | --snmp-version         | The SNMP version                                                     | <ul><li>V1</li><li>V2C</li><li>V3_NO_AUTH</li><li>V3_MD5</li><li>V3_SHA</li> </ul> | V1            |
  | --snmp-port            | The SNMP port                                                        |                                             | 161           |
  | --snmp-community       | The SNMP community                                                   |                                             | public        |
  | --snmp-timeout         | The SNMP timeout, in seconds                                         |                                             | 120           |
  | --snmp-username        | The user name (when the SNMP version is V3_MD5 or V3-SHA)            |                                             |               |
  | --snmp-password        | The user password (when the SNMP version is V3_MD5 or V3-SHA)        |                                             |               |
  | --snmp-privacy         | The encryption algorithm (when the SNMP version is V3_MD5 or V3-SHA) | <ul><li>AES</li><li>DES</li><li>NO_ENCRYPTION </li>   </ul>             |               |
  | --snmp-privacyPassword | The privacy password (when the SNMP version is V3_MD5 or V3-SHA)     |

<a name="wmi"></a>

## WMI configuration options

Here are the available options for the WMI configuration:
  
| Option          | Description                 | Available Values | Default Value                                                   |
|-----------------|-----------------------------|------------------|---------------|
| --wmi-namespace | The WMI namespace. <br> Leave blank to let the solution detect the proper namespace </br>          |                  | root/cimv2    |
| --wmi-timeout   | The WMI timeout, in seconds |                  | 120           |
| --wmi-username  | The user name               |                  |               |
| --wmi-password  | The user password           |                  |               | 

<a name="wbem"></a>

## WBEM configuration options

Here are the available options for the WBEM configuration:
  
| Option           | Description                  | Available Values | Default Value |
|------------------|------------------------------|------------------|---------------|
| --wbem-protocol  | The WBEM protocol            | <ul><li>https</li><li>http</li></ul>    | https         |
| --wbem-port      | The WBEM port                |                  | 5989          |
| --wbem-namespace | The WBEM namespace           |                  | root/cimv2    |
| --wbem-timeout   | The WBEM timeout, in seconds |                  | 120           |
| --wbem-username  | The user name                |                  |               |
| --wbem-password  | The user password            |                  |               |

<a name="http"></a>

## HTTP configuration options

Here are the available options for the HTTP configuration:
  
| Option          | Description                     | Available Values | Default Value | Remark            |
|-----------------|---------------------------------|------------------|---------------|-------------------|
| --http          | Use HTTP instead of HTTPS       |                  | false         | Takes no argument |
| --http-port     | The HTTP(S) port                |                  | 443           |                   |
| --http-timeout  | The HTTP(S) timeout, in seconds |                  | 120           |                   |
| --http-username | The user name                   |                  |               |                   |
| --http-password | The user password               |                  |               |                   |
