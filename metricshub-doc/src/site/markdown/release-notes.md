keywords: release notes
description: Learn more about the new features, changes and improvements, and bug fixes brought to MetricsHub Enterprise.

# Release Notes

## MetricsHub Enterprise Edition v1.1.00

### MetricsHub Enterprise Edition v1.1.00

#### What's New

| ID       | Description                                                        |
| -------- | ------------------------------------------------------------------ |
| M8BEE-37 | Provided protocol CLI executables for easy troubleshooting purpose |

#### Changes and Improvements

| ID       | Description                                               |
| -------- | --------------------------------------------------------- |
| M8BEE-38 | Update OpenTelemetry Collector Contrib to version 0.116.0 |

### MetricsHub Enterprise Connectors v103

#### What's New

| ID    | Description                                                                                      |
| ----- | ------------------------------------------------------------------------------------------------ |
| EC-72 | Added support for Pure Storage FlashArray storage systems via REST API                           |
| EC-75 | Added performance and capacity metrics for NetApp FAS and AFF storage systems via ONTAP REST API |
| EC-86 | Added support for Citrix NetScaler via SNMP                                                      |

#### Changes and Improvements

| ID     | Description                                                                                                                                 |
| ------ | ------------------------------------------------------------------------------------------------------------------------------------------- |
| EC-10  | Detection criteria are enhanced in `EMC VPLEX Version 5`, `EMC VPLEX Version 6` and `Huawei OceanStor (REST)` connectors                    |
| EC-57  | Pure Storage FA Series (REST Token Authentication): NVRAM modules are now reported as memory monitors                                       |
| EC-88  | Added support for HPE ProLiant Gen 11 servers via iLO 6                                                                                     |
| EC-90  | HP iLO Gen 10 (REST): Renamed to HPE iLO 5 (ProLiant Gen10 and Gen10 Plus) and iLO6 support moved to `HPE iLO 6 (ProLiant Gen11)` connector |
| EC-91  | HP iLO Gen 9 (REST): Renamed to `HPE iLO4 (ProLiant Gen 8, Gen9)`                                                                           |
| EC-100 | EMC uemcli (VNXe): Power and temperature values are now collected                                                                           |

#### Fixed issues

| ID    | Description                                                                                                         |
| ----- | ------------------------------------------------------------------------------------------------------------------- |
| EC-84 | Pure Storage FA Series: The `hw.parent.type` attribute reports `DiskController` instead of `disk_controller`        |
| EC-95 | Dell EMC PowerStore: Metrics for physical disks, network cards, memory modules, fans and power supplies are missing |
| EC-97 | Pure Storage FA Series (SSH): `hw.temperature` metrics are not collected                                            |
| EC-98 | Dell iDRAC9 (REST): Incorrect JSON response handling leads to HTTP 404 error on network devices                     |

### MetricsHub Community Edition v1.0.00

### What's New

