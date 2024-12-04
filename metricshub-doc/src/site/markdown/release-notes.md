keywords: release notes
description: Learn more about the new features, changes and improvements, and bug fixes brought to MetricsHub Enterprise.

# Release Notes

## MetricsHub Enterprise Edition v1.0.02

### MetricsHub Enterprise Edition v1.0.02

#### Changes and Improvements

| ID       | Description                                                                  |
| -------- | ---------------------------------------------------------------------------- |
| M8BEE-35 | Replace `loggingexporter` with `debugexporter` in `otel-config-example.yaml` |

### MetricsHub Enterprise Connectors v102

#### Changes and Improvements

| ID    | Description                                                            |
| ----- | ---------------------------------------------------------------------- |
| EC-87 | Add LedIndicator value to the `StatusInformation` of the `led` monitor |

#### Fixed issues

| ID    | Description                                                                                                        |
| ----- | ------------------------------------------------------------------------------------------------------------------ |
| EC-74 | HP Insight Management Agent - Drive Array: The `disk_controller` status is not reported                            |
| EC-77 | Redfish: Duplicate enclosures for Dell iDRAC and HP                                                                |
| EC-78 | Dell OpenManage Server Administrator: The `hw.enclosure.energy` metric is not converted to Joules                  |
| EC-79 | Dell XtremIO REST API: The `hw.parent.type` attribute is reported as `DiskController` instead of `disk_controller` |
| EC-93 | `high.critical` threshold is not collected by connectors that report voltage metrics                               |

### MetricsHub Community Edition v0.9.08

#### Changes and Improvements

