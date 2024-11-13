keywords: service, linux
description: How to monitor a service running on Linux

# Monitoring a service running on Linux

**MetricsHub** makes it easy to monitor your services running on Linux. To set up monitoring, you only need to create a connector based on the [Linux - Service (systemctl)](../connectors/linuxservice.html) connector and specify the service to be monitored in the `config/metricshub.yaml` configuration file.

In the example below, we created a `metricshubLinuxService` connector based on the `LinuxService` connector. This connector is always activated and monitors the `metricshub` service running on a Linux server.

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
2. We create the `metricshubLinuxService` connector based on the [Linux - Service (systemctl)](../connectors/linuxservice.html) connector:

    ```yaml
        additionalConnectors:
          metricshubLinuxService: 
            uses: LinuxService # Connector used
            force: true # Connector is always activated
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
        force: true 
        variables:
          serviceNames: metricshub
```