| ID                                                                   | Description                                                        |
| -------------------------------------------------------------------- | ------------------------------------------------------------------ |
| [**\#424**](https://github.com/sentrysoftware/metricshub/issues/424) | Provided protocol CLI executables for easy troubleshooting purpose |

#### Changes and Improvements

| ID                                                                   | Description                                                                                                                 |
| -------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------- |
| [**\#525**](https://github.com/sentrysoftware/metricshub/issues/518) | Ability to enable or disable self monitoring                                                                                |
| [**\#519**](https://github.com/sentrysoftware/metricshub/issues/519) | Replaced `leftConcat` with `prepend`, `rightConcat` with `append` and `osCommand` with `commandLine` in connector semantics |
| [**\#521**](https://github.com/sentrysoftware/metricshub/issues/521) | Updated OpenTelemetry Java dependencies to version 1.45.0                                                                   |
| [**\#525**](https://github.com/sentrysoftware/metricshub/issues/525) | Ability to enable or disable self-monitoring                                                                                |

#### Fixed issues

| ID                                                      | Description                                                                          |
| ------------------------------------------------------- | ------------------------------------------------------------------------------------ |
| https://github.com/sentrysoftware/metricshub/issues/523 | `hw.network.up` metric not reported for connectors with `WARN` or `ALARM` link state |

#### Documentation updates

| ID                                                                   | Description                                                                                       |
| -------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
| [**\#546**](https://github.com/sentrysoftware/metricshub/issues/546) | Integrate platform icons and enhance documentation connectors directory page                      |
| [**\#541**](https://github.com/sentrysoftware/metricshub/issues/541) | Moved use cases from the documentation to [MetricsHub Use Cases](https://metricshub.com/usecases) |
| [**\#533**](https://github.com/sentrysoftware/metricshub/issues/533) | Documented the self-monitoring feature                                                            |
| [**\#529**](https://github.com/sentrysoftware/metricshub/issues/529) | Create a Troubleshooting section in the user documentation                                        |

### MetricsHub Community Connectors v1.0.08

### What's New

| ID                                                                                        | Description                                |
| ----------------------------------------------------------------------------------------- | ------------------------------------------ |
| [**\#137**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/137) | Added support for MySQL databases via JDBC |

#### Changes and Improvements

| ID                                                                                        | Description                                |
| ----------------------------------------------------------------------------------------- | ------------------------------------------ |
| [**\#158**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/158) | Updated platforms for community connectors |
| [**\#160**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/160) | Create Storage metric semantic conventions |

#### Fixed issues

| ID                                                                                        | Description                                               |
| ----------------------------------------------------------------------------------------- | --------------------------------------------------------- |
| [**\#111**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/111) | LinuxIPNetwork: Fails to monitor some Ethernet interfaces |

## MetricsHub Enterprise Edition v1.0.02

### MetricsHub Enterprise Edition v1.0.02

#### Changes and Improvements

| ID       | Description                                                                              |
| -------- | ---------------------------------------------------------------------------------------- |
| M8BEE-35 | Replaced deprecated `loggingexporter` with `debugexporter` in `otel-config-example.yaml` |

### MetricsHub Enterprise Connectors v102

#### Changes and Improvements

| ID    | Description                                                                       |
| ----- | --------------------------------------------------------------------------------- |
| EC-87 | The `StatusInformation` of the `led` monitor now reports the `LedIndicator` value |

#### Fixed issues

| ID    | Description                                                                                                            |
| ----- | ---------------------------------------------------------------------------------------------------------------------- |
| EC-74 | **HP Insight Management Agent - Drive Array**: The `disk_controller` status is not reported                            |
| EC-77 | **Redfish**: Enclosures are duplicated for Dell iDRAC and HP                                                           |
| EC-78 | **Dell OpenManage Server Administrator**: The `hw.enclosure.energy` metric is not converted to Joules                  |
| EC-79 | **Dell XtremIO REST API**: The `hw.parent.type` attribute is reported as `DiskController` instead of `disk_controller` |
| EC-93 | Connectors reporting voltage metrics do not set the `high.critical` threshold                                          |

### MetricsHub Community Edition v0.9.08

#### Changes and Improvements

| ID                                                                   | Description                                                                                        |
| -------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------- |
| [**\#379**](https://github.com/sentrysoftware/metricshub/issues/379) | Added support for escaped macros                                                                   |
| [**\#422**](https://github.com/sentrysoftware/metricshub/issues/422) | Developed a **JDBC** Extension to enable support for SQL-based connectors                          |
| [**\#432**](https://github.com/sentrysoftware/metricshub/issues/432) | Standardized the log messages for all the criteria tests                                           |
| [**\#435**](https://github.com/sentrysoftware/metricshub/issues/435) | [BREAKING_CHANGE] Added support for multiple variable values for the same connector                |
| [**\#468**](https://github.com/sentrysoftware/metricshub/issues/468) | Added support for shared-characteristics for centralized resource configuration                    |
| [**\#470**](https://github.com/sentrysoftware/metricshub/issues/470) | Added support for `host.id`, `host.name`, and other attributes as arrays in resource configuration |
| [**\#472**](https://github.com/sentrysoftware/metricshub/issues/472) | Prevented sensitive configuration details from being displayed in error logs                       |
| [**\#474**](https://github.com/sentrysoftware/metricshub/issues/474) | Handled blank values when creating INSERT queries for `internalDbQuery` Sources                    |
| [**\#498**](https://github.com/sentrysoftware/metricshub/issues/498) | Improved monitoring jobs when invoking **Jawk** sources in connectors                              |

#### Fixed issues

| ID                                                                   | Description                                                             |
| -------------------------------------------------------------------- | ----------------------------------------------------------------------- |
| [**\#478**](https://github.com/sentrysoftware/metricshub/issues/478) | A NullPointerException occurs when processing `HTTP` detection criteria |
| [**\#480**](https://github.com/sentrysoftware/metricshub/issues/480) | IPMITool criteria and source fail due to invalid `ipmitool` command     |
| [**\#500**](https://github.com/sentrysoftware/metricshub/issues/500) | Only one monitor is processed due to incorrect indexing                 |
| [**\#502**](https://github.com/sentrysoftware/metricshub/issues/502) | Incorrect link status check leads to an incorrect power consumption     |

#### Documentation updates

| ID                                                                   | Description                                                                                                                                                                                                                                                                |
| -------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [**\#462**](https://github.com/sentrysoftware/metricshub/issues/462) | Reviewed **Configure Monitoring** documentation                                                                                                                                                                                                                            |
| [**\#462**](https://github.com/sentrysoftware/metricshub/issues/462) | Moved the CLI documentation to the Appendix section                                                                                                                                                                                                                        |
| [**\#463**](https://github.com/sentrysoftware/metricshub/issues/463) | Combined the Linux and Windows Prometheus quick starts into a unique Prometheus quick start                                                                                                                                                                                |
| [**\#484**](https://github.com/sentrysoftware/metricshub/issues/484) | Documented the Prometheus/Grafana integration                                                                                                                                                                                                                              |
| [**\#289**](https://github.com/sentrysoftware/metricshub/issues/289) | Documented the use cases: **Monitoring network interfaces using SNMP**, **Monitoring a process on Windows**, **Monitoring a remote system running on Linux**, **Monitoring a service running on Linux**, **Monitoring the Health of a Service**, and **Pinging resources** |
| [**\#505**](https://github.com/sentrysoftware/metricshub/issues/505) | Updated references to the deprecated `loggingexporter`                                                                                                                                                                                                                     |

### MetricsHub Community Connectors v1.0.07

#### Changes and Improvements

| ID                                                                                        | Description                                                                                                      |
| ----------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| [**\#112**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/112) | **Windows Process**: The process user name is now retrieved and selectable through configuration variables       |
| [**\#143**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/143) | **Linux OS**: The connector no longer reports services, as these are now handled by the `LinuxService` connector |
| [**\#148**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/148) | **Linux OS**: Enhanced `filesystem` utilization calculation                                                      |

#### Fixed issues

| ID                                                                                        | Description                                                                                                                                       |
| ----------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- |
| [**\#140**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/140) | `Platform` mispelling in `Linux` & `LinuxService` connectors                                                                                      |
| [**\#145**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/145) | **IpmiTool**: The `hw.status` metric is not collected because `enclosure.awk` reports `OK`, `WARN`, `ALARM` instead of `ok`, `degraded`, `failed` |
| [**\#152**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/152) | Connectors reporting voltage metrics do not set the `high.critical` threshold                                                                     |

#### Documentation updates

| ID                                                                                        | Description                                             |
| ----------------------------------------------------------------------------------------- | ------------------------------------------------------- |
| [**\#128**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/128) | Documented default connector `variables`                |
| [**\#129**](https://github.com/sentrysoftware/metricshub-community-connectors/issues/129) | Replaced all references to `sql` with `internalDbQuery` |

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
