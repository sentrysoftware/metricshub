keywords: Hardware Sentry CLI, commands, options
description: How to run the Hardware Sentry CLI: commands and options available.

# Running ${project.name}

To run the **${project.name}**, use one of these commands:

```shell script
   $ java -jar hardware-sentry-cli-<version>.jar -host <hostname> -dt <device-type> <protocol-configuration>
```
  
```shell script
   $ java -jar hardware-sentry-cli-<version>.jar --hostname <hostname> --device-type <device-type> <protocol-configuration>
```

where:

* `<version>` corresponds to the version of the **${project.name}**
* `<hostname>` corresponds to the name of the device, or its IP address
* `<device-type>` corresponds to the operating system or the type of the device to be monitored. Possible values are:

    * `HP_OPEN_VMS` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#hp-openvms" target="_blank">HP Open VMS systems</a>
    * `HP_TRU64_UNIX` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#hp-tru64" target="_blank">HP Tru64 systems</a>
    * `HP_UX` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#hp-ux" target="_blank">HP UX systems</a>
    * `IBM_AIX` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#ibm-aix" target="_blank">IBM AIX systems</a>
    * `LINUX` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#linux" target="_blank">Linux systems</a>
    * `MGMT_CARD_BLADE_ESXI` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#out-of-band" target="_blank">Out-of-band</a>, <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#blade-chassis" target="_blank">blade chassis</a>, and <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#vmware-esx" target="_blank">VMware ESX systems</a> 
    * `MS_WINDOWS` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#microsoft-windows" target="_blank">Microsoft Windows systems</a>
    * `NETWORK_SWITCH` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#network-device" target="_blank">network devices</a>
    * `STORAGE` for these <a href="https://www.sentrysoftware.com/library/hc/platform-requirements.html#storage-system" target="_blank">storage systems</a>
    * `SUN_SOLARIS` for these <a href="https://www.sentrysoftware.com/library/hc/24/platform-requirements.html#oracle-solaris" target="_blank">Oracle solaris systems</a>.

* `<protocol-configuration>` corresponds to the protocol the **${project.name}** will use to communicate with the hardware instrumentation layers to retrieve hardware information. To know how to set these different options, refer to:

    * [Using the HTTP Protocol](./using-http-protocol.html)
    * [Using the SNMP Protocol](./using-snmp-protocol.html)
    * [Using the WBEM Protocol](./using-wbem-protocol.html)
    * [Using the WMI Protocol](./using-wmi-protocol.html)