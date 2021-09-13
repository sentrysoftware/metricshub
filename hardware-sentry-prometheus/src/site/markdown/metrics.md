keywords: prometheus exporter, hardware, metrics, output
description: How Hardware Sentry Exporter for Prometheus exposes hardware metrics in a Prometheus format.

# Viewing Collected Metrics

**${project.name}** scrapes all targets listed in the configuration file and returns the collected metrics in the standard Prometheus format via an HTTP endpoint.

Use```http://<host>:<port_number>/metrics``` to view the metrics of all monitored targets, or
```http://<host>:<port_number>/metrics/<myhostname>``` to view the metrics of a specific target.

```
Example:

# HELP hw_target_heating_margin_celsius Metric: hw_target_heating_margin_celsius - Unit: celsius
# TYPE hw_target_heating_margin_celsius gauge
hw_target_heating_margin_celsius{id="ilo-dev-vm-01",parent="",label="ilo-dev-vm-01",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 22.0
# HELP hw_target_info Metric: hw_target_info
# TYPE hw_target_info gauge
hw_target_info{id="ilo-dev-vm-01",parent="",label="ilo-dev-vm-01",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",location="remote",} 1.0
# HELP hw_connector_status Metric: hw_connector_status - Unit: {0 = OK ; 1 = Degraded ; 2 = Failed}
# TYPE hw_connector_status gauge
hw_connector_status{id="ilo-dev-vm-01@MS_HW_CiscoUCSBlade.connector",parent="ilo-dev-vm-01",label="MS_HW_CiscoUCSBlade.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 0.0
hw_connector_status{id="ilo-dev-vm-01@MS_HW_CpMgServNT.connector",parent="ilo-dev-vm-01",label="MS_HW_CpMgServNT.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 0.0
hw_connector_status{id="ilo-dev-vm-01@MS_HW_CpqDriveArrayNT.connector",parent="ilo-dev-vm-01",label="MS_HW_CpqDriveArrayNT.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 0.0
hw_connector_status{id="ilo-dev-vm-01@MS_HW_CpMgSm2.connector",parent="ilo-dev-vm-01",label="MS_HW_CpMgSm2.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 0.0
hw_connector_status{id="ilo-dev-vm-01@MS_HW_CpqHeResMem2.connector",parent="ilo-dev-vm-01",label="MS_HW_CpqHeResMem2.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 0.0
# HELP hw_connector_info Metric: hw_connector_info
# TYPE hw_connector_info gauge
hw_connector_info{id="ilo-dev-vm-01@MS_HW_CiscoUCSBlade.connector",parent="ilo-dev-vm-01",label="MS_HW_CiscoUCSBlade.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",displayName="Cisco UCS Manager (Fabric Interconnect Switch)",fileName="MS_HW_CiscoUCSBlade.connector",description="This connector provides hardware monitoring for Cisco UCS Blade chassis (as well as the Cisco Fabric Interconnect Switch) through the UCS Manager (running on the Fabric Interconnect Switch).",} 1.0
hw_connector_info{id="ilo-dev-vm-01@MS_HW_CpMgServNT.connector",parent="ilo-dev-vm-01",label="MS_HW_CpMgServNT.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",displayName="HP Insight Management Agent - Server",fileName="MS_HW_CpMgServNT.connector",description="This connector provides hardware monitoring through the HP Insight Manager (Server Agent) which supports almost all HP ProLiant and Integrity servers under Windows and Linux, as well as Tru64 servers.",} 1.0
hw_connector_info{id="ilo-dev-vm-01@MS_HW_CpqDriveArrayNT.connector",parent="ilo-dev-vm-01",label="MS_HW_CpqDriveArrayNT.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",displayName="HP Insight Management Agent - Drive Array",fileName="MS_HW_CpqDriveArrayNT.connector",description="This connector monitors the HP/Compaq Drive Arrays by connecting to the Storage Management SNMP sub-agent of the HP Insight Manager agent.",} 1.0
hw_connector_info{id="ilo-dev-vm-01@MS_HW_CpMgSm2.connector",parent="ilo-dev-vm-01",label="MS_HW_CpMgSm2.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",displayName="HP Insight Management Agent - iLO",fileName="MS_HW_CpMgSm2.connector",description="This connector provides hardware monitoring of the HP iLO card in HP ProLiant servers through the HP Insight Manager (Server Agent) which supports almost all HP ProLiant and Integrity servers under Windows and Linux, as well as Tru64 servers.",} 1.0
hw_connector_info{id="ilo-dev-vm-01@MS_HW_CpqHeResMem2.connector",parent="ilo-dev-vm-01",label="MS_HW_CpqHeResMem2.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",displayName="HP Insight Management Agent (v8.25 or higher) - Memory",fileName="MS_HW_CpqHeResMem2.connector",description="This connector provides Memory Information through the HP Insight Manager (Newer Server Agents).",} 1.0
# HELP hw_enclosure_info Metric: hw_enclosure_info
# TYPE hw_enclosure_info gauge
hw_enclosure_info{id="MS_HW_CpMgServNT.connector_enclosure_ilo-dev-vm-01_0",parent="ilo-dev-vm-01",label="Computer: HP ProLiant DL360 Gen9",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",deviceId="0",serialNumber="CZJ6190871",vendor="HP",model="ProLiant DL360 Gen9",biosVersion="",type="Computer",additionalInformation1="Part Number: 755259-B21",additionalInformation2="Alternative Serial Number: ",additionalInformation3="",} 1.0
# HELP hw_cpu_current_speed_hertz Metric: hw_cpu_current_speed_hertz - Unit: hertz
# TYPE hw_cpu_current_speed_hertz gauge
hw_cpu_current_speed_hertz{id="MS_HW_CpMgServNT.connector_cpu_ilo-dev-vm-01_0",parent="MS_HW_CpMgServNT.connector_enclosure_ilo-dev-vm-01_0",label="0 (Intel - Xeon - 1.60 GHz)",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 1.6E9
hw_cpu_current_speed_hertz{id="MS_HW_CpMgServNT.connector_cpu_ilo-dev-vm-01_1",parent="MS_HW_CpMgServNT.connector_enclosure_ilo-dev-vm-01_0",label="1 (Intel - Xeon - 1.60 GHz)",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 1.6E9
# HELP hw_cpu_predicted_failure Metric: hw_cpu_predicted_failure - Unit: {0 = OK ; 1 = Failure Predicted}
# TYPE hw_cpu_predicted_failure gauge
hw_cpu_predicted_failure{id="MS_HW_CpMgServNT.connector_cpu_ilo-dev-vm-01_0",parent="MS_HW_CpMgServNT.connector_enclosure_ilo-dev-vm-01_0",label="0 (Intel - Xeon - 1.60 GHz)",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 1.0
hw_cpu_predicted_failure{id="MS_HW_CpMgServNT.connector_cpu_ilo-dev-vm-01_1",parent="MS_HW_CpMgServNT.connector_enclosure_ilo-dev-vm-01_0",label="1 (Intel - Xeon - 1.60 GHz)",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 1.0
# HELP hw_cpu_present Metric: hw_cpu_present - Unit: {0 = Missing ; 1 = Present}
# TYPE hw_cpu_present gauge
...
```

