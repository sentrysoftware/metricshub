keywords: process command lines, windows
description: How to monitor a process on Windows with MetricsHub

# Monitoring a process on Windows

**MetricsHub** makes it easy to monitor a process on Windows. To set up monitoring, you only need to create a connector based on the [Windows - Processes (WMI)](../connectors/windowsprocess.html) connector and specify the process to be monitored in the `config/metricshub.yaml` configuration file.

In this example, we created a `metricshubWindowsProcess` connector based on the `WindowsProcess` connector. This connector is always activated and monitors the `metricshub` process. 

  > Note: This page provides just one example of the data collection feature available in **MetricsHub**. For more information, refer to [Customize Data Collection](../configuration/configure-monitoring.md#customize-data-collection).


## Procedure

To monitor the `metricshub` process:

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

2. We create the `metricshubWindowsProcess` connector based on the [Windows - Processes (WMI)](../connectors/windowsprocess.html) connector: 

    ```yaml
        additionalConnectors:
          metricshubWindowsProcess:
            uses: WindowsProcess # Connector used
            force: true # Connector is always activated
    ```

3.  We specify the process to be monitored:

    ```yaml
            variables:
              matchCommand: metricshub
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
      metricshubWindowsProcess: 
        uses: WindowsProcess 
        force: true
        variables:
          matchCommand: metricshub
```