keywords: http, cli
description: The MetricsHub HTTP CLI provides a command-line interface for MetricsHub's core HTTP client. It enables to efficiently troubleshoot and run HTTP requests against monitored resources directly from the command line.

# HTTP CLI Documentation

HTTP (Hypertext Transfer Protocol) is the foundation of data communication on the web. It is used for transferring requests and responses between clients and servers. In MetricsHub, the HTTP CLI facilitates HTTP-based interactions to monitor resources, query data, or send configuration updates.
The HTTP CLI is a command-line utility that allows users to send HTTP requests, such as GET, POST, PUT, and DELETE, to specified endpoints. It supports authentication, custom headers, and request body configurations.

## Syntax

```bash
http --method <GET|POST|PUT|DELETE> --url <URL> --username <USERNAME> --password <PASSWORD> [--body <BODY> | --body-file <FILE PATH>] [--header <HEADER> | --header-file <FILE PATH>] --timeout <TIMEOUT>
```

## Options

| Option          | Description                                                           | Default Value |
| --------------- | --------------------------------------------------------------------- | ------------- |
| `--url`         | The URL for the HTTP request.                                         | None          |
| `--method`      | HTTP method to use (`GET`, `POST`, `PUT`, `DELETE`).                  | `GET`         |
| `--header`      | Custom headers for the request. Multiple headers can be added.        | None          |
| `--header-file` | Path to a file containing headers for the request.                    | None          |
| `--body`        | Request body as a string.                                             | None          |
| `--body-file`   | Path to a file containing the request body.                           | None          |
| `--username`    | Username for HTTP authentication.                                     | None          |
| `--password`    | Password for HTTP authentication. If omitted, prompted interactively. | None          |
| `--timeout`     | Timeout for the HTTP operation in seconds.                            | 120           |
| `-v`            | Enable verbose mode. Repeat to increase verbosity (e.g., `-vv`).      | None          |
| `-h, --help`    | Display help information.                                             | None          |

## Examples

### Example 1: HTTP GET Request with Headers and Body

```bash
http --method get --url https://dev-01:443/users --username username --password password --header="Content-Type:application/xml" --header="Accept:application/json" --body="<aaaLogin inName='username' inPassword='password' />" --timeout 120
```

### Example 2: HTTP POST Request with Header and Body Files

```bash
http --method post --url https://dev-01:443/users --username admin --password pass --header-file="/opt/metricshub/header.txt" --body-file="/opt/metricshub/body.txt" --timeout 120
```

### Example 3: Interactive Password Input

```bash
http --method get --url https://example.com --username user
```
The CLI prompts for a password if not provided.