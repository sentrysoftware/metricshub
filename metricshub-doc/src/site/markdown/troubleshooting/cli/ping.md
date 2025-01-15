keywords: ping, cli
description: How to execute ICMP ping requests with the MetricsHub Ping CLI.

# ICMP Ping CLI Documentation

The **MetricsHub Ping CLI** is a command-line utility you can use to execute ICMP ping requests against your resources to verify that they can be reached within an acceptable response time.

## Syntax

```bash
ping <HOSTNAME> --timeout <TIMEOUT>
```

## Options

| Option       | Description                                                              | Default Value |
| ------------ | ------------------------------------------------------------------------ | ------------- |
| `HOSTNAME`   | Hostname or IP address of the host to ping. **This option is required.** | None          |
| `--timeout`  | Timeout in seconds for the ICMP Ping operation.                          | 5             |
| `-v`         | Enables verbose mode. Use `-v` for basic logs, `-vv` for detailed logs.  | None          |
| `-h, --help` | Displays detailed help information about available options.              | None          |

## Examples

### Example 1: Basic ICMP Ping

```bash
ping dev-01
```

### Example 2: ICMP Ping with Custom Timeout

```bash
ping dev-01 --timeout 10
```

### Example 3: ICMP Ping with Debug Verbose Mode

```bash
ping dev-01 --timeout 5 -vvv
```
