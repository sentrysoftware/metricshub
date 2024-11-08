keywords: snmp
description: How to configure MetricsHub to monitor network interfaces using the SNMP protocol.

# Monitoring network interfaces using SNMP

## Overview

**MetricsHub** can leverage the SNMP protocol (v1, v2c, or v3) to monitor network devices such as [Ethernet switches, UPS, or network interfaces](../metricshub-connectors-directory.html), provided that an SNMP agent is installed on the resource and the connection is configured.

In the example below, we configured **MetricsHub** to monitor Lenovo xSeries systems using:
* SNMP v2
* the community connector [MIB-2 Standard SNMP Agent - Network Interfaces](https://sentrysoftware.org/metricshub-community-connectors/connectors/mib2.html)
* the Enterprise Connector [LenovoIMM (SNMP)](https://metricshub.com/docs/latest/connectors/lenovoimm.html).


## Procedure

To monitor our Lenoxo xSeries systems:

1. In the `config/metricshub.yaml` file, we created the resource `Lenovo-xSeries-SNMPv2` with the following attributes:

   * hostname: `lenonoxSeries.domain.com`
   * host type: `oob`

    ```yaml
        Lenovo-xSeries-SNMPv2:
            attributes: 
            host.name: lenonoxSeries.domain.com 
            host.type: oob
    ```
2.  We forced **MetricsHub** to use the `LenovoIMM` and `MIB2` connectors

    ```yaml
            connectors: [LenovoIMM, MIB2]
    ```

3. We configured **MetricsHub** to leverage the SNMP v2c protocol using the `public` community

    ```yaml

            protocols:
            snmp:
                hostname:  <hostname or IP address of IMM>
                version: v2c
                community: public
    ```

We came up to this version of the `config/metricshub.yaml` configuration file:

 ```yaml
        Lenovo-xSeries-SNMPv2:
            attributes: 
            host.name: lenonoxSeries.domain.com 
            host.type: mgmt
            connectors: [LenovoIMM, MIB2]
            protocols:
            snmp:
                hostname:  <hostname or IP address of IMM>
                version: v2c
                community: public
```