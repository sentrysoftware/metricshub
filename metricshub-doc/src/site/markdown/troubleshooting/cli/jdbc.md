keywords: jdbc, cli
description: The MetricsHub JDBC CLI provides a command-line interface for MetricsHub's core JDBC client. It enables to efficiently troubleshoot and run SQL requests against monitored databases directly from the command line.

# JDBC CLI Documentation

JDBC (Java Database Connectivity) is a standard Java API for accessing and managing relational databases. It enables executing SQL queries and retrieving results programmatically. In MetricsHub, the JDBC CLI allows users to connect to databases, execute queries, and retrieve data or perform updates.
The JDBC CLI facilitates interaction with relational databases through SQL queries. It supports JDBC-compliant URLs, authentication, and configurable timeouts.

## Syntax

```bash
jdbc <HOSTNAME> --username <USERNAME> --password <PASSWORD> --url <JDBC URL> --query <SQL QUERY>
```

## Options
| Option       | Description                                                           | Default Value   |
| ------------ | --------------------------------------------------------------------- | --------------- |
| `HOSTNAME`   | Hostname or IP address of the database server.                        | None            |
| `--url`      | The JDBC URL for the database connection.                             | None (required) |
| `--username` | Username for JDBC authentication.                                     | None            |
| `--password` | Password for JDBC authentication. If omitted, prompted interactively. | None            |
| `--query`    | The SQL query to execute.                                             | None (required) |
| `--timeout`  | Timeout for SQL query execution in seconds.                           | 30              |
| `-v`         | Enables verbose mode. Repeat to increase verbosity (e.g., `-vv`).     | None            |
| `-h, --help` | Displays help information for the JDBC CLI.                           | None            |

## Examples

### Example 1: Execute a Basic SQL Query

```bash
jdbc dev-01 --username admin --password secret --url="jdbc:mysql://dev-01:3306/MyDb" --query="SELECT * FROM users"
```

### Example 2: Interactive Password Input

```bash
jdbc dev-01 --username admin --url="jdbc:postgresql://dev-01:5432/MyDb" --query="SELECT * FROM employees"
```
The CLI prompts for the password if not provided.

### Example 3: Timeout Configuration

```bash
jdbc dev-01 --username admin --password secret --url="jdbc:oracle:thin:@dev-01:1521/MyDb" --query="UPDATE accounts SET balance = balance - 100" --timeout 60
```