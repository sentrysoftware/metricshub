keywords: Hardware Sentry CLI, WBEM Protocol, configuration options
description: Hardware Sentry CLI: Configuration options when using the WBEM protocol.

# Using the WBEM Protocol

Use the options below to configure the WBEM protocol:

| Option          | Description                                                                                | Available Values                                                                            | Default Value |
| --------------- | ------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------- | ------------- |
| -wbem-protocol  | Protocol to be used to establish the connection with the device through the WBEM protocol. | <ul><li>https</li><li>http</li></ul>                                                        | https         |
| -wbem-port      | Port to be used to perform WBEM queries.                                                   | <ul><li>5989 for encrypted connections</li><li>5988 for non-encrypted connections</li></ul> | 5989          |
| -wbem-namespace | WBEM namespace. <br> Leave blank to let the solution detect the proper namespace.</br>     |                                                                                             | root/cimv2    |
| -wbem-timeout   | Number of seconds **${project.name}** will wait for a WBEM response.                       |                                                                                             | 120           |
| -wbem-username  | Username to be used to establish the connection with the device through the WBEM protocol. |                                                                                             |               |
| -wbem-password  | Password to be used to establish the connection with the device through the WBEM protocol. |                                                                                             |               |
