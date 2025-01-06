keywords: winrm, cli
description: The MetricsHub WinRm CLI provides a command-line interface for MetricsHub's core WinRm client. It enables to efficiently troubleshoot and run WinRm requests against monitored resources directly from the command line.

# WinRM CLI Documentation

WinRM (Windows Remote Management) is a protocol that allows remote management and execution of commands on Windows systems. It is based on the WS-Management protocol and facilitates secure communication between systems for administrative tasks and data retrieval.
The WinRM CLI in MetricsHub enables execution of WinRM queries on remote systems. It supports querying namespaces, retrieving specific data, and authenticating using protocols like NTLM and Kerberos.

## Syntax

```bash
winrm <HOSTNAME> --username <USERNAME> --password <PASSWORD> --namespace <NAMESPACE> --query <QUERY> --transport <PROTOCOL> --port <PORT> --timeout <TIMEOUT> --authentications <AUTH1>,<AUTH2>,...
```

## Options

| Option              | Description                                                              | Default Value     |
| ------------------- | ------------------------------------------------------------------------ | ----------------- |
| `HOSTNAME`          | Hostname or IP address of the WinRM-enabled device.                      | None (required)   |
| `--transport`       | Transport protocol for WinRM: `HTTP` or `HTTPS`.                         | `HTTP`            |
| `--username`        | Username for WinRM authentication.                                       | None              |
| `--password`        | Password for WinRM authentication. If omitted, prompted interactively.   | None              |
| `--port`            | Port for the WinRM service. Defaults: `5985` for HTTP, `5986` for HTTPS. | Based on protocol |
| `--timeout`         | Timeout in seconds for WinRM operations.                                 | 30                |
| `--authentications` | Comma-separated list of authentication schemes: `NTLM`, `KERBEROS`.      | `NTLM`            |
| `--query`           | WinRM query to execute.                                                  | None (required)   |
| `--namespace`       | Namespace for the query.                                                 | None (required)   |
| `-v`                | Enables verbose mode. Repeat to increase verbosity (e.g., `-vv`).        | None              |
| `-h, --help`        | Displays help information for the WinRM CLI.                             | None              |

## Examples

### Example 1: Basic WinRM Query

```bash
winrm dev-01 --username admin --password secret --namespace "root/cimv2" --query "SELECT * FROM Win32_OperatingSystem" \
--transport https --port 5986 --timeout 30 --authentications NTLM
```

### Example 2: Multiple Authentication Schemes

```bash
winrm dev-01 --username admin --password secret --namespace "root/cimv2" --query "SELECT * FROM Win32_LogicalDisk" \
--transport http --port 5985 --timeout 60 --authentications NTLM,KERBEROS
```

### Example 3: Interactive Password Input

```bash
winrm dev-01 --username admin --namespace "root/cimv2" --query "SELECT * FROM CIM_ManagedElement"
```
The CLI prompts for the password if not provided.