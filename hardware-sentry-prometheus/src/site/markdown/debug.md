keywords: debug, prometheus exporter, hardware
description: How to enable the debug mode on Hardware Sentry Exporter for Prometheus

# Enabling the debug mode

To enable the debug mode, edit the `hardware-sentry-config.yml` file and add the `loggerLevel` parameter just before the `targets` section:

```
loggerLevel: debug
targets:
- target:
    hostname: myhost
    type: linux
  snmp:
    version: v1
    community: public
    port: 161
    timeout: 120s
  excludedConnectors: [ SunF15K, HPiLO ]
```

Possible values are: `all`, `trace`, `debug`, `info`, `warn`, `error`, `fatal`, `off`.

By default, the debug output file is saved in the `hardware-logs` directory under the temporary directory of the local machine, which is:

* `C:\Users\<username>\AppData\Local\Temp\hardware-logs` on Windows
* `/tmp/hardware-logs` on Linux.

If you want to specify another output directory, edit the `hardware-sentry-config.yml` file and add the `outputDirectory` parameter just before the `targets` section:

```
loggerLevel: debug
outputDirectory: C:\\Users\\<username>\\AppData\\Local\\Temp\\hardware-logs2021
targets:
- target:
    hostname: myhost
    type: linux
  snmp:
    version: v1
    community: public
    port: 161
    timeout: 120s
  excludedConnectors: [ SunF15K, HPiLO ]
```
