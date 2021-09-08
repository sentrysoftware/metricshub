keywords: debug, prometheus exporter, hardware
description: How to enable the debug mode on Hardware Sentry Exporter for Prometheus

# Enabling the debug mode

To enable the debug mode, run **${project.name}** using the debugMode option:

```
$ java -jar hardware-sentry-prometheus-0.0.1-SNAPSHOT.jar --debugMode=true
```

By default, the debug output file is saved in the hardware-logs directory under the temporary directory of the local machine. Example: ```C:\Users\<username>\AppData\Local\Temp\hardware-logs on Windows or /tmp/hardware-logs on Linux.```

If you want to specify another output directory, start **${project.name}** using the outputDirectory option:

```
$ java -jar hardware-sentry-prometheus-0.0.1-SNAPSHOT.jar --debugMode=true --outputDirectory=/path-to-logs
```
