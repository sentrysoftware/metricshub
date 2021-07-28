keywords: hardware sentry cli, help, available options
description: Hardware Sentry CLI: how to get the list of all the available options.

# Getting Help

Use the `-h` or `--help` option to list all the available options:

 ###### Example:

```shell script
$ java -jar hardware-sentry-cli-0.9.jar -h
```

   or

```shell script
$ java -jar hardware-sentry-cli-0.9.jar --help
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
