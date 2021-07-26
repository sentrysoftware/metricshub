keywords: Hardware Sentry CLI, HTTP Protocol, configuration options
description: Hardware Sentry CLI: Configuration options when using the HTTP protocol.

# Using the HTTP Protocol

Use the options below to configure the HTTP protocol:
  
| Option          | Description                     | Available Values | Default Value |
|-----------------|---------------------------------|------------------|---------------|
| --http          |This option takes no argument. <br> Use http instead of https.</br>     |                  | false         |
| --http-port     | Port to be used to perform HTTP requests.               |                  | 443           | 
| --http-timeout  | Number of seconds **${project.name}** will wait for an HTTP response. |                  | 120           |
| --http-username | Username to be used to perform HTTP requests.               |              |                   |
| --http-password | Password to be used to perform HTTP requests.             |                  |               |
