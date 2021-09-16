keywords: prometheus exporter, hardware, metrics, output
description: How Hardware Sentry Exporter for Prometheus exposes hardware metrics in a Prometheus format.

# Viewing Collected Metrics

**${project.name}** scrapes all targets listed in the configuration file and returns the collected metrics in the standard Prometheus format via an HTTP endpoint.

Use`http://<host>:<port_number>/metrics` to view the metrics of all monitored targets, or
`http://<host>:<port_number>/metrics/<myhostname>` to view the metrics of a specific target.

```
Example:

# HELP hw_target_ambient_temperature_celsius Metric: hw_target_ambient_temperature_celsius - Unit: celsius
# TYPE hw_target_ambient_temperature_celsius gauge
hw_target_ambient_temperature_celsius{fqdn="ecs1-01.internal.sentrysoftware.net",id="ecs1-01",label="ecs1-01",parent="",} 19.0
# HELP hw_target_cpu_temperature_celsius Metric: hw_target_cpu_temperature_celsius - Unit: celsius
# TYPE hw_target_cpu_temperature_celsius gauge
hw_target_cpu_temperature_celsius{fqdn="ecs1-01.internal.sentrysoftware.net",id="ecs1-01",label="ecs1-01",parent="",} 34.0
# HELP hw_target_cpu_thermal_dissipation_ratio Metric: hw_target_cpu_thermal_dissipation_ratio - Unit: ratio
# TYPE hw_target_cpu_thermal_dissipation_ratio gauge
hw_target_cpu_thermal_dissipation_ratio{fqdn="ecs1-01.internal.sentrysoftware.net",id="ecs1-01",label="ecs1-01",parent="",} 0.23
# HELP hw_target_energy_joules_total Metric: hw_target_energy_joules - Unit: joules
# TYPE hw_target_energy_joules_total counter
hw_target_energy_joules_total{fqdn="ecs1-01.internal.sentrysoftware.net",id="ecs1-01",label="ecs1-01",parent="",} 1.22876352E10
# HELP hw_target_heating_margin_celsius Metric: hw_target_heating_margin_celsius - Unit: celsius
# TYPE hw_target_heating_margin_celsius gauge
hw_target_heating_margin_celsius{fqdn="ecs1-01.internal.sentrysoftware.net",id="ecs1-01",label="ecs1-01",parent="",} 23.0
# HELP hw_target_info Metric: hw_target_info
# TYPE hw_target_info gauge
hw_target_info{fqdn="ecs1-01.internal.sentrysoftware.net",id="ecs1-01",label="ecs1-01",location="remote",parent="",} 1.0
# HELP hw_connector_status Metric: hw_connector_status - Unit: {0 = OK ; 1 = Degraded ; 2 = Failed}
# TYPE hw_connector_status gauge
hw_connector_status{fqdn="ecs1-01.internal.sentrysoftware.net",id="ecs1-01@MIB2Linux",label="MIB2Linux",parent="ecs1-01",} 0.0
hw_connector_status{fqdn="ecs1-01.internal.sentrysoftware.net",id="ecs1-01@DellOpenManage",label="DellOpenManage",parent="ecs1-01",} 0.0
hw_connector_status{fqdn="ecs1-01.internal.sentrysoftware.net",id="ecs1-01@DellStorageManager",label="DellStorageManager",parent="ecs1-01",} 0.0
# HELP hw_connector_info Metric: hw_connector_info
# TYPE hw_connector_info gauge
hw_connector_info{description="This connector discovers the enclosure and Ethernet ports of a system equipped with an MIB-2 standard SNMP Agent.",display_name="MIB-2 Standard SNMP Agent - Network Interfaces - Linux",file_name="MIB2Linux",fqdn="ecs1-01.internal.sentrysoftware.net",id="ecs1-01@MIB2Linux",label="MIB2Linux",parent="ecs1-01",} 1.0
hw_connector_info{description="This connector provides hardware monitoring through the Dell OpenManage Server Administrator SNMP agent which supports almost all Dell PowerEdge servers.",display_name="Dell OpenManage Server Administrator",file_name="DellOpenManage",fqdn="ecs1-01.internal.sentrysoftware.net",id="ecs1-01@DellOpenManage",label="DellOpenManage",parent="ecs1-01",} 1.0
hw_connector_info{description="This connector provides Dell disk array monitoring through the Dell Storage Manager Agent which supports almost all Dell disk arrays.",display_name="Dell OpenManage Storage Manager",file_name="DellStorageManager",fqdn="ecs1-01.internal.sentrysoftware.net",id="ecs1-01@DellStorageManager",label="DellStorageManager",parent="ecs1-01",} 1.0
# HELP hw_enclosure_energy_joules_total Metric: hw_enclosure_energy_joules - Unit: joules
# TYPE hw_enclosure_energy_joules_total counter
hw_enclosure_energy_joules_total{fqdn="ecs1-01.internal.sentrysoftware.net",id="DellOpenManage_enclosure_ecs1-01_1",label="Computer: Dell PowerEdge R630",parent="ecs1-01",} 1.22876352E10
# HELP hw_enclosure_intrusion_status Metric: hw_enclosure_intrusion_status - Unit: {0 = OK ; 2 = Intrusion Detected}
# TYPE hw_enclosure_intrusion_status gauge
hw_enclosure_intrusion_status{fqdn="ecs1-01.internal.sentrysoftware.net",id="DellOpenManage_enclosure_ecs1-01_1",label="Computer: Dell PowerEdge R630",parent="ecs1-01",} 0.0
# HELP hw_enclosure_status Metric: hw_enclosure_status - Unit: {0 = OK ; 1 = Degraded ; 2 = Failed}
# TYPE hw_enclosure_status gauge
hw_enclosure_status{fqdn="ecs1-01.internal.sentrysoftware.net",id="DellOpenManage_enclosure_ecs1-01_1",label="Computer: Dell PowerEdge R630",parent="ecs1-01",} 0.0
# HELP hw_cpu_current_speed_hertz Metric: hw_cpu_current_speed_hertz - Unit: hertz
# TYPE hw_cpu_current_speed_hertz gauge
hw_cpu_current_speed_hertz{fqdn="ecs1-01.internal.sentrysoftware.net",id="DellOpenManage_cpu_ecs1-01_1.1",label="1.1 (Intel - Xeon CPU E5-2620 v4 @ 2.10GHz - 4.00 GHz)",parent="DellOpenManage_enclosure_ecs1-01_1",} 2.1E9
# HELP hw_cpu_present Metric: hw_cpu_present - Unit: {0 = Missing ; 1 = Present}
# TYPE hw_cpu_present gauge
hw_cpu_present{fqdn="ecs1-01.internal.sentrysoftware.net",id="DellOpenManage_cpu_ecs1-01_1.1",label="1.1 (Intel - Xeon CPU E5-2620 v4 @ 2.10GHz - 4.00 GHz)",parent="DellOpenManage_enclosure_ecs1-01_1",} 1.0
# HELP hw_cpu_status Metric: hw_cpu_status - Unit: {0 = OK ; 1 = Degraded ; 2 = Failed}
# TYPE hw_cpu_status gauge
hw_cpu_status{fqdn="ecs1-01.internal.sentrysoftware.net",id="DellOpenManage_cpu_ecs1-01_1.1",label="1.1 (Intel - Xeon CPU E5-2620 v4 @ 2.10GHz - 4.00 GHz)",parent="DellOpenManage_enclosure_ecs1-01_1",} 0.0
# HELP hw_cpu_maximum_speed_hertz Metric: hw_cpu_maximum_speed_hertz - Unit: hertz
# TYPE hw_cpu_maximum_speed_hertz gauge
hw_cpu_maximum_speed_hertz{fqdn="ecs1-01.internal.sentrysoftware.net",id="DellOpenManage_cpu_ecs1-01_1.1",label="1.1 (Intel - Xeon CPU E5-2620 v4 @ 2.10GHz - 4.00 GHz)",parent="DellOpenManage_enclosure_ecs1-01_1",} 4.0E9
# HELP hw_battery_present Metric: hw_battery_present - Unit: {0 = Missing ; 1 = Present}
# TYPE hw_battery_present gauge
hw_battery_present{fqdn="ecs1-01.internal.sentrysoftware.net",id="DellOpenManage_battery_ecs1-01_1.1",label="1.1 (System Board CMOS Battery)",parent="DellOpenManage_enclosure_ecs1-01_1",} 1.0
hw_battery_present{fqdn="ecs1-01.internal.sentrysoftware.net",id="DellStorageManager_battery_ecs1-01_1",label="1 (DELL Battery 0)",parent="DellStorageManager_diskcontroller_ecs1-01_1",} 1.0
# HELP hw_battery_status Metric: hw_battery_status - Unit: {0 = OK ; 1 = Degraded ; 2 = Failed}
# TYPE hw_battery_status gauge
hw_battery_status{fqdn="ecs1-01.internal.sentrysoftware.net",id="DellOpenManage_battery_ecs1-01_1.1",label="1.1 (System Board CMOS Battery)",parent="DellOpenManage_enclosure_ecs1-01_1",} 0.0
hw_battery_status{fqdn="ecs1-01.internal.sentrysoftware.net",id="DellStorageManager_battery_ecs1-01_1",label="1 (DELL Battery 0)",parent="DellStorageManager_diskcontroller_ecs1-01_1",} 0.0
# HELP hw_power_supply_present Metric: hw_power_supply_present - Unit: {0 = Missing ; 1 = Present}
# TYPE hw_power_supply_present gauge
hw_power_supply_present{fqdn="ecs1-01.internal.sentrysoftware.net",id="DellOpenManage_powersupply_ecs1-01_1.1",label="1.1 (AC - 750.0 W)",parent="DellOpenManage_enclosure_ecs1-01_1",} 1.0
hw_power_supply_present{fqdn="ecs1-01.internal.sentrysoftware.net",id="DellOpenManage_powersupply_ecs1-01_1.2",label="1.2 (AC - 750.0 W)",parent="DellOpenManage_enclosure_ecs1-01_1",} 1.0
# HELP hw_power_supply_status Metric: hw_power_supply_status - Unit: {0 = OK ; 1 = Degraded ; 2 = Failed}
# TYPE hw_power_supply_status gauge
hw_power_supply_status{fqdn="ecs1-01.internal.sentrysoftware.net",id="DellOpenManage_powersupply_ecs1-01_1.1",label="1.1 (AC - 750.0 W)",parent="DellOpenManage_enclosure_ecs1-01_1",} 0.0
hw_power_supply_status{fqdn="ecs1-01.internal.sentrysoftware.net",id="DellOpenManage_powersupply_ecs1-01_1.2",label="1.2 (AC - 750.0 W)",parent="DellOpenManage_enclosure_ecs1-01_1",} 0.0
```

