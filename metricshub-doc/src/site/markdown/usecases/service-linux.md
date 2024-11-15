keywords: service, linux
description: How to monitor a service running on Linux

# Monitoring a service running on Linux

**MetricsHub** makes it easy to monitor your services through its name or command line. To set up monitoring, we only need to specify a dedicated instance of the [Linux - Service (systemctl)](../connectors/linuxservice.html) connector in the `config/metricshub.yaml` configuration file. Under this instance, we specify the name or the command line of the process to be monitored.

In the example below, we specify a dedicated instance of the  `metricshubLinuxService` connector (`LinuxService`) to monitor the `metricshub` service running on a Linux server.

  > Note: This page provides just one example of the data collection feature available in **MetricsHub**. For more information, refer to [Customize Data Collection](../configuration/configure-monitoring.md#customize-data-collection).

## Procedure

To monitor the `metricshub` service running on Linux: 

1. In the `config/metricshub.yaml` file, we configure the monitoring on a local Linux machine through `SSH`: 

    ```yaml
    resources:
      localhost:
        attributes:
          host.name: localhost
          host.type: linux
        protocols:
          ssh:
            timeout: 120
    ```
2. We specify the `metricshubLinuxService` dedicated instance of the [Linux - Service (systemctl)](../connectors/linuxservice.html) connector:

    ```yaml
        additionalConnectors:
          metricshubLinuxService: 
            uses: LinuxService # Connector used
    ```

3. We specify the service to be monitored:

    ```yaml
            variables:
              serviceNames: metricshub
    ```

Here is the complete YAML configuration to be added to `config/metricshub.yaml` to monitor the `metricshub` service running on Linux:

```yaml
resources:
  localhost:
    attributes:
      host.name: localhost
      host.type: linux
    protocols:
      ssh:
        timeout: 120
    additionalConnectors:
      metricshubLinuxService:
        uses: LinuxService
        variables:
          serviceNames: metricshub
```