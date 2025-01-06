keywords: ipmi, cli
description: The MetricsHub IPMI CLI provides a command-line interface for MetricsHub's core IPMI client. It enables to efficiently troubleshoot and run IPMI requests against monitored resources directly from the command line.

# IPMI CLI Documentation

IPMI (Intelligent Platform Management Interface) is a protocol used for managing servers independently of the operating system. It enables out-of-band monitoring, diagnostics, and management, providing administrators with access to system health, event logs, and power control.
The IPMI CLI allows users to execute IPMI-over-LAN queries to monitor and manage remote servers. It supports authentication via username/password and BMC keys.

## Syntax

```bash
ipmi <HOSTNAME> --username <USERNAME> --password <PASSWORD> --bmc-key <KEY> --timeout <TIMEOUT> --skip-auth <BOOLEAN>
```

## Options
| Option        | Description                                                                    | Default Value |
| ------------- | ------------------------------------------------------------------------------ | ------------- |
| `HOSTNAME`    | Hostname or IP address of the system to monitor.                               | None          |
| `--username`  | Username for IPMI-over-LAN authentication.                                     | None          |
| `--password`  | Password for IPMI-over-LAN authentication. If omitted, prompted interactively. | None          |
| `--bmc-key`   | BMC key for two-key authentication in hexadecimal format.                      | None          |
| `--skip-auth` | Skips authentication for IPMI queries if set to `true`.                        | `false`       |
| `--timeout`   | Timeout for IPMI operations in seconds.                                        | `120`         |
| `-v`          | Enables verbose mode. Repeat to increase verbosity (e.g., `-vv`).              | None          |
| `-h, --help`  | Displays help information for the IPMI CLI.                                    | None          |

## Examples

### Example 1: Basic IPMI Query with Authentication

```bash
ipmi dev-01 --username admin --password secret --timeout 60
```

### Example 2: IPMI Query with BMC Key

```bash
ipmi dev-01 --username admin --password secret --bmc-key AE4C7AB47FD --timeout 120
```

### Example 3: Skip Authentication

```bash
ipmi dev-01 --skip-auth true --timeout 90
```

### Example 4: Interactive Password Input

```bash
ipmi dev-01 --username admin
```
The CLI prompts for the password if it is not provided.