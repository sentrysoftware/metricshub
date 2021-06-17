# How to use the Hardware Sentry Command Line Interface

  ## Prerequisites

  - Minimum JRE version: 11

  ## Minimal execution

   The simplest way to execute the Hardware Sentry Command Line Interface is :

   ```shell script
   $ java -jar hardware-sentry-cli-<version>.jar -host <hostname> -dt <device-type> <protocol-configuration>
   ```
   or
   ```shell script
   $ java -jar hardware-sentry-cli-<version>.jar --hostname <hostname> --device-type <device-type> <protocol-configuration>
   ```
   where:
   
   - _\<hostname\>_ can be the name of the target, or its IP address
   - _\<device-type\>_ can be one of the following:
   
        - HP
        - LINUX
        - NETWORK
        - NT
        - OOB
        - OSF1
        - RS6000
        - SOLARIS
        - STORAGE
        - VMS
   - _\<protocol-configuration\>_ refers to a set of:
   
        - [SNMP configuration options](#snmp)
        - [HTTP configuration options](#http)
   
   ###### Example:
   ```shell script
   $ java -jar hardware-sentry-cli-0.0.1-SNAPSHOT.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1
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

  ###### Note:
  The engine automatically selects a list of connectors to monitor the target.
  You can also [specify a list of connectors](#hdfs) to monitor the target.

  <a name="snmp"></a>
  ## SNMP configuration options
  Here are the available options for the SNMP configuration:
  
  | Option                 | Description                                                          | Available Values                            | Default Value |
  |------------------------|----------------------------------------------------------------------|---------------------------------------------|---------------|
  | --snmp-version         | The SNMP version                                                     | V1<br>V2C<br>V3_NO_AUTH<br>V3_MD5<br>V3_SHA | V1            |
  | --snmp-port            | The SNMP port                                                        |                                             | 161           |
  | --snmp-community       | The SNMP community                                                   |                                             | public        |
  | --snmp-timeout         | The SNMP timeout, in seconds                                         |                                             | 120           |
  | --snmp-username        | The user name (when the SNMP version is V3_MD5 or V3-SHA)            |                                             |               |
  | --snmp-password        | The user password (when the SNMP version is V3_MD5 or V3-SHA)        |                                             |               |
  | --snmp-privacy         | The encryption algorithm (when the SNMP version is V3_MD5 or V3-SHA) | AES<br>DES<br>NO_ENCRYPTION                 |               |
  | --snmp-privacyPassword | The privacy password (when the SNMP version is V3_MD5 or V3-SHA)     |                                             |               |
  
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

  <a name="hdfs"></a>
  ## Specifying a list of connectors
  You can specify a comma-separated list of connectors by using the _-hdfs_ (or _--connectors_) option.
  
   ###### Example:
   ```shell script
   $ java -jar hardware-sentry-cli-0.0.1-SNAPSHOT.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1 -hdfs MS_HW_DellOpenManage.connector,MS_HW_DellStorageManager.connector
   ```
   or
   ```shell script
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

  ## Excluding a list of connectors
  You can exclude a comma-separated list of connectors by using the _-hdfsExcluded_ (or _--connectorsExcluded_) option.
  
   ###### Example:
   ```shell script
   $ java -jar hardware-sentry-cli-0.0.1-SNAPSHOT.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1 -hdfsExcluded MS_HW_DellOpenManage.connector,MS_HW_DellStorageManager.connector
   ```
   or
   ```shell script
   $ java -jar hardware-sentry-cli-0.0.1-SNAPSHOT.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1 --connectorsExcluded MS_HW_DellOpenManage.connector,MS_HW_DellStorageManager.connector
   ```

   ###### Result:
   This will run all stored connectors, except _MS_HW_DellOpenManage.connector_ and _MS_HW_DellStorageManager.connector_,
   against _ecs1-01_.

  ## Enabling the debugging mode
  You can enable the debugging mode by using the _-d_ (or _--debug_) option.
  
   ###### Example:
   ```shell script
   $ java -jar hardware-sentry-cli-0.0.1-SNAPSHOT.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1 -hdfs MS_HW_DellOpenManage.connector -d
   ```
   or
   ```shell script
   $ java -jar hardware-sentry-cli-0.0.1-SNAPSHOT.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1 -hdfs MS_HW_DellOpenManage.connector --debug
   ```

   ###### Result:
   This will run the _MS_HW_DellOpenManage.connector_ connector against _ecs1-01_,
   and generate a log file in the **logs** directory.

  ## Displaying the available options
  You can get an overview of all available options by using the _-h_ (or _--help_) option.
  
   ###### Example:
   ```shell script
   $ java -jar hardware-sentry-cli-0.0.1-SNAPSHOT.jar -h
   ```
   or
   ```shell script
   $ java -jar hardware-sentry-cli-0.0.1-SNAPSHOT.jar --help
   ```

   ###### Result:
   ```shell script
   Usage: hardware-sentry-cli [-dhV] -dt=<deviceType> -host=<hostname>
                              [--snmp-community=<community>]
                              [--snmp-password=<password>] [--snmp-port=<port>]
                              [--snmp-privacy=<privacy>]
                              [--snmp-privacyPassword=<privacyPassword>]
                              [--snmp-timeout=<timeout>]
                              [--snmp-username=<username>]
                              [--snmp-version=<snmpVersion>] [-hdfs=<hdfs>[,
                              <hdfs>...]]... [-hdfsExcluded=<hdfsExclusion>[,
                              <hdfsExclusion>...]]...
     -d, --debug              Activate debug mode for logs.
         -dt, --device-type=<deviceType>
                              Enter the Device Type to monitor.
     -h, --help               Show this help message and exit.
         -hdfs, --connectors=<hdfs>[,<hdfs>...]
                              Enter the hdfs to run.
         -hdfsExcluded, --connectorsExcluded=<hdfsExclusion>[,<hdfsExclusion>...]
                              Enter the hdfs to exclude.
         -host, --hostname=<hostname>
                              Enter a hostname or an  IP Address.
         --snmp-community=<community>
                              SNMP Community, default : public.
         --snmp-password=<password>
                              Password.
         --snmp-port=<port>   SNMP Port, default : 161.
         --snmp-privacy=<privacy>
                              Privacy(Encryption).
         --snmp-privacyPassword=<privacyPassword>
                              Privacy Password.
         --snmp-timeout=<timeout>
                              SNMP Timeout, unit: seconds, default: 120.
         --snmp-username=<username>
                              Username.
         --snmp-version=<snmpVersion>
                              SNMP version : V1, V2C, V3_NO_AUTH, V3_MD5, V3_SHA.
     -V, --version            Print version information and exit.
   ```
