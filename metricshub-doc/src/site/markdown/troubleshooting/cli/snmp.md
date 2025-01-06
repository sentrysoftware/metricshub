keywords: snmp, cli
description: The MetricsHub SNMP CLI provides a command-line interface for MetricsHub's core SNMP client. It enables to efficiently troubleshoot and run SNMP v1 and v2c requests against monitored resources directly from the command line.

# SNMP CLI Documentation

SNMP (Simple Network Management Protocol) is a protocol used for monitoring and managing devices on IP networks. It enables retrieving and modifying device configuration or obtaining real-time information using Object Identifiers (OIDs).
The SNMP CLI in MetricsHub facilitates various SNMP queries, such as `GET`, `GETNEXT`, `WALK`, and `TABLE`, to interact with SNMP-enabled devices. It supports configuration of SNMP versions, community strings, ports, and retry intervals.

## Syntax
### SNMP Get Request

```bash
snmp <HOSTNAME> --get <OID> --community <COMMUNITY> --version <VERSION> --port <PORT> --timeout <TIMEOUT> --retry <INTERVAL1>,<INTERVAL2>,...
```

### SNMP Get Next Request

```bash
snmp <HOSTNAME> --getNext <OID> --community <COMMUNITY> --version <VERSION> --port <PORT> --timeout <TIMEOUT> --retry <INTERVAL1>,<INTERVAL2>,...
```

### SNMP Walk Request

```bash
snmp <HOSTNAME> --walk <OID> --community <COMMUNITY> --version <VERSION> --port <PORT> --timeout <TIMEOUT> --retry <INTERVAL1>,<INTERVAL2>,...
```

### SNMP Table Request

```bash
snmp <HOSTNAME> --table <OID> --columns <COLUMN,COLUMN,...> --community <COMMUNITY> --version <VERSION> --port <PORT> --timeout <TIMEOUT> --retry <INTERVAL1>,<INTERVAL2>,...
```

## Options

| Option        | Description                                                         | Default Value   |
| ------------- | ------------------------------------------------------------------- | --------------- |
| `HOSTNAME`    | Hostname or IP address of the SNMP-enabled device.                  | None (required) |
| `--version`   | SNMP protocol version to use: `1` or `2c`.                          | `v2c`           |
| `--community` | Community string for SNMP authentication.                           | `public`        |
| `--port`      | Port on which the SNMP agent is listening.                          | `161`           |
| `--timeout`   | Timeout in seconds for SNMP operations.                             | 5               |
| `--retry`     | Comma-separated retry intervals in milliseconds (e.g., `500,1000`). | None            |
| `--get`       | Object Identifier (OID) for SNMP Get request.                       | None            |
| `--getNext`   | OID for SNMP Get Next request.                                      | None            |
| `--walk`      | OID for SNMP Walk request.                                          | None            |
| `--table`     | OID for SNMP Table request.                                         | None            |
| `--columns`   | Comma-separated columns for SNMP Table request.                     | None            |
| `-v`          | Enables verbose mode. Repeat to increase verbosity (e.g., `-vv`).   | None            |
| `-h, --help`  | Displays help information for the SNMP CLI.                         | None            |

## Examples

### Example 1: SNMP Get Request

```bash
snmp dev-01 --get 1.3.6.1.4.1.674.10892.5.5.1.20.130.4.1.1.1 --community public --version v2c --port 161 --timeout 60 --retry 500,1000
```

### Example 2: SNMP Get Next Request

```bash
snmp dev-01 --getNext 1.3.6.1.4.1.674.10892.5.5.1.20.130.4 --community public --version v2c --port 161 --timeout 60 --retry 500,1000
```

### Example 3: SNMP Walk Request

```bash
snmp dev-01 --walk 1.3.6.1 --community public --version v1 --port 161 --timeout 60 --retry 500,1000
```

### Example 4: SNMP Table Request

```bash
snmp dev-01 --table 1.3.6.1.4.1.674.10892.5.4.300.10.1 --columns 1,3,8,9,11 --community public --version v1 --port 161 --timeout 60 --retry 500,1000
```