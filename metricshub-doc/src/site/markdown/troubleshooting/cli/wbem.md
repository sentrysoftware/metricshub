keywords: wbem, cli
description: The MetricsHub WBEM CLI provides a command-line interface for MetricsHub's core WBEM client. It enables to efficiently troubleshoot and run WBEM requests against monitored resources directly from the command line.

# WBEM CLI Documentation

WBEM (Web-Based Enterprise Management) is a set of standards for managing and interoperating with systems, devices, and applications across a network. It uses protocols like CIM-XML over HTTP/HTTPS for communication with WBEM-compliant systems.
The WBEM CLI in MetricsHub enables executing WBEM queries on WBEM-enabled devices or systems. It supports querying namespaces, retrieving specific information, and accessing vCenter-hosted resources.

## Syntax

```bash
wbem <HOSTNAME> --namespace <NAMESPACE> --query <QUERY> --username <USERNAME> --password <PASSWORD> --vcenter <VCENTER> --transport <PROTOCOL> --port <PORT> --timeout <TIMEOUT>
```

## Options

| Option        | Description                                                           | Default Value     |
| ------------- | --------------------------------------------------------------------- | ----------------- |
| `HOSTNAME`    | Hostname or IP address of the WBEM-enabled device.                    | None (required)   |
| `--query`     | WBEM query to execute.                                                | None (required)   |
| `--transport` | Transport protocol for WBEM: `HTTP` or `HTTPS`.                       | `HTTPS`           |
| `--port`      | Port of the WBEM server. Defaults: `5988` for HTTP, `5989` for HTTPS. | Based on protocol |
| `--username`  | Username for WBEM authentication.                                     | None              |
| `--password`  | Password for WBEM authentication. If omitted, prompted interactively. | None              |
| `--timeout`   | Timeout in seconds for WBEM operations.                               | 30                |
| `--namespace` | WBEM namespace for the query.                                         | None (required)   |
| `--vcenter`   | VCenter hostname providing the authentication ticket (if applicable). | None              |
| `-v`          | Enables verbose mode. Repeat to increase verbosity (e.g., `-vv`).     | None              |
| `-h, --help`  | Displays help information for the WBEM CLI.                           | None              |

## Examples

### Example 1: Basic WBEM Query

```bash
wbem esx-01 --namespace "root/cimv2" --query "SELECT MajorVersion FROM VMware_HypervisorSoftwareIdentity" \
--username admin --password secret --vcenter hci-vcenter
```

### Example 2: Query EMC SAN Namespace

```bash
wbem emc-san --namespace "root/emc" --query "SELECT DeviceID FROM EMC_DiskDrive" \
--transport https --username admin --password secret
```

### Example 3: Interactive Password Input

```bash
wbem esx-01 --namespace "root/cimv2" --query "SELECT * FROM CIM_ManagedElement" --username admin
```
The CLI prompts for the password if not provided.