# Enabling the Debug Mode

Use the `-d` or `--debug` option to enable the debug mode.
  
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