## Metrics Descriptions

**${project.name}** exposes two types of metrics:

**Counters** are cumulative metrics whose value can only increase or be reset to zero on restart. Counter-type metrics are composed of:

* **Metric Type**: Typically ```counter```
* **Metric Name**: Composed of the _product name_, _monitor label_, _metric_, _total_ (example: ```hw_cpu_corrected_errors_total```)
* **Label**: Monitor's additional properties

**Gauges** are metrics that represent a single numerical value that can arbitrarily go up and down. Gauge-type metrics are composed of:

* **Metric Type**: Typically ```gauge```
* **Metric Name**: Composed of the _product name_, _monitor label_, _metric_, _status/units_ (example: ```hw_cpu_core_used_time_ratio```)
* **Label**: Monitor's additional properties

The table below provides detailed information about the metrics scrapped by **${project.name}** for each Monitor and metric type.

| monitor         | metric type | metric name                                 | labels                                                                                                                                                          |
| --------------- | ----------- | ------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Connector       | Gauge       | hw_connector_info                           | description, display_name, file_name, fqdn, id, label, parent                                                                                                   |
| Connector       | Gauge       | hw_connector_status                         | fqdn, id, label, parent                                                                                                                                         |
| Target          | Gauge       | hw_target_info                              | fqdn, id, label, location, parent                                                                                                                               |
| Target          | Gauge       | hw_target_ambient_temperature_celsius       | fqdn, id, label, parent                                                                                                                                         |
| Target          | Gauge       | hw_target_cpu_temperature_celsius           | fqdn, id, label, parent                                                                                                                                         |
| Target          | Gauge       | hw_target_cpu_thermal_dissipation_ratio     | fqdn, id, label, parent                                                                                                                                         |
| Target          | Counter     | hw_target_energy_joules_total               | fqdn, id, label, parent                                                                                                                                         |
| Target          | Gauge       | hw_target_heating_margin_celsius            | fqdn, id, label, parent                                                                                                                                         |
| Target          | Gauge       | hw_target_status                            | fqdn, id, label, parent                                                                                                                                         |
| Battery         | Gauge       | hw_battery_info                             | additional_information, chemistry, device_id, fqdn, id, label, model, parent, type, vendor                                                                      |
| Battery         | Gauge       | hw_battery_charge_ratio                     | fqdn, id, label, parent                                                                                                                                         |
| Battery         | Gauge       | hw_battery_present                          | fqdn, id, label, parent                                                                                                                                         |
| Battery         | Gauge       | hw_battery_status                           | fqdn, id, label, parent                                                                                                                                         |
| Battery         | Gauge       | hw_battery_time_left_seconds                | fqdn, id, label, parent                                                                                                                                         |
| Blade           | Gauge       | hw_blade_info                               | additional_information, blade_name, device_id, fqdn, id, label, model, parent, serial_number                                                                    |
| Blade           | Gauge       | hw_blade_power_state                        | fqdn, id, label, parent                                                                                                                                         |
| Blade           | Gauge       | hw_blade_present                            | fqdn, id, label, parent                                                                                                                                         |
| Blade           | Gauge       | hw_blade_status                             | fqdn, id, label, parent                                                                                                                                         |
| CPU             | Gauge       | hw_cpu_info                                 | additional_information, device_id, fqdn, id, label, maximum_speed, model, parent, vendor                                                                        |
| CPU             | Counter     | hw_cpu_corrected_errors_total               | fqdn, id, label, parent                                                                                                                                         |
| CPU             | Gauge       | hw_cpu_current_speed_hertz                  | fqdn, id, label, parent                                                                                                                                         |
| CPU             | Counter     | hw_cpu_energy_joules_total                  | fqdn, id, label, parent                                                                                                                                         |
| CPU             | Gauge       | hw_cpu_predicted_failure                    | fqdn, id, label, parent                                                                                                                                         |
| CPU             | Gauge       | hw_cpu_present                              | fqdn, id, label, parent                                                                                                                                         |
| CPU             | Gauge       | hw_cpu_status                               | fqdn, id, label, parent                                                                                                                                         |
| CPU Core        | Gauge       | hw_cpu_core_info                            | additional_information, device_id, fqdn, id, label, parent                                                                                                      |
| CPU Core        | Gauge       | hw_cpu_core_current_speed_hertz             | fqdn, id, label, parent                                                                                                                                         |
| CPU Core        | Gauge       | hw_cpu_core_present                         | fqdn, id, label, parent                                                                                                                                         |
| CPU Core        | Gauge       | hw_cpu_core_status                          | fqdn, id, label, parent                                                                                                                                         |
| CPU Core        | Gauge       | hw_cpu_core_used_time_ratio                 | fqdn, id, label, parent                                                                                                                                         |
| Disk Controller | Gauge       | hw_disk_controller_info                     | additional_information, bios_version, device_id, driver_version, firmware_version, fqdn, id, label, model, parent, serial_number, vendor                        |
| Disk Controller | Gauge       | hw_disk_controller_battery_status           | fqdn, id, label, parent                                                                                                                                         |
| Disk Controller | Gauge       | hw_disk_controller_controller_status        | fqdn, id, label, parent                                                                                                                                         |
| Disk Controller | Counter     | hw_disk_controller_energy_joules_total      | fqdn, id, label, parent                                                                                                                                         |
| Disk Controller | Gauge       | hw_disk_controller_present                  | fqdn, id, label, parent                                                                                                                                         |
| Disk Controller | Gauge       | hw_disk_controller_status                   | fqdn, id, label, parent                                                                                                                                         |
| Enclosure       | Gauge       | hw_enclosure_info                           | additional_information, bios_version, device_id, fqdn, id, label, model, parent, serial_number, type, vendor                                                    |
| Enclosure       | Counter     | hw_enclosure_energy_joules_total            | fqdn, id, label, parent                                                                                                                                         |
| Enclosure       | Gauge       | hw_enclosure_intrusion_status               | fqdn, id, label, parent                                                                                                                                         |
| Enclosure       | Gauge       | hw_enclosure_status                         | fqdn, id, label, parent                                                                                                                                         |
| Fan             | Gauge       | hw_fan_info                                 | additional_information, device_id, fan_type, fqdn, id, label, parent                                                                                            |
| Fan             | Counter     | hw_fan_energy_joules_total                  | fqdn, id, label, parent                                                                                                                                         |
| Fan             | Gauge       | hw_fan_present                              | fqdn, id, label, parent                                                                                                                                         |
| Fan             | Gauge       | hw_fan_speed_rpm                            | fqdn, id, label, parent                                                                                                                                         |
| Fan             | Gauge       | hw_fan_speed_ratio                          | fqdn, id, label, parent                                                                                                                                         |
| Fan             | Gauge       | hw_fan_status                               | fqdn, id, label, parent                                                                                                                                         |
| LED             | Gauge       | hw_led_info                                 | additional_information, device_id, fqdn, id, label, name, parent                                                                                                |
| LED             | Gauge       | hw_led_color_status                         | fqdn, id, label, parent                                                                                                                                         |
| LED             | Gauge       | hw_led_indicator_status                     | fqdn, id, label, parent                                                                                                                                         |
| LED             | Gauge       | hw_led_status                               | fqdn, id, label, parent                                                                                                                                         |
| Logical Disk    | Gauge       | hw_logical_disk_info                        | additional_information, device_id, fqdn, id, label, parent, raid_level, size                                                                                    |
| Logical Disk    | Counter     | hw_logical_disk_errors_total                | fqdn, id, label, parent                                                                                                                                         |
| Logical Disk    | Gauge       | hw_logical_disk_status                      | fqdn, id, label, parent                                                                                                                                         |
| Logical Disk    | Gauge       | hw_logical_disk_unallocated_space_bytes     | fqdn, id, label, parent                                                                                                                                         |
| LUN             | Gauge       | hw_lun_info                                 | additional_information, array_name, device_id, expected_path_count, fqdn, id, label, local_device_name, parent, remote_device_name, wwn                         |
| LUN             | Gauge       | hw_lun_available_paths                      | fqdn, id, label, parent                                                                                                                                         |
| LUN             | Gauge       | hw_lun_status                               | fqdn, id, label, parent                                                                                                                                         |
| Memory          | Gauge       | hw_memory_info                              | additional_information, chemistry, device_id, fqdn, id, label, model, parent, type, vendor                                                                      |
| Memory          | Counter     | hw_memory_energy_joules_total               | fqdn, id, label, parent                                                                                                                                         |
| Memory          | Counter     | hw_memory_errors_total                      | fqdn, id, label, parent                                                                                                                                         |
| Memory          | Gauge       | hw_memory_error_status                      | fqdn, id, label, parent                                                                                                                                         |
| Memory          | Gauge       | hw_memory_predicted_failure                 | fqdn, id, label, parent                                                                                                                                         |
| Memory          | Gauge       | hw_memory_present                           | fqdn, id, label, parent                                                                                                                                         |
| Memory          | Gauge       | hw_memory_status                            | fqdn, id, label, parent                                                                                                                                         |
| Network Card    | Gauge       | hw_network_card_info                        | additional_information, bandwidth, device_id, fqdn, id, label, logical_address, model, parent, physical_address, remote_physical_address, serial_number, vendor |
| Network Card    | Gauge       | hw_network_card_bandwidth_utilization_ratio | fqdn, id, label, parent                                                                                                                                         |
| Network Card    | Gauge       | hw_network_card_duplex_mode                 | fqdn, id, label, parent                                                                                                                                         |
| Network Card    | Counter     | hw_network_card_energy_joules_total         | fqdn, id, label, parent                                                                                                                                         |
| Network Card    | Counter     | hw_network_card_errors_total                | fqdn, id, label, parent                                                                                                                                         |
| Network Card    | Gauge       | hw_network_card_link_speed_bytes_per_second | fqdn, id, label, parent                                                                                                                                         |
| Network Card    | Gauge       | hw_network_card_link_status                 | fqdn, id, label, parent                                                                                                                                         |
| Network Card    | Gauge       | hw_network_card_present                     | fqdn, id, label, parent                                                                                                                                         |
| Network Card    | Counter     | hw_network_card_received_bytes_total        | fqdn, id, label, parent                                                                                                                                         |
| Network Card    | Counter     | hw_network_card_received_packets_total      | fqdn, id, label, parent                                                                                                                                         |
| Network Card    | Gauge       | hw_network_card_status                      | fqdn, id, label, parent                                                                                                                                         |
| Network Card    | Counter     | hw_network_card_transmitted_bytes_total     | fqdn, id, label, parent                                                                                                                                         |
| Network Card    | Counter     | hw_network_card_transmitted_packets_total   | fqdn, id, label, parent                                                                                                                                         |
| Network Card    | Counter     | hw_network_card_zero_buffer_credits_total   | fqdn, id, label, parent                                                                                                                                         |
| Other Device    | Gauge       | hw_other_device_info                        | additional_information, device_id, device_type, fqdn, id, label, parent                                                                                         |
| Other Device    | Gauge       | hw_other_device_present                     | fqdn, id, label, parent                                                                                                                                         |
| Other Device    | Gauge       | hw_other_device_status                      | fqdn, id, label, parent                                                                                                                                         |
| Other Device    | Counter     | hw_other_device_usage_times_total           | fqdn, id, label, parent                                                                                                                                         |
| Other Device    | Gauge       | hw_other_device_value                       | fqdn, id, label, parent                                                                                                                                         |
| Physical Disk   | Gauge       | hw_physical_disk_info                       | additional_information, device_id, firmware_version, fqdn, id, label, model, parent, serial_number, size, vendor                                                |
| Physical Disk   | Gauge       | hw_physical_disk_endurance_remaining_ratio  | fqdn, id, label, parent                                                                                                                                         |
| Physical Disk   | Counter     | hw_physical_disk_energy_joules_total        | fqdn, id, label, parent                                                                                                                                         |
| Physical Disk   | Counter     | hw_physical_disk_errors_total               | fqdn, id, label, parent                                                                                                                                         |
| Physical Disk   | Gauge       | hw_physical_disk_intrusion_status           | fqdn, id, label, parent                                                                                                                                         |
| Physical Disk   | Gauge       | hw_physical_disk_present                    | fqdn, id, label, parent                                                                                                                                         |
| Physical Disk   | Gauge       | hw_physical_disk_status                     | fqdn, id, label, parent                                                                                                                                         |
| Power Supply    | Gauge       | hw_power_supply_info                        | additional_information, device_id, fqdn, id, label, parent, power_supply_type                                                                                   |
| Power Supply    | Gauge       | hw_power_supply_present                     | fqdn, id, label, parent                                                                                                                                         |
| Power Supply    | Gauge       | hw_power_supply_status                      | fqdn, id, label, parent                                                                                                                                         |
| Power Supply    | Gauge       | hw_power_supply_used_capacity_ratio         | fqdn, id, label, parent                                                                                                                                         |
| Robotics        | Gauge       | hw_robotics_info                            | additional_information, device_id, fqdn, id, label, model, parent, robotic_type, serial_number, vendor                                                          |
| Robotics        | Counter     | hw_robotics_energy_joules_total             | fqdn, id, label, parent                                                                                                                                         |
| Robotics        | Counter     | hw_robotics_errors_total                    | fqdn, id, label, parent                                                                                                                                         |
| Robotics        | Counter     | hw_robotics_moves_total                     | fqdn, id, label, parent                                                                                                                                         |
| Robotics        | Gauge       | hw_robotics_present                         | fqdn, id, label, parent                                                                                                                                         |
| Robotics        | Gauge       | hw_robotics_status                          | fqdn, id, label, parent                                                                                                                                         |
| Tape Drive      | Gauge       | hw_tape_drive_info                          | additional_information, device_id, fqdn, id, label, model, parent, serial_number, vendor                                                                        |
| Tape Drive      | Counter     | hw_tape_drive_energy_joules_total           | fqdn, id, label, parent                                                                                                                                         |
| Tape Drive      | Counter     | hw_tape_drive_errors_total                  | fqdn, id, label, parent                                                                                                                                         |
| Tape Drive      | Counter     | hw_tape_drive_mounts_total                  | fqdn, id, label, parent                                                                                                                                         |
| Tape Drive      | Gauge       | hw_tape_drive_needs_cleaning                | fqdn, id, label, parent                                                                                                                                         |
| Tape Drive      | Gauge       | hw_tape_drive_present                       | fqdn, id, label, parent                                                                                                                                         |
| Tape Drive      | Gauge       | hw_tape_drive_status                        | fqdn, id, label, parent                                                                                                                                         |
| Tape Drive      | Counter     | hw_tape_drive_unmounts_total                | fqdn, id, label, parent                                                                                                                                         |
| Temperature     | Gauge       | hw_temperature_info                         | additional_information, device_id, fqdn, id, label, parent, temperature_type                                                                                    |
| Temperature     | Gauge       | hw_temperature_status                       | fqdn, id, label, parent                                                                                                                                         |
| Temperature     | Gauge       | hw_temperature_celsius                      | fqdn, id, label, parent                                                                                                                                         |
| Voltage         | Gauge       | hw_voltage_info                             | additional_information, device_id, fqdn, id, label, parent, voltage_type                                                                                        |
| Voltage         | Gauge       | hw_voltage_status                           | fqdn, id, label, parent                                                                                                                                         |
| Voltage         | Gauge       | hw_voltage_volts                            | fqdn, id, label, parent                                                                                                                                         |
