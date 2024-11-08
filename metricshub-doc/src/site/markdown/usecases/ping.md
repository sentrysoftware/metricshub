keywords: ping
description: How to ping resources with MetricsHub

# Pinging resources

## Overview

With **MetricsHub**, you can ping your resources through ICMP (Internet Control Message Protocol) to verify that they can be reached within an acceptable response time. To achieve this, you just need to configure in the `config/metricshub.yaml` file:
* the resources to be monitored 
* the ICMP Ping protocol for each of your resource.

In the example below, we ping 9 resources that belong to the  `baie9` resource group using the **ICMP protocol**. A timeout of 3 seconds is configured and the result (OK, KO status) is displayed in a Grafana Dashboard.

![MetricsHub - Pinging resources](../images/metricshub-ping-check-feature.png)

## Procedure

To configure the **Ping Check** feature: 

1. In the `config/metricshub.yaml` file, we created the `baie9` resource group. The site, which corresponds to the physical location, is `baie9` as well:

    ```yaml
    resourceGroups:

      baie9:

        attributes:
          site: baie9
    ```

2. We configured our resources as follows:

    ```yaml
        resources:
          host-service:
            attributes:
              host.names: [ tallinn, bacon, powerscale, purex-san, purem-san, xtremio, oracle-zfs-7320, pure-san, vm-users-01]
    ```

3. We configured the ICMP protocol as follows:

    ```yaml
            protocols:
              ping:
                timeout: 3  
    ```

We came up to this version of the `config/metricshub.yaml` configuration file:

   ```yaml
    resourceGroups:

      baie9:

        attributes:
          site: baie9

        resources:
          host-service:
            attributes:
              host.names: [ tallinn, bacon, powerscale, purex-san, purem-san, xtremio, oracle-zfs-7320, pure-san, vm-users-01]
            protocols:
              ping:
                timeout:  3  
  ```