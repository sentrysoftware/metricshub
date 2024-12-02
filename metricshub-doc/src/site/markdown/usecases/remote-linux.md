keywords: linux, remote
description: How to monitor a remote system running on Linux

# Monitoring a remote system running on Linux

Monitoring a remote system running on Linux is straightforward with **MetricsHub**. 
After identifying the [linux connectors](https://www.metricshub.com/docs/latest/connectors/tags/linux.html) 
available to collect metrics about the OS, the network interfaces, or the server itself, 
you need to configure one or several monitoring protocols, typically: 

- `ssh` to collect operating system-specific metrics and information about CPU, file system, memory, network interface, physical disk, process, etc.
- `snmp` or `wbem` to collect hardware information about the physical machine.  

In the example below, we configure **MetricsHub** to monitor the remote machine `dev-nvidia-01` running on `Linux` through the `SSH` protocol.
We do not declare any specific connectors to let **MetricsHub** detect the most suitable ones and collect the metrics available. 

## Procedure

To monitor a remote machine running on Linux:

1. In the `config/metricshub.yaml` file, we configure the monitoring on a Linux machine through `SSH`: 

    ```yaml
          dev-nvidia-01:
            attributes:
              host.name: dev-nvidia-01
              host.type: linux
    ```
2. Then, we configure the SSH protocol

    ```yaml
          protocols:
              ssh:
                username: myusername
                password: mypassword
    ```

Here is the complete YAML configuration to be added to `config/metricshub.yaml` 
to monitor a remote machine running on Linux:

```yaml
      dev-nvidia-01:
        attributes:
          host.name: dev-nvidia-01
          host.type: linux
        protocols:
          ssh:
            username: myusername
            password: mypassword
```