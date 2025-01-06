keywords: data, troubleshooting, collection, resource
description: How to resolve issues where MetricsHub fails to collect data for a specific resource.

# No Data for a Specific Resource

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

**MetricsHub** extracts metrics from resources based on the [protocols and credentials](../configuration/configure-monitoring.md#protocols-and-credentials) defined in the `config/metricshub.yaml` file. If a connectivity issue occurs between **MetricsHub** and the configured resource, data collection will fail.

To troubleshoot this issue, test the connection to the resource using the CLI associated with the protocol specified in `config/metricshub.yaml`. Use the credentials configured in the file. If the CLI returns:

* **An error**: Verify that the credentials and protocol settings in the configuration file are correct
* **A successful output**: Confirm that the query retrieves the expected data.

For detailed guidance, refer to the protocol-specific CLI documentation:

- **[HTTP](cli/http.md)**: Facilitates troubleshooting and testing of HTTP-based resources using GET, POST, PUT, or DELETE methods.
- **[IPMI](cli/ipmi.md)**: Enables communication with hardware components via IPMI-over-LAN.
- **[JDBC](cli/jdbc.md)**: Allows direct interaction with relational databases to test SQL queries and database connectivity.
- **[Ping](cli/ping.md)**: Verifies the reachability of network hosts using ICMP ping requests and measures response times.
- **[SNMP](cli/snmp.md)**: Supports running SNMP v1 and v2c requests.
- **[SNMPv3](cli/snmpv3.md)**: Provides secure SNMP communication with support for authentication and encryption.
- **[SSH](cli/ssh.md)**: Executes commands on remote systems over a secure SSH connection.
- **[WBEM](cli/wbem.md)**: Executes CIM-compliant queries on WBEM-enabled systems.
- **[WinRm](cli/winrm.md)**: Facilitates querying and management of Windows systems using the WS-Management protocol.
- **[WMI](cli/wmi.md)**: Executes WMI queries to extract detailed information from Windows-based systems.

Each CLI simplifies the testing and troubleshooting process, helping administrators quickly diagnose and resolve issues with monitored resources.