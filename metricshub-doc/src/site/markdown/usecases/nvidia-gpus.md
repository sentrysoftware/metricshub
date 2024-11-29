keywords: nvidia, GPU
description: How to configure MetricsHub to monitor Nvidia GPUs.

# Monitoring NVIDIA GPUs

## Overview

**MetricsHub** leverages the NVIDIA System Management Interface (NVIDIA SMI) to collect hardware metrics from Windows and Linux systems equipped with NVIDIA GPUs.

By configuring monitoring, you can track:
* GPU memory usage,
* Data transfer (sent and received),
* GPU resource utilization for encoding and decoding tasks,
* And much more.

For the complete list of available metrics, refer to the [Nvidi Smi connector documentation](https://www.metricshub.com/docs/latest/connectors/nvidiasmi.html#!#metrics).

In the example below, we configure **MetricsHub** to connect to the management card of a Linux server to monitor its NVIDIA GPUs. 


## Procedure

To monitor the NVIDIA GPUs:

1. In the `config/metricshub.yaml` file, we create the resource `dev-nvidia-01` with the following attributes:

   * hostname: `dev-nvidia-01`
   * host type: `linux`

    ```yaml
    resources:
      dev-nvidia-01:
        attributes:
          host.name: dev-nvidia-01
          host.type: linux
    ```

2. We configure **MetricsHub** to connect to the management card of the server using the `http` protocol:

    ```yaml
        protocols:
            http:
                hostname: mgt-dev-nvidia-01
                https: true
                port: 443
                username: myusername # Update with the actual username
                password: mypassword # Update with the actual password
    ```

Here is the complete YAML configuration to be added to `config/metricshub.yaml` to monitor NVIDIA GPUs:

```yaml
    resources:
      dev-nvidia-01:
        attributes:
          host.name: dev-nvidia-01
          host.type: linux
        protocols:
          http:
            hostname: mgt-dev-nvidia-01
            https: true
            port: 443
            username: myusername # Update with the actual username
            password: mypassword # Update with the actual password
```