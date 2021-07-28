keywords: debug, logs, logs directory
description: Hardware Sentry CLI: How to enable the debug mode and specify where the generated logs should be stored.

# Generating Logs

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

## Enabling the Debug Mode

Use the `-d` or `--debug` option to enable the debug mode.

###### Example:

```shell script
$ java -jar hardware-sentry-cli-0.9.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1 -hdfs MS_HW_DellOpenManage.connector -d
```

   or

```shell script
$ java -jar hardware-sentry-cli-0.9.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1 -hdfs MS_HW_DellOpenManage.connector --debug
```

###### Result:

This command will run the **MS_HW_DellOpenManage.connector** connector against the **ecs1-01** host and generate a log file in `C:\Users\<username>\AppData\Local\Temp\hardware-logs` on Windows, `/tmp/hardware-logs` on Linux.

## Specifying a different log directory

Use the `-o` or `--output` option to specify where the log should be stored.

###### Examples:

```shell script
   $ java -jar hardware-sentry-cli-0.9.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1 -hdfs MS_HW_DellOpenManage.connector -d -o D:/hardware-logs
```
  
```shell script
   $ java -jar hardware-sentry-cli-0.9.jar --hostname ecs1-01 --device-type LINUX --snmp-version V1 -hdfs MS_HW_DellOpenManage.connector --debug --output D:/hardware-logs
```

###### Result:

This will run the _MS_HW_DellOpenManage.connector_ connector against _ecs1-01_,
and generate a log file in the **D:/hardware-logs** directory.