keywords: release notes
description: Learn more about the new features, changes and improvements, and bug fixes brought to MetricsHub Enterprise.

# Release Notes

## MetricsHub Enterprise Edition v1.0.01

### MetricsHub Enterprise Edition v1.0.01

#### Changes and Improvements

| ID           | Description                                                                                                              |
| ------------ | ------------------------------------------------------------------------------------------------------------------------ |
| **M8BEE-29** | Removed Otel collector resourcedetection processor to prevent localhost system resource attributes from being overridden |

### MetricsHub Enterprise Connectors v101

#### Changes and Improvements

| ID       | Description                                                    |
| -------- | -------------------------------------------------------------- |
| **EC-9** | The hw.network.bandwidth.limit metric is now reported in bytes |

#### Fixed issues

| ID        | Description                                                            |
| --------- | ---------------------------------------------------------------------- |
| **EC-73** | Dell iDRAC9 (REST): Some network link physical addresses are incorrect |

### MetricsHub Community Edition v0.9.07

#### Changes and Improvements

| ID                                                                   | Description                                                                                                                |
| -------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| **[\#433](https://github.com/sentrysoftware/metricshub/issues/433)** | [BREAKING_CHANGE] Disabled Automatic Hostname to FQDN resolution                                                           |
| **[\#427](https://github.com/sentrysoftware/metricshub/issues/427)** | BMC Helix Integration: Added the \`StatusInformation\` internal text parameter to the connector monitor                    |
| **[\#421](https://github.com/sentrysoftware/metricshub/issues/421)** | Reduced Alert noise for \`hw.status{state="present"}\`                                                                     |
| **[\#414](https://github.com/sentrysoftware/metricshub/issues/414)** | Added a link to MetricsHub Community Connectors 1.0.06                                                                     |
| **[\#412](https://github.com/sentrysoftware/metricshub/issues/412)** | The \`hw.status{state="present"}\` metric is no longer reported for cpu monitors discovered by Linux and Window connectors |
| **[\#383](https://github.com/sentrysoftware/metricshub/issues/383)** | Implemented a new engine method \`megaBit2Byte\` to align with OpenTelemetry unit standards                                |
| **[\#374](https://github.com/sentrysoftware/metricshub/issues/374)** | Default connector variables can now be specified in YAML connector files                                                   |
| **[\#302](https://github.com/sentrysoftware/metricshub/issues/302)** | Defined \`afterAll\` and \`beforeAll\` jobs in YAML connectors                                                             |

#### Fixed issues

| ID                                                                   | Description                                                                      |
| -------------------------------------------------------------------- | -------------------------------------------------------------------------------- |
| [**\#436**](https://github.com/sentrysoftware/metricshub/issues/436) | The log message for SNMP v3 credential validation is incorrect                   |
| [**\#439**](https://github.com/sentrysoftware/metricshub/issues/439) | Connector default variables are not serializable                                 |
| [**\#417**](https://github.com/sentrysoftware/metricshub/issues/417) | JavaDoc references are incorrect                                                 |
| [**\#410**](https://github.com/sentrysoftware/metricshub/issues/410) | Protocol definition is applied to only one host in a multiple-host configuration |
| [**\#368**](https://github.com/sentrysoftware/metricshub/issues/368) | The \`hw.power{hw.type="vm"}\` metric is erroneously set to 0                    |

### MetricsHub Community Connectors v1.0.06

#### Changes and Improvements

| ID                                                                                        | Description                                                                                                                 |
| ----------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------- |
| [**\#125**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/125) | Disabled automatic detection for WindowsProcess, WindowsService, and LinuxService                                           |
| [**\#122**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/122) | Added default values for connector variables in \`WindowsService\`, \`LinuxService\`, \`WindowsProcess\` & \`LinuxProcess\` |
| [**\#114**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/114) | The \`hw.network.bandwidth.limit\` metric is now displayed in bytes                                                         |

#### Fixed issues

| ID                                                                                        | Description                                                                |
| ----------------------------------------------------------------------------------------- | -------------------------------------------------------------------------- |
| [**\#120**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/120) | The \`hw.vm.power_ratio\` unit is incorrect. It should be 1 instead of Cel |
