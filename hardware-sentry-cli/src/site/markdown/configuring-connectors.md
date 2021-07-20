# Configuring Connectors

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

The connectors are automatically selected based on the device type and enabled protocols. You can however choose to specify a list of connectors or exclude some connectors.

## Specifying a list of connectors
  
Use the `-hdfs` (or ``--connectors``) option to specify the connector(s) to be used. Separate each connector by a comma.
  
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

Use `-hdfsExcluded` (or `--connectorsExcluded`) option to exclude connectors. Separate each connector to be excluded by a comma.

###### Example:

```shell script
$ java -jar hardware-sentry-cli-0.0.1-SNAPSHOT.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1 -hdfsExcluded MS_HW_DellOpenManage.connector,MS_HW_DellStorageManager.connector
```

   or

```shell script
$ java -jar hardware-sentry-cli-0.0.1-SNAPSHOT.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1 --connectorsExcluded MS_HW_DellOpenManage.connector,MS_HW_DellStorageManager.connector
```

###### Result:

All connectors will be run, except **MS_HW_DellOpenManage.connector** and **MS_HW_DellStorageManager.connector**, against **ecs1-01**.