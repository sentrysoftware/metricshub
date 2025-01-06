keywords: snmpv3, cli
description: The MetricsHub SNMPv3 CLI provides a command-line interface for MetricsHub's core SNMPv3 client. It enables to efficiently troubleshoot and run SNMP v3 requests against monitored resources directly from the command line.

# SNMPv3 CLI Documentation

SNMPv3 (Simple Network Management Protocol Version 3) is an enhanced version of SNMP, providing secure communication with features like encryption, authentication, and access control. It is widely used for monitoring and managing devices in a secure environment.
The SNMPv3 CLI in MetricsHub facilitates secure interaction with SNMPv3-enabled devices. It supports queries such as `GET`, `GETNEXT`, `WALK`, and `TABLE`, with configurable options for authentication, encryption, and context.

## Syntax
### SNMPv3 Get Request

```bash
snmpv3 <HOSTNAME> --get <OID> --privacy <DES|AES> --privacy-password <PASSWORD> --auth <SHA|MD5> --username <USERNAME> --password <PASSWORD> --context-name <CONTEXT> --timeout <TIMEOUT> --retry <INTERVAL1>,<INTERVAL2>,...
```

### SNMPv3 Get Next Request

```bash
snmpv3 <HOSTNAME> --getNext <OID> --privacy <DES|AES> --privacy-password <PASSWORD> --auth <SHA|MD5> --username <USERNAME> --password <PASSWORD> --context-name <CONTEXT> --timeout <TIMEOUT> --retry <INTERVAL1>,<INTERVAL2>,...
```

### SNMPv3 Walk Request

```bash
snmpv3 <HOSTNAME> --walk <OID> --privacy <DES|AES> --privacy-password <PASSWORD> --auth <SHA|MD5> --username <USERNAME> --password <PASSWORD> --context-name <CONTEXT> --timeout <TIMEOUT> --retry <INTERVAL1>,<INTERVAL2>,...
```

### SNMPv3 Table Request

```bash
snmpv3 <HOSTNAME> --table <OID> --columns <COLUMN,COLUMN,...> --privacy <DES|AES> --privacy-password <PASSWORD> --auth <SHA|MD5> --username <USERNAME> --password <PASSWORD> --context-name <CONTEXT> --timeout <TIMEOUT> --retry <INTERVAL1>,<INTERVAL2>,...
```

## Options

| Option               | Description                                                             | Default Value   |
| -------------------- | ----------------------------------------------------------------------- | --------------- |
| `HOSTNAME`           | Hostname or IP address of the SNMPv3-enabled device.                    | None (required) |
| `--privacy`          | Encryption type: `DES`, `AES`, or `none`.                               | None            |
| `--privacy-password` | Password for SNMPv3 encryption.                                         | None            |
| `--auth`             | Authentication type: `SHA`, `MD5`, or `NO_AUTH`.                        | None            |
| `--username`         | Username for SNMPv3 authentication.                                     | None            |
| `--password`         | Password for SNMPv3 authentication. If omitted, prompted interactively. | None            |
| `--context-name`     | Context name for SNMPv3.                                                | None            |
| `--timeout`          | Timeout in seconds for SNMPv3 operations.                               | 5               |
| `--port`             | Port of the SNMPv3 agent.                                               | 161             |
| `--retry`            | Comma-separated retry intervals in milliseconds (e.g., `500,1000`).     | None            |
| `--get`              | OID for SNMPv3 Get request.                                             | None            |
| `--getNext`          | OID for SNMPv3 Get Next request.                                        | None            |
| `--walk`             | OID for SNMPv3 Walk request.                                            | None            |
| `--table`            | OID for SNMPv3 Table request.                                           | None            |
| `--columns`          | Comma-separated columns for SNMPv3 Table request.                       | None            |
| `-v`                 | Enables verbose mode. Repeat to increase verbosity (e.g., `-vv`).       | None            |
| `-h, --help`         | Displays help information for the SNMPv3 CLI.                           | None            |

## Examples

### Example 1: SNMPv3 Get Request

```bash
snmpv3 dev-01 --get 1.3.6.1.4.1.674.10892.5.5.1.20.130.4.1.1.1 --privacy AES --privacy-password privacyPassword --auth MD5 --username admin --password secret --context-name context --timeout 120 --retry 500,1000
```

### Example 2: SNMPv3 Get Next Request

```bash
snmpv3 dev-01 --getNext 1.3.6.1.4.1.674.10892.5.5.1.20.130.4 --privacy AES --privacy-password privacyPassword --auth SHA --username admin --password secret --context-name context --timeout 120 --retry 500,1000
```

### Example 3: SNMPv3 Walk Request

```bash
snmpv3 dev-01 --walk 1.3.6.1 --privacy DES --privacy-password privacyPassword --auth SHA --username admin --password secret --context-name context --timeout 120 --retry 500,1000
```

### Example 4: SNMPv3 Table Request

```bash
snmpv3 dev-01 --table 1.3.6.1.4.1.674.10892.5.4.300.10.1 --columns 1,3,8,9,11 --privacy AES --privacy-password privacyPassword --auth MD5 --username admin --password secret --context-name context --timeout 120 --retry 500,1000
```