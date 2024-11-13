keywords: snmp
description: How to configure MetricsHub to monitor network interfaces using the SNMP protocol.

# Monitoring network interfaces using SNMP

## Overview

**MetricsHub** can leverage the SNMP protocol (v1, v2c, or v3) to monitor network devices such as [Ethernet switches, UPS, or network interfaces](https://metricshub.com/docs/latest/connectors/tags/network.html), provided that an SNMP agent is installed on the resource and the connection is configured.

In the example below:
* we configured **MetricsHub** to monitor a switch using SNMP v2c
* we displayed the total traffic in bytes/s in a Grafana Dashboard

![MetricsHub - Monitoring network interfaces using SNMP](../images/metricshub-network-monitoring-snmp.png)


## Procedure

To monitor our switch:

1. In the `config/metricshub.yaml` file, we create the resource `alcatel-switch` with the following attributes:

   * hostname: `alcatel-switch`
   * host type: `oob`

    ```yaml
    alcatel-switch:
        attributes: 
          host.name: alcatel-switch
          host.type: oob
    ```
2.  We configure **MetricsHub** to connect to the network switch using the SNMP `v2c` protocol and the `public` community

   ```yaml
        protocols:
          snmp:
            version: v2c 
            community: public 
  ```

Here is the complete YAML configuration to be added to `config/metricshub.yaml` to monitor the switch:

   ```yaml
    alcatel-switch:
        attributes: 
          host.name: alcatel-switch
          host.type: oob
        protocols:
          snmp:
            version: v2c 
            community: public 
  ```