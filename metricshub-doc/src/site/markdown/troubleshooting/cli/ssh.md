keywords: ssh, cli
description: The MetricsHub SSH CLI provides a command-line interface for MetricsHub's core SSH client. It enables to efficiently troubleshoot and run SSH requests against monitored resources directly from the command line.

# SSH CLI Documentation

SSH (Secure Shell) is a protocol used for securely accessing and managing devices over a network. It provides secure remote login and command execution, making it ideal for configuration management, monitoring, and troubleshooting.
The SSH CLI in MetricsHub facilitates executing remote commands on SSH-enabled devices. It supports authentication via username/password or public key, with options to configure timeout and port settings.

## Syntax
### SSH with Username and Password

```bash
ssh <HOSTNAME> --username <USERNAME> --password <PASSWORD> --command <COMMAND> --timeout <TIMEOUT> --port <PORT>
```

### SSH with Public Key Authentication

```bash
ssh <HOSTNAME> --public-key <PATH> --command <COMMAND> --timeout <TIMEOUT> --port <PORT>
```

## Options

| Option         | Description                                                          | Default Value   |
| -------------- | -------------------------------------------------------------------- | --------------- |
| `HOSTNAME`     | Hostname or IP address of the SSH-enabled device.                    | None (required) |
| `--username`   | Username for SSH authentication.                                     | None            |
| `--password`   | Password for SSH authentication. If omitted, prompted interactively. | None            |
| `--public-key` | Path to the public key file for SSH authentication.                  | None            |
| `--command`    | The command to execute on the remote device.                         | `sudo`          |
| `--timeout`    | Timeout in seconds for the SSH operation.                            | 30              |
| `--port`       | Port for the SSH connection.                                         | 22              |
| `-v`           | Enables verbose mode. Repeat to increase verbosity (e.g., `-vv`).    | None            |
| `-h, --help`   | Displays help information for the SSH CLI.                           | None            |

## Examples

### Example 1: Basic Authentication with Username and Password

```bash
ssh dev-01 --username admin --password secret --command="echo Hello, World!" --timeout 30 --port 22
```

### Example 2: Authentication with Public Key

```bash
ssh dev-01 --public-key="/opt/ssh-rsa.txt" --command="ls /home/admin" --timeout 30 --port 22
```

### Example 3: Interactive Password Input

```bash
ssh dev-01 --username admin --command="whoami"
```
The CLI prompts for the password if not provided.
