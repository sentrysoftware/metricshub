keywords: wmi, cli
description: How to execute WMI queries on remote systems with MetricsHub WMI CLI.

# WMI CLI

The **MetricsHub WMI CLI** allows you to execute WMI queries on remote systems. It supports querying namespaces, retrieving system information, and managing resources.

## Syntax

```bash
wmi <HOSTNAME> --username <USERNAME> --password <PASSWORD> --namespace <NAMESPACE> --query <QUERY> --timeout <TIMEOUT>
```

## Options

| Option        | Description                                                                           | Default Value   |
| ------------- | ------------------------------------------------------------------------------------- | --------------- |
| `HOSTNAME`    | Hostname or IP address of the WMI-enabled device. **This option is required**         | None            |
| `--username`  | Username for WMI authentication.                                                      | None            |
| `--password`  | Password for WMI authentication. If not provided, you will be prompted interactively. | None            |
| `--timeout`   | Timeout in seconds for WMI operations.                                                | 30              |
| `--query`     | WMI query to execute.                                                                 | None (required) |
| `--namespace` | Namespace for the WMI query.                                                          | None (required) |
| `-v`          | Enables verbose mode. Use `-v` for basic logs, `-vv` for detailed logs.               | None            |
| `-h, --help`  | Displays detailed help information about available options.                           | None            |

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
