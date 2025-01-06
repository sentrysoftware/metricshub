keywords: wmi, cli
description: The MetricsHub WMI CLI provides a command-line interface for MetricsHub's core WMI client. It enables to efficiently troubleshoot and run WMI requests against Windows systems directly from the command line.

# WMI CLI 

WMI (Windows Management Instrumentation) is a Microsoft technology for managing and monitoring Windows-based systems. It enables querying system information and executing management tasks programmatically via namespaces and WQL (WMI Query Language).
The WMI CLI in MetricsHub facilitates execution of WMI queries on remote systems. It supports querying namespaces, retrieving system information, and managing resources.

## Syntax

```bash
wmi <HOSTNAME> --username <USERNAME> --password <PASSWORD> --namespace <NAMESPACE> --query <QUERY> --timeout <TIMEOUT>
```

## Options
| Option        | Description                                                          | Default Value   |
| ------------- | -------------------------------------------------------------------- | --------------- |
| `HOSTNAME`    | Hostname or IP address of the WMI-enabled device.                    | None (required) |
| `--username`  | Username for WMI authentication.                                     | None            |
| `--password`  | Password for WMI authentication. If omitted, prompted interactively. | None            |
| `--timeout`   | Timeout in seconds for WMI operations.                               | 30              |
| `--query`     | WMI query to execute.                                                | None (required) |
| `--namespace` | Namespace for the WMI query.                                         | None (required) |
| `-v`          | Enables verbose mode. Repeat to increase verbosity (e.g., `-vv`).    | None            |
| `-h, --help`  | Displays help information for the WMI CLI.                           | None            |

## Examples

### Example 1: Basic WMI Query

```bash
wmi dev-01 --username admin --password secret --namespace "root/cimv2" --query "SELECT * FROM Win32_OperatingSystem" --timeout 30
```

### Example 2: Interactive Password Input

```bash
wmi dev-01 --username admin --namespace "root/cimv2" --query "SELECT * FROM CIM_ManagedElement"
```
The CLI prompts for the password if not provided.