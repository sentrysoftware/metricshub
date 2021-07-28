keywords: Hardware Sentry CLI, WMI Protocol, configuration options
description: Hardware Sentry CLI: configuration options when using the WMI protocol.

# Using the WMI Protocol

Use the options below to configure the WMI protocol:

| Option         | Description                                                                               | Available Values | Default Value |
| -------------- | ----------------------------------------------------------------------------------------- | ---------------- | ------------- |
| -wmi-namespace | WMI namespace. <br> Leave blank to let the solution detect the proper namespace. </br>    |                  | root/cimv2    |
| -wmi-timeout   | Number of seconds **${project.name}** will wait for a WMI response.                       |                  | 120           |
| -wmi-username  | Username to be used to establish the connection with the device through the WMI protocol. |                  |               |
| -wmi-password  | Password to be used to establish the connection with the device through the WMI protocol. |
