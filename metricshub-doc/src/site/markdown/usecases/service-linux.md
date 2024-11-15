keywords: service, linux
description: How to monitor a service running on Linux

# Monitoring a service running on Linux

**MetricsHub** makes it easy to monitor your services managed by [systemd](https://systemd.io/). To set up monitoring, we only need to specify a dedicated instance of the [Linux - Service (systemctl)](../connectors/linuxservice.html) connector in the `config/metricshub.yaml` configuration file. Under this instance, we specify the name or the command line of the service to be monitored.

In the example below, we specify a dedicated instance of the  `metricshubLinuxService` connector (`LinuxService`) to monitor the `metricshub` service running on a Linux server.

  > Note: This page provides just one example of the data collection feature available in **MetricsHub**. For more information, refer to [Customize Data Collection](../configuration/configure-monitoring.md#customize-data-collection).

## Procedure

To monitor the `metricshub` service running on Linux: 

1. In the `config/metricshub.yaml` file, we configure the monitoring on a Linux machine through `SSH`: 

    ```yaml
    resources:
      prod-web:
        attributes:
          host.name: [prod-web-01, prod-web-02]
          host.type: linux
        protocols:
          ssh:
            username: monagent
            password: REDACTED
            timeout: 30
    ```
2. We specify the `httpd` dedicated instance of the [Linux - Service (systemctl)](../connectors/linuxservice.html) connector:

    ```yaml
        additionalConnectors:
          httpd: 
            uses: LinuxService # Connector used
    ```

3. We specify the service to be monitored:

    ```yaml
            variables:
              serviceNames: httpd
    ```

Here is the complete YAML configuration to be added to `config/metricshub.yaml` to monitor the `metricshub` service running on Linux:

```yaml
resources:
      prod-web:
        attributes:
          host.name: [prod-web-01, prod-web-02]
          host.type: linux
        protocols:
          ssh:
            username: monagent
            password: REDACTED
            timeout: 30
    additionalConnectors:
      httpd:
        uses: LinuxService
        variables:
          serviceNames: httpd
```