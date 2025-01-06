keywords: ping, cli
description: The MetricsHub Ping CLI provides a command-line interface for MetricsHub's core Ping client. It enables to efficiently troubleshoot and ping monitored resources directly from the command line.

# ICMP Ping CLI Documentation

ICMP (Internet Control Message Protocol) is used for diagnostic and error-reporting purposes in network communications. Ping is an application of ICMP that tests the reachability of a host by sending echo request messages and measuring the time it takes for responses.
The Ping CLI allows users to execute ICMP ping requests to test the reachability and response time of a network host.

## Syntax

```bash
ping <HOSTNAME> --timeout <TIMEOUT>
```

## Options

| Option       | Description                                                       | Default Value   |
| ------------ | ----------------------------------------------------------------- | --------------- |
| `HOSTNAME`   | Hostname or IP address of the host to ping.                       | None (required) |
| `--timeout`  | Timeout in seconds for the ICMP Ping operation.                   | 5               |
| `-v`         | Enables verbose mode. Repeat to increase verbosity (e.g., `-vv`). | None            |
| `-h, --help` | Displays help information for the Ping CLI.                       | None            |

## Examples

### Example 1: Basic ICMP Ping

```bash
ping dev-01
```

### Example 2: ICMP Ping with Custom Timeout

```bash
ping dev-01 --timeout 10
```

### Example 3: Debug Verbose Mode

```bash
ping dev-01 --timeout 5 -vvv
```