| ID                                                                   | Description                                                                                      |
| -------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------ |
| [**\#379**](https://github.com/sentrysoftware/metricshub/issues/379) | Add support for escaped macros                                                                   |
| [**\#422**](https://github.com/sentrysoftware/metricshub/issues/422) | Develop a **JDBC** Extension to enable support for SQL-based connectors                          |
| [**\#432**](https://github.com/sentrysoftware/metricshub/issues/432) | Standardize the messages for all the criteria tests                                              |
| [**\#435**](https://github.com/sentrysoftware/metricshub/issues/435) | [BREAKING_CHANGE] Added support for multiple variable values for the same connector              |
| [**\#468**](https://github.com/sentrysoftware/metricshub/issues/468) | Support shared-characteristics for centralized resource configuration                            |
| [**\#470**](https://github.com/sentrysoftware/metricshub/issues/470) | Add support for `host.id`, `host.name`, and other attributes as arrays in resource configuration |
| [**\#472**](https://github.com/sentrysoftware/metricshub/issues/472) | Prevent sensitive configuration details from being displayed in error logs                       |
| [**\#474**](https://github.com/sentrysoftware/metricshub/issues/474) | Handle blank values when creating INSERT queries for `internalDbQuery` Sources                   |
| [**\#498**](https://github.com/sentrysoftware/metricshub/issues/498) | Use data from the TableSource when RawData is empty in **Jawk** Sources                          |

#### Fixed issues

| ID                                                                   | Description                                                                  |
| -------------------------------------------------------------------- | ---------------------------------------------------------------------------- |
| [**\#478**](https://github.com/sentrysoftware/metricshub/issues/478) | Correct `HttpCriterion` toString method                                      |
| [**\#480**](https://github.com/sentrysoftware/metricshub/issues/480) | IPMITool criteria and source failure due to bad `ipmitool` command           |
| [**\#500**](https://github.com/sentrysoftware/metricshub/issues/500) | Mono-Instance collect processes only one monitor due to incorrect indexation |
| [**\#502**](https://github.com/sentrysoftware/metricshub/issues/502) | Incorrect link status check leads to wrong power consumption                 |

#### Documentation updates

| ID                                                                   | Description                                                                                 |
| -------------------------------------------------------------------- | ------------------------------------------------------------------------------------------- |
| [**\#462**](https://github.com/sentrysoftware/metricshub/issues/462) | Review **Configure Monitoring** documentation                                               |
| [**\#462**](https://github.com/sentrysoftware/metricshub/issues/462) | Move CLI documentation to the Appendix section                                              |
| [**\#463**](https://github.com/sentrysoftware/metricshub/issues/463) | Combine the Linux and Windows Prometheus quick starts into a unified Prometheus quick start |
| [**\#484**](https://github.com/sentrysoftware/metricshub/issues/484) | Document the Prometheus/Grafana integration                                                 |
| [**\#494**](https://github.com/sentrysoftware/metricshub/issues/494) | Document the use case: **Monitoring Remote Linux**                                          |
| [**\#505**](https://github.com/sentrysoftware/metricshub/issues/505) | Update references to the deprecated `loggingexporter`                                       |

### MetricsHub Community Connectors v1.0.07

#### Changes and Improvements

| ID                                                                                        | Description                                                                                           |
| ----------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------- |
| [**\#143**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/143) | Linux: The connector longer report services, as these are now handled by the `LinuxService` connector |
| [**\#148**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/148) | Linux: Enhance `filesystem` utilization calculation                                                   |

#### Fixed issues

| ID                                                                                        | Description                                                                                                                                   |
| ----------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| [**\#140**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/140) | Fix `Platform` typo instead of `Platforms` on `Linux` & `LinuxService` connectors                                                             |
| [**\#145**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/145) | IpmiTool: The `hw.status` metric is not collected because `enclosure.awk` reports `OK`, `WARN`, `ALARM` instead of `ok`, `degraded`, `failed` |
| [**\#152**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/152) | `high.critical` threshold is not collected by connectors that report voltage metrics                                                          |

#### Documentation updates

| ID                                                                                        | Description                                                                 |
| ----------------------------------------------------------------------------------------- | --------------------------------------------------------------------------- |
| [**\#128**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/128) | Document default connector `variables`                                      |
| [**\#129**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/129) | Replace all references to `sql` with `internalDbQuery` in the documentation |

## MetricsHub Enterprise Edition v1.0.01

### MetricsHub Enterprise Edition v1.0.01

#### Changes and Improvements

| ID           | Description                                                                                                              |
| ------------ | ------------------------------------------------------------------------------------------------------------------------ |
| **M8BEE-29** | Removed Otel collector resourcedetection processor to prevent localhost system resource attributes from being overridden |
| **M8BEE-32** | Moved the localhost resource configuration to the `data center 1` resource group in `metricshub-example.yaml`            |

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

| ID                                                                   | Description                                                                                                              |
| -------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| [**\#433**](https://github.com/sentrysoftware/metricshub/issues/433) | [BREAKING_CHANGE] Disabled Automatic Hostname to FQDN resolution                                                         |
| [**\#427**](https://github.com/sentrysoftware/metricshub/issues/427) | BMC Helix Integration: Added the `StatusInformation` internal text parameter to the connector monitor                    |
| [**\#421**](https://github.com/sentrysoftware/metricshub/issues/421) | Reduced Alert noise for `hw.status{state="present"}`                                                                     |
| [**\#414**](https://github.com/sentrysoftware/metricshub/issues/414) | Added a link to MetricsHub Community Connectors 1.0.06                                                                   |
| [**\#412**](https://github.com/sentrysoftware/metricshub/issues/412) | The `hw.status{state="present"}` metric is no longer reported for cpu monitors discovered by Linux and Window connectors |
| [**\#383**](https://github.com/sentrysoftware/metricshub/issues/383) | Implemented a new engine method `megaBit2Byte` to align with OpenTelemetry unit standards                                |
| [**\#374**](https://github.com/sentrysoftware/metricshub/issues/374) | Default connector variables can now be specified in YAML connector files                                                 |
| [**\#302**](https://github.com/sentrysoftware/metricshub/issues/302) | Defined `afterAll` and `beforeAll` jobs in YAML connectors                                                               |
| [**\#423**](https://github.com/sentrysoftware/metricshub/issues/423) | Added the ability to filter monitors                                                                                     |

#### Fixed issues

| ID                                                                   | Description                                                                      |
| -------------------------------------------------------------------- | -------------------------------------------------------------------------------- |
| [**\#436**](https://github.com/sentrysoftware/metricshub/issues/436) | The log message for SNMP v3 credential validation is incorrect                   |
| [**\#439**](https://github.com/sentrysoftware/metricshub/issues/439) | Connector default variables are not serializable                                 |
| [**\#417**](https://github.com/sentrysoftware/metricshub/issues/417) | JavaDoc references are incorrect                                                 |
| [**\#410**](https://github.com/sentrysoftware/metricshub/issues/410) | Protocol definition is applied to only one host in a multiple-host configuration |
| [**\#368**](https://github.com/sentrysoftware/metricshub/issues/368) | The `hw.power{hw.type="vm"}` metric is erroneously set to 0                      |
| [**\#456**](https://github.com/sentrysoftware/metricshub/issues/456) | An exception occurs when monitoring ESXi through vCenter authentication          |

### MetricsHub Community Connectors v1.0.06

#### Changes and Improvements

| ID                                                                                        | Description                                                                                                         |
| ----------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| [**\#125**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/125) | Disabled automatic detection for WindowsProcess, WindowsService, and LinuxService                                   |
| [**\#122**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/122) | Added default values for connector variables in `WindowsService`, `LinuxService`, `WindowsProcess` & `LinuxProcess` |
| [**\#114**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/114) | The `hw.network.bandwidth.limit` metric is now displayed in bytes                                                   |

#### Fixed issues

| ID                                                                                        | Description                                                              |
| ----------------------------------------------------------------------------------------- | ------------------------------------------------------------------------ |
| [**\#120**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/120) | The `hw.vm.power_ratio` unit is incorrect. It should be 1 instead of Cel |