## Metrics Descriptions

**${project.name}** exposes two types of metrics:

**Counters** are cumulative metrics whose value can only increase or be reset to zero on restart. Counter-type metrics are composed of:

- **Metric Type**: Typically `counter`
- **Metric Name**: Composed of the _product name_, _monitor label_, _metric_, _total_ (example: `hw_cpu_corrected_errors_total`)
- **Label**: Monitor's additional properties

**Gauges** are metrics that represent a single numerical value that can arbitrarily go up and down. Gauge-type metrics are composed of:

- **Metric Type**: Typically `gauge`
- **Metric Name**: Composed of the _product name_, _monitor label_, _metric_, _status/units_ (example: `hw_cpu_core_used_time_ratio`)
- **Label**: Monitor's additional properties

The table below provides detailed information about the metrics scrapped by **${project.name}** for each Monitor and metric type.

| Monitor         | Metric Type | Metric Name                                 | Labels                                                                                                                                                           |
| --------------- | ----------- | ------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| N/A             | Gauge       | hw_exporter_info                            | build_number, hc_version, project_name, project_version, timestamp                                                                                               |
| Connector       | Gauge       | hw_connector_info                           | description, display_name, file_name, fqdn, id, label, parent                                                                                                    |
| Connector       | Gauge       | hw_connector_status                         | fqdn, id, label, parent                                                                                                                                          |
| Target          | Gauge       | hw_target_info                              | fqdn, id, label, location, parent                                                                                                                                |
| Target          | Gauge       | hw_target_ambient_temperature_celsius       | fqdn, id, label, parent                                                                                                                                          |
| Target          | Gauge       | hw_target_cpu_temperature_celsius           | fqdn, id, label, parent                                                                                                                                          |
| Target          | Gauge       | hw_target_cpu_thermal_dissipation_ratio     | fqdn, id, label, parent                                                                                                                                          |
| Target          | Counter     | hw_target_energy_joules_total               | fqdn, id, label, parent                                                                                                                                          |
| Target          | Gauge       | hw_target_heating_margin_celsius            | fqdn, id, label, parent                                                                                                                                          |
| Target          | Gauge       | hw_target_status                            | fqdn, id, label, parent                                                                                                                                          |
| Battery         | Gauge       | hw_battery_info                             | identifying_information, chemistry, device_id, fqdn, id, label, model, parent, type, vendor                                                                      |
| Battery         | Gauge       | hw_battery_charge_ratio                     | fqdn, id, label, parent                                                                                                                                          |
| Battery         | Gauge       | hw_battery_present                          | fqdn, id, label, parent                                                                                                                                          |
| Battery         | Gauge       | hw_battery_status                           | fqdn, id, label, parent                                                                                                                                          |
| Battery         | Gauge       | hw_battery_time_left_seconds                | fqdn, id, label, parent                                                                                                                                          |
| Blade           | Gauge       | hw_blade_info                               | identifying_information, blade_name, device_id, fqdn, id, label, model, parent, serial_number                                                                    |
| Blade           | Gauge       | hw_blade_power_state                        | fqdn, id, label, parent                                                                                                                                          |
| Blade           | Gauge       | hw_blade_present                            | fqdn, id, label, parent                                                                                                                                          |
| Blade           | Gauge       | hw_blade_status                             | fqdn, id, label, parent                                                                                                                                          |
| CPU             | Gauge       | hw_cpu_info                                 | identifying_information, device_id, fqdn, id, label, maximum_speed, model, parent, vendor                                                                        |
| CPU             | Counter     | hw_cpu_corrected_errors_total               | fqdn, id, label, parent                                                                                                                                          |
| CPU             | Gauge       | hw_cpu_current_speed_hertz                  | fqdn, id, label, parent                                                                                                                                          |
| CPU             | Counter     | hw_cpu_energy_joules_total                  | fqdn, id, label, parent                                                                                                                                          |
| CPU             | Gauge       | hw_cpu_predicted_failure                    | fqdn, id, label, parent                                                                                                                                          |
| CPU             | Gauge       | hw_cpu_present                              | fqdn, id, label, parent                                                                                                                                          |
| CPU             | Gauge       | hw_cpu_status                               | fqdn, id, label, parent                                                                                                                                          |
| CPU Core        | Gauge       | hw_cpu_core_info                            | identifying_information, device_id, fqdn, id, label, parent                                                                                                      |
| CPU Core        | Gauge       | hw_cpu_core_current_speed_hertz             | fqdn, id, label, parent                                                                                                                                          |
| CPU Core        | Gauge       | hw_cpu_core_present                         | fqdn, id, label, parent                                                                                                                                          |
| CPU Core        | Gauge       | hw_cpu_core_status                          | fqdn, id, label, parent                                                                                                                                          |
| CPU Core        | Gauge       | hw_cpu_core_used_time_ratio                 | fqdn, id, label, parent                                                                                                                                          |
| Disk Controller | Gauge       | hw_disk_controller_info                     | identifying_information, bios_version, device_id, driver_version, firmware_version, fqdn, id, label, model, parent, serial_number, vendor                        |
| Disk Controller | Gauge       | hw_disk_controller_battery_status           | fqdn, id, label, parent                                                                                                                                          |
| Disk Controller | Gauge       | hw_disk_controller_controller_status        | fqdn, id, label, parent                                                                                                                                          |
| Disk Controller | Counter     | hw_disk_controller_energy_joules_total      | fqdn, id, label, parent                                                                                                                                          |
| Disk Controller | Gauge       | hw_disk_controller_present                  | fqdn, id, label, parent                                                                                                                                          |
| Disk Controller | Gauge       | hw_disk_controller_status                   | fqdn, id, label, parent                                                                                                                                          |
| Enclosure       | Gauge       | hw_enclosure_info                           | identifying_information, bios_version, device_id, fqdn, id, label, model, parent, serial_number, type, vendor                                                    |
| Enclosure       | Counter     | hw_enclosure_energy_joules_total            | fqdn, id, label, parent                                                                                                                                          |
| Enclosure       | Gauge       | hw_enclosure_intrusion_status               | fqdn, id, label, parent                                                                                                                                          |
| Enclosure       | Gauge       | hw_enclosure_present                        | fqdn, id, label, parent                                                                                                                                          |
| Enclosure       | Gauge       | hw_enclosure_status                         | fqdn, id, label, parent                                                                                                                                          |
| Fan             | Gauge       | hw_fan_info                                 | identifying_information, device_id, fan_type, fqdn, id, label, parent                                                                                            |
| Fan             | Counter     | hw_fan_energy_joules_total                  | fqdn, id, label, parent                                                                                                                                          |
| Fan             | Gauge       | hw_fan_present                              | fqdn, id, label, parent                                                                                                                                          |
| Fan             | Gauge       | hw_fan_speed_rpm                            | fqdn, id, label, parent                                                                                                                                          |
| Fan             | Gauge       | hw_fan_speed_ratio                          | fqdn, id, label, parent                                                                                                                                          |
| Fan             | Gauge       | hw_fan_status                               | fqdn, id, label, parent                                                                                                                                          |
| LED             | Gauge       | hw_led_info                                 | identifying_information, device_id, fqdn, id, label, name, parent                                                                                                |
| LED             | Gauge       | hw_led_color_status                         | fqdn, id, label, parent                                                                                                                                          |
| LED             | Gauge       | hw_led_indicator_status                     | fqdn, id, label, parent                                                                                                                                          |
| LED             | Gauge       | hw_led_status                               | fqdn, id, label, parent                                                                                                                                          |
| Logical Disk    | Gauge       | hw_logical_disk_info                        | identifying_information, device_id, fqdn, id, label, parent, raid_level, size                                                                                    |
| Logical Disk    | Counter     | hw_logical_disk_errors_total                | fqdn, id, label, parent                                                                                                                                          |
| Logical Disk    | Gauge       | hw_logical_disk_status                      | fqdn, id, label, parent                                                                                                                                          |
| Logical Disk    | Gauge       | hw_logical_disk_unallocated_space_bytes     | fqdn, id, label, parent                                                                                                                                          |
| LUN             | Gauge       | hw_lun_info                                 | identifying_information, array_name, device_id, expected_path_count, fqdn, id, label, local_device_name, parent, remote_device_name, wwn                         |
| LUN             | Gauge       | hw_lun_available_paths                      | fqdn, id, label, parent                                                                                                                                          |
| LUN             | Gauge       | hw_lun_status                               | fqdn, id, label, parent                                                                                                                                          |
| Memory          | Gauge       | hw_memory_info                              | identifying_information, chemistry, device_id, fqdn, id, label, model, parent, type, vendor                                                                      |
| Memory          | Counter     | hw_memory_energy_joules_total               | fqdn, id, label, parent                                                                                                                                          |
| Memory          | Counter     | hw_memory_errors_total                      | fqdn, id, label, parent                                                                                                                                          |
| Memory          | Gauge       | hw_memory_error_status                      | fqdn, id, label, parent                                                                                                                                          |
| Memory          | Gauge       | hw_memory_predicted_failure                 | fqdn, id, label, parent                                                                                                                                          |
| Memory          | Gauge       | hw_memory_present                           | fqdn, id, label, parent                                                                                                                                          |
| Memory          | Gauge       | hw_memory_status                            | fqdn, id, label, parent                                                                                                                                          |
| Network Card    | Gauge       | hw_network_card_info                        | identifying_information, bandwidth, device_id, fqdn, id, label, logical_address, model, parent, physical_address, remote_physical_address, serial_number, vendor |
| Network Card    | Gauge       | hw_network_card_bandwidth_utilization_ratio | fqdn, id, label, parent                                                                                                                                          |
| Network Card    | Gauge       | hw_network_card_duplex_mode                 | fqdn, id, label, parent                                                                                                                                          |
| Network Card    | Counter     | hw_network_card_energy_joules_total         | fqdn, id, label, parent                                                                                                                                          |
| Network Card    | Counter     | hw_network_card_errors_total                | fqdn, id, label, parent                                                                                                                                          |
| Network Card    | Gauge       | hw_network_card_link_speed_bytes_per_second | fqdn, id, label, parent                                                                                                                                          |
| Network Card    | Gauge       | hw_network_card_link_status                 | fqdn, id, label, parent                                                                                                                                          |
| Network Card    | Gauge       | hw_network_card_present                     | fqdn, id, label, parent                                                                                                                                          |
| Network Card    | Counter     | hw_network_card_received_bytes_total        | fqdn, id, label, parent                                                                                                                                          |
| Network Card    | Counter     | hw_network_card_received_packets_total      | fqdn, id, label, parent                                                                                                                                          |
| Network Card    | Gauge       | hw_network_card_status                      | fqdn, id, label, parent                                                                                                                                          |
| Network Card    | Counter     | hw_network_card_transmitted_bytes_total     | fqdn, id, label, parent                                                                                                                                          |
| Network Card    | Counter     | hw_network_card_transmitted_packets_total   | fqdn, id, label, parent                                                                                                                                          |
| Network Card    | Counter     | hw_network_card_zero_buffer_credits_total   | fqdn, id, label, parent                                                                                                                                          |
| Other Device    | Gauge       | hw_other_device_info                        | identifying_information, device_id, device_type, fqdn, id, label, parent                                                                                         |
| Other Device    | Gauge       | hw_other_device_present                     | fqdn, id, label, parent                                                                                                                                          |
| Other Device    | Gauge       | hw_other_device_status                      | fqdn, id, label, parent                                                                                                                                          |
| Other Device    | Counter     | hw_other_device_usage_times_total           | fqdn, id, label, parent                                                                                                                                          |
| Other Device    | Gauge       | hw_other_device_value                       | fqdn, id, label, parent                                                                                                                                          |
| Physical Disk   | Gauge       | hw_physical_disk_info                       | identifying_information, device_id, firmware_version, fqdn, id, label, model, parent, serial_number, size, vendor                                                |
| Physical Disk   | Gauge       | hw_physical_disk_endurance_remaining_ratio  | fqdn, id, label, parent                                                                                                                                          |
| Physical Disk   | Counter     | hw_physical_disk_energy_joules_total        | fqdn, id, label, parent                                                                                                                                          |
| Physical Disk   | Counter     | hw_physical_disk_errors_total               | fqdn, id, label, parent                                                                                                                                          |
| Physical Disk   | Gauge       | hw_physical_disk_intrusion_status           | fqdn, id, label, parent                                                                                                                                          |
| Physical Disk   | Gauge       | hw_physical_disk_present                    | fqdn, id, label, parent                                                                                                                                          |
| Physical Disk   | Gauge       | hw_physical_disk_status                     | fqdn, id, label, parent                                                                                                                                          |
| Physical Disk   | Gauge       | hw_physical_disk_predicted_failure          | fqdn, id, label, parent                                                                                                                                          |
| Power Supply    | Gauge       | hw_power_supply_info                        | identifying_information, device_id, fqdn, id, label, parent, power_supply_power label, power_supply_type                                                         |
| Power Supply    | Gauge       | hw_power_supply_present                     | fqdn, id, label, parent                                                                                                                                          |
| Power Supply    | Gauge       | hw_power_supply_status                      | fqdn, id, label, parent                                                                                                                                          |
| Power Supply    | Gauge       | hw_power_supply_used_capacity_ratio         | fqdn, id, label, parent                                                                                                                                          |
| Robotics        | Gauge       | hw_robotics_info                            | identifying_information, device_id, fqdn, id, label, model, parent, robotic_type, serial_number, vendor                                                          |
| Robotics        | Counter     | hw_robotics_energy_joules_total             | fqdn, id, label, parent                                                                                                                                          |
| Robotics        | Counter     | hw_robotics_errors_total                    | fqdn, id, label, parent                                                                                                                                          |
| Robotics        | Counter     | hw_robotics_moves_total                     | fqdn, id, label, parent                                                                                                                                          |
| Robotics        | Gauge       | hw_robotics_present                         | fqdn, id, label, parent                                                                                                                                          |
| Robotics        | Gauge       | hw_robotics_status                          | fqdn, id, label, parent                                                                                                                                          |
| Tape Drive      | Gauge       | hw_tape_drive_info                          | identifying_information, device_id, fqdn, id, label, model, parent, serial_number, vendor                                                                        |
| Tape Drive      | Counter     | hw_tape_drive_energy_joules_total           | fqdn, id, label, parent                                                                                                                                          |
| Tape Drive      | Counter     | hw_tape_drive_errors_total                  | fqdn, id, label, parent                                                                                                                                          |
| Tape Drive      | Counter     | hw_tape_drive_mounts_total                  | fqdn, id, label, parent                                                                                                                                          |
| Tape Drive      | Gauge       | hw_tape_drive_needs_cleaning                | fqdn, id, label, parent                                                                                                                                          |
| Tape Drive      | Gauge       | hw_tape_drive_present                       | fqdn, id, label, parent                                                                                                                                          |
| Tape Drive      | Gauge       | hw_tape_drive_status                        | fqdn, id, label, parent                                                                                                                                          |
| Tape Drive      | Counter     | hw_tape_drive_unmounts_total                | fqdn, id, label, parent                                                                                                                                          |
| Temperature     | Gauge       | hw_temperature_info                         | identifying_information, device_id, fqdn, id, label, parent, temperature_type                                                                                    |
| Temperature     | Gauge       | hw_temperature_status                       | fqdn, id, label, parent                                                                                                                                          |
| Temperature     | Gauge       | hw_temperature_celsius                      | fqdn, id, label, parent                                                                                                                                          |
| Voltage         | Gauge       | hw_voltage_info                             | identifying_information, device_id, fqdn, id, label, parent, voltage_type                                                                                        |
| Voltage         | Gauge       | hw_voltage_status                           | fqdn, id, label, parent                                                                                                                                          |
| Voltage         | Gauge       | hw_voltage_volts                            | fqdn, id, label, parent                                                                                                                                          |
