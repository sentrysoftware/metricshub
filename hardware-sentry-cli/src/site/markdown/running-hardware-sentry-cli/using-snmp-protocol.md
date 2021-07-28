keywords: Hardware Sentry CLI, SNMP Protocol, configuration options
description: Hardware Sentry CLI: Configuration options when using the SNMP protocol.

# Using the SNMP Protocol

Use the options below to configure the SNMP protocol:

| Option                   | Description                                                                                                 | Available Values                                                                   | Default Value |
| ------------------------ | ----------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------- | ------------- |
| `--snmp-version`         | Version of the SNMP protocol that **${project.name}** must use to retrieve information from the SNMP agent. | <ul><li>V1</li><li>V2C</li><li>V3_NO_AUTH</li><li>V3_MD5</li><li>V3_SHA</li> </ul> | V1            |
| `--snmp-port`            | SNMP port to be used to perform SNMP queries .                                                              |                                                                                    | 161           |
| `--snmp-community`       | SNMP community to be used to perform SNMP queries.                                                          |                                                                                    | public        |
| `--snmp-timeout`         | Number of seconds **${project.name}** will wait for an SNMP response.                                       |                                                                                    | 120           |
| `--snmp-username`        | _(When SNMP Version is V3_MD5 or V3-SHA)_ Username to be used to perform the SNMP queries.                  |                                                                                    |               |
| `--snmp-password`        | _(When SNMP Version is V3_MD5 or V3-SHA)_ Password to be used to perform the SNMP queries.                  |                                                                                    |               |
| `--snmp-privacy`         | _(When SNMP Version is V3_MD5 or V3-SHA)_ Encryption algorithm to be used to perform the SNMP queries.      | <ul><li>AES</li><li>DES</li><li>NO_ENCRYPTION </li> </ul>                          |               |
| `--snmp-privacyPassword` | _(When SNMP Version is V3_MD5 or V3-SHA)_ Privacy password to be used to perform the SNMP queries.          |
