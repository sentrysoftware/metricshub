keywords: process command lines, windows
description: How to monitor a process on Windows with MetricsHub

# Monitoring a process on Windows

**MetricsHub** makes it easy to monitor a process through its name or command line. To set up monitoring, we only need to specify a dedicated instance of the [Windows - Processes (WMI)](../connectors/windowsprocess.html) connector in the `config/metricshub.yaml` configuration file. Under this instance, we specify the name or the command line of the process to be monitored.

In the example below, we specify a dedicated instance of the `WindowsProcess` connector (`mssqlProcess`) to monitor the process of Microsoft SQL Server, i.e. the process(es) whose command line contains `sqlservr.exe`.

  > Note: This page provides just one example of the data collection feature available in **MetricsHub**. For more information, refer to [Customize Data Collection](../configuration/configure-monitoring.md#customize-data-collection).

## Procedure

To monitor the Microsoft SQL Server process:

1. In the `config/metricshub.yaml` file, we configure the monitoring on a local Windows machine through `WMI`:

    ```yaml
        resources:
          localhost:
            attributes:
              host.name: localhost
              host.type: windows
            protocols:
              wmi:
                timeout: 120
    ```

2. We specify the `mssqlProcess` dedicated instance of the [Windows - Processes (WMI)](../connectors/windowsprocess.html) connector:

    ```yaml
            additionalConnectors:
              mssqlProcess:
                uses: WindowsProcess # Connector used
    ```

3.  We specify the regular expression that will match with the command line of Microsoft SQL Server processes (note how we escape the dot in `sqlservr.exe`):

    ```yaml
                variables:
                  matchCommand: "sqlservr\\.exe"
    ```

Here is the complete YAML configuration to be added to `config/metricshub.yaml` to monitor the `metricshub` process on a Windows machine:

```yaml
    resources:
      localhost:
        attributes:
          host.name: localhost
          host.type: windows
        protocols:
          wmi:
            timeout: 120
        additionalConnectors:
          mssqlProcess: 
            uses: WindowsProcess 
            variables:
              matchCommand: "sqlservr\\.exe"
```