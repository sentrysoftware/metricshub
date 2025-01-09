keywords: jdbc, cli
description:  How to execute SQL queries against relational databases with MetricsHub JDBC CLI.

# JDBC CLI Documentation

JDBC (Java Database Connectivity) is a standard Java API for accessing and managing relational databases. It can be used to execute SQL queries and retrieve results programmatically. Refer to [Java Database Connectivity](https://en.wikipedia.org/wiki/Java_Database_Connectivity) for more details.

The **MetricsHub JDBC CLI** allows users to connect to databases, execute queries, and retrieve data or perform updates. It supports JDBC-compliant URLs, authentication, and configurable timeouts.

## Syntax

```bash
jdbc <HOSTNAME> --username <USERNAME> --password <PASSWORD> --url <JDBC URL> --query <SQL QUERY>
```

## Options

| Option       | Description                                                                            | Default Value |
|--------------|----------------------------------------------------------------------------------------|---------------|
| `HOSTNAME`   | Hostname or IP address of the database server.                                         | None          |
| `--url`      | The JDBC URL for the database connection. **This option is required.**                 | None          |
| `--username` | Username for JDBC authentication.                                                      | None          |
| `--password` | Password for JDBC authentication. If not provided, you will be prompted interactively. | None          |
| `--query`    | The SQL query to execute. **This option is required.**                                 | None          |
| `--timeout`  | Timeout in seconds for SQL query execution.                                            | 30            |
| `-v`         | Enables verbose mode. Use `-v` for basic logs, `-vv` for detailed logs.                | None          |
| `-h, --help` | Displays detailed help information about available options.                            | None          |

## Examples

### Example 1: Basic SQL Query

```bash
jdbc dev-01 --username admin --password secret --url="jdbc:mysql://dev-01:3306/MyDb" --query="SELECT * FROM users"
```

### Example 2: Basic SQL Query with Interactive Password Input

```bash
jdbc dev-01 --username admin --url="jdbc:postgresql://dev-01:5432/MyDb" --query="SELECT * FROM employees"
```

The CLI prompts for the password if not provided.

### Example 3: Basic SQL Query with Timeout Configuration

```bash
jdbc dev-01 --username admin --password secret --url="jdbc:oracle:thin:@dev-01:1521/MyDb" --query="UPDATE accounts SET balance = balance - 100" --timeout 60
```
