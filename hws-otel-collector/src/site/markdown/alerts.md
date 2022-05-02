keywords: hardware, monitoring, alert, otel, log
description: How ${project.name} triggers alerts and how you can customize the alert's content.

# Alerts

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

## Overview

An alert is a notification that a hardware problem has occurred, such as a critical low speed on a fan leading to an increase in CPU temperature.

**${project.name}** defines a set of conditions that trigger alerts when failures are detected. These alerts are sent as OpenTelemetry `logs` from the **Hardware Sentry Agent**'s internal `OTLP Exporter` to the **OpenTelemetry Collector**'s internal `OTLP Receiver`.

## Alert Content

**${project.name}**'s alert reports the following information:

- The host's Fully Qualified Domain Name.
- The resource's attributes.
- The faulty component with its identifying information (Serial Number, Model, Manufacturer, Bios Version, Driver Version, Physical Address).
- The parent dependency and its identifying information.
- The alert severity (WARN, ALARM).
- The alert rule.
- The date at which the alert is triggered.
- The metric that triggered the alert.
- The status information of the component.
- The encountered problem, consequence and recommended action.
- A complete hardware health report on the faulty component.

Here is an example of an alert triggered by an unplugged cable on a network interface. This alert log has been captured using the OpenTelemetry [Logging Exporter](https://github.com/open-telemetry/opentelemetry-collector/tree/main/exporter/loggingexporter):

```
2022-04-21T14:37:57.034+0200	DEBUG	loggingexporter/logging_exporter.go:81	ResourceLog #0
Resource SchemaURL: https://opentelemetry.io/schemas/1.6.1
Resource labels:
     -> agent.host.name: STRING(hws-otel-collector.internal.sentrysoftware.net)
     -> host.id: STRING(netapp9-san)
     -> host.name: STRING(netapp9-san.internal.sentrysoftware.net)
     -> host.type: STRING(storage)
     -> os.type: STRING(storage)
     -> site: STRING(Datacenter 1)
ScopeLogs #0
ScopeLogs SchemaURL: 
InstrumentationScope netapp9-san 
LogRecord #0
Timestamp: 2022-04-21 12:37:47.201 +0000 UTC
Severity: WARN
Body: Hardware problem on netapp9-san.internal.sentrysoftware.net with 0c (FC Port).

Alert Severity    : WARN
Alert Rule        : hw.network_card.link_status == 1

Alert Details
=============
Problem           : The network link is down.
Consequence       : The network traffic (if any) that was processed by this adapter is no longer being handled, or is overloading another network adapter.
Recommended Action: Check that the network cable (if any) is not unplugged or broken/cut, and that it is properly plugged into the network card. Ensure that the network hub/switch/router is working properly.

Hardware Health Report (2022-04-21T14:37:47.201)
================================================

Monitor           : 0c (FC Port)
Type              : Network Card
On Host           : netapp9-san.internal.sentrysoftware.net
Monitor ID        : NetAppREST_networkcard_netapp9-san_netapp9-san-01.0c
Connector Used    : NetAppREST
Parent ID         : NetAppREST_enclosure_netapp9-san_netapp9-san-01
Physical Address  : 50:0a:09:83:80:72:2b:36

This object is attached to: Enclosure: netapp9-san-01 (NetApp FAS2650)
Type              : Enclosure
Manufacturer      : NetApp
Model             : FAS2650
Serial Number     : 651652000067

=================================================================
Metric: hw.network_card.link_status
-----------------------------------------------------------------
Current Value     : 1 (Unplugged)
Unit              : { 0 = Plugged ; 1 = Unplugged }
Alert Rules       : 
 - WARN: hw.network_card.link_status == 1

=================================================================
Metric: hw.network_card.present
-----------------------------------------------------------------
Current Value     : 1 (Present)
Unit              : { 0 = Missing ; 1 = Present }
Alert Rules       : 
 - ALARM: hw.network_card.present == 0
Attributes:
     -> agent.host.name: STRING(hws-otel-collector.internal.sentrysoftware.net)
     -> host.id: STRING(netapp9-san)
     -> host.name: STRING(netapp9-san.internal.sentrysoftware.net)
     -> host.type: STRING(storage)
     -> os.type: STRING(storage)
     -> site: STRING(Datacenter 1)
Trace ID: 
Span ID: 
Flags: 0
```

## Alert Rules

Alert rules are sets of conditions used to identify the alert's severity and whether the alert should be triggered or not.

The following table lists **${project.name}**'s alert rules:

| Monitor         | Metric                                      | Severity | Default Alert Conditions                                         |
| --------------- | ------------------------------------------- | -------- | -------------------------------------------------- |
| Connector       | hw.connector.status                         | ALARM    | hw.connector.status == 2                           |
| Battery         | hw.battery.charge_ratio                     | WARN     | hw.battery.charge_ratio <= 0.5                     |
| Battery         | hw.battery.charge_ratio                     | ALARM    | hw.battery.charge_ratio <= 0.3                     |
| Battery         | hw.battery.present                          | ALARM    | hw.battery.present == 0                            |
| Battery         | hw.battery.status                           | WARN     | hw.battery.status == 1                             |
| Battery         | hw.battery.status                           | ALARM    | hw.battery.status == 2                             |
| Blade           | hw.blade.present                            | ALARM    | hw.blade.present == 0                              |
| Blade           | hw.blade.status                             | WARN     | hw.blade.status == 1                               |
| Blade           | hw.blade.status                             | ALARM    | hw.blade.status == 2                               |
| CPU             | hw.cpu.corrected_errors_total               | ALARM    | hw.cpu.corrected_errors_total >= 1                 |
| CPU             | hw.cpu.predicted_failure                    | WARN     | hw.cpu.predicted_failure == 1                      |
| CPU             | hw.cpu.present                              | ALARM    | hw.cpu.present == 0                                |
| CPU             | hw.cpu.status                               | WARN     | hw.cpu.status == 1                                 |
| CPU             | hw.cpu.status                               | ALARM    | hw.cpu.status == 2                                 |
| CPU Core        | hw.cpu_core.present                         | ALARM    | hw.cpu_core.present == 0                           |
| CPU Core        | hw.cpu_core.status                          | WARN     | hw.cpu_core.status == 1                            |
| CPU Core        | hw.cpu_core.status                          | ALARM    | hw.cpu_core.status == 2                            |
| Disk Controller | hw.disk_controller.battery_status           | WARN     | hw.disk_controller.battery_status == 1             |
| Disk Controller | hw.disk_controller.battery_status           | ALARM    | hw.disk_controller.battery_status == 2             |
| Disk Controller | hw.disk_controller.controller_status        | WARN     | hw.disk_controller.controller_status == 1          |
| Disk Controller | hw.disk_controller.controller_status        | ALARM    | hw.disk_controller.controller_status == 2          |
| Disk Controller | hw.disk_controller.present                  | ALARM    | hw.disk_controller.present == 0                    |
| Enclosure       | hw.enclosure.intrusion_status               | ALARM    | hw.enclosure.intrusion_status == 2                 |
| Enclosure       | hw.enclosure.present                        | ALARM    | hw.enclosure.present == 0                          |
| Fan             | hw.fan.present                              | ALARM    | hw.fan.present == 0                                |
| Fan             | hw.fan.speed_rpm                            | ALARM    | hw.fan.speed_rpm == 0                              |
| Fan             | hw.fan.speed_rpm                            | WARN     | hw.fan.speed_rpm <= 500                            |
| Fan             | hw.fan.speed_ratio                          | ALARM    | hw.fan.speed_ratio == 0                            |
| Fan             | hw.fan.speed_ratio                          | WARN     | hw.fan.speed_ratio <= 0.05                         |
| Fan             | hw.fan.status                               | WARN     | hw.fan.status == 1                                 |
| Fan             | hw.fan.status                               | ALARM    | hw.fan.status == 2                                 |
| GPU             | hw.gpu.corrected_errors_total               | ALARM    | hw.gpu.corrected_errors_total >= 1                 |
| GPU             | hw.gpu.errors_total                         | ALARM    | hw.gpu.errors_total >= 1                           |
| GPU             | hw.gpu.memory_utilization_ratio             | WARN     | hw.gpu.memory_utilization_ratio >= 0.9             |
| GPU             | hw.gpu.memory_utilization_ratio             | ALARM    | hw.gpu.memory_utilization_ratio >= 0.99            |
| GPU             | hw.gpu.predicted_failure                    | WARN     | hw.gpu.predicted_failure == 1                      |
| GPU             | hw.gpu.present                              | ALARM    | hw.gpu.present == 0                                |
| GPU             | hw.gpu.status                               | WARN     | hw.gpu.status == 1                                 |
| GPU             | hw.gpu.status                               | ALARM    | hw.gpu.status == 2                                 |
| GPU             | hw.gpu.used_time_ratio                      | WARN     | hw.gpu.used_time_ratio >= 0.8                      |
| GPU             | hw.gpu.used_time_ratio                      | ALARM    | hw.gpu.used_time_ratio >= 0.9                      |
| LED             | hw.led.color_status                         | WARN     | hw.led.color_status == 1                           |
| LED             | hw.led.color_status                         | ALARM    | hw.led.color_status == 2                           |
| LED             | hw.led.status                               | WARN     | hw.led.status == 1                                 |
| LED             | hw.led.status                               | ALARM    | hw.led.status == 2                                 |
| Logical Disk    | hw.logical_disk.errors_total                | ALARM    | hw.logical_disk.errors_total >= 1                  |
| Logical Disk    | hw.logical_disk.status                      | WARN     | hw.logical_disk.status == 1                        |
| Logical Disk    | hw.logical_disk.status                      | ALARM    | hw.logical_disk.status == 2                        |
| LUN             | hw.lun.available_paths                      | ALARM    | hw.lun.available_paths < 1                         |
| LUN             | hw.lun.status                               | WARN     | hw.lun.status == 1                                 |
| LUN             | hw.lun.status                               | ALARM    | hw.lun.status == 2                                 |
| Memory Module   | hw.memory.errors_total                      | ALARM    | hw.memory.errors_total >= 1                        |
| Memory Module   | hw.memory.error_status                      | WARN     | hw.memory.error_status == 1                        |
| Memory Module   | hw.memory.error_status                      | ALARM    | hw.memory.error_status == 2                        |
| Memory Module   | hw.memory.predicted_failure                 | WARN     | hw.memory.predicted_failure == 1                   |
| Memory Module   | hw.memory.present                           | ALARM    | hw.memory.present == 0                             |
| Memory Module   | hw.memory.status                            | WARN     | hw.memory.status == 1                              |
| Memory Module   | hw.memory.status                            | ALARM    | hw.memory.status == 2                              |
| Network Card    | hw.network_card.bandwidth_utilization_ratio | WARN     | hw.network_card.bandwidth_utilization_ratio >= 0.8 |
| Network Card    | hw.network_card.error_ratio                 | WARN     | hw.network_card.error_ratio >= 0.1                 |
| Network Card    | hw.network_card.error_ratio                 | ALARM    | hw.network_card.error_ratio >= 0.3                 |
| Network Card    | hw.network_card.link_status                 | WARN     | hw.network_card.link_status == 1                   |
| Network Card    | hw.network_card.present                     | ALARM    | hw.network_card.present == 0                       |
| Network Card    | hw.network_card.status                      | WARN     | hw.network_card.status == 1                        |
| Network Card    | hw.network_card.status                      | ALARM    | hw.network_card.status == 2                        |
| Other           | hw.other_device.present                     | ALARM    | hw.other_device.present == 0                       |
| Other           | hw.other_device.status                      | WARN     | hw.other_device.status == 1                        |
| Other           | hw.other_device.status                      | ALARM    | hw.other_device.status == 2                        |
| Physical Disk   | hw.physical_disk.endurance_remaining_ratio  | WARN     | hw.physical_disk.endurance_remaining_ratio <= 0.05 |
| Physical Disk   | hw.physical_disk.endurance_remaining_ratio  | ALARM    | hw.physical_disk.endurance_remaining_ratio <= 0.02 |
| Physical Disk   | hw.physical_disk.errors_total               | ALARM    | hw.physical_disk.errors_total >= 1                 |
| Physical Disk   | hw.physical_disk.predicted_failure          | WARN     | hw.physical_disk.predicted_failure == 1            |
| Physical Disk   | hw.physical_disk.present                    | ALARM    | hw.physical_disk.present == 0                      |
| Physical Disk   | hw.physical_disk.status                     | WARN     | hw.physical_disk.status == 1                       |
| Physical Disk   | hw.physical_disk.status                     | ALARM    | hw.physical_disk.status == 2                       |
| Power Supply    | hw.power_supply.present                     | ALARM    | hw.power_supply.present == 0                       |
| Power Supply    | hw.power_supply.status                      | WARN     | hw.power_supply.status == 1                        |
| Power Supply    | hw.power_supply.status                      | ALARM    | hw.power_supply.status == 2                        |
| Power Supply    | hw.power_supply.used_capacity_ratio         | WARN     | hw.power_supply.used_capacity_ratio >= 0.9         |
| Power Supply    | hw.power_supply.used_capacity_ratio         | ALARM    | hw.power_supply.used_capacity_ratio >= 0.99        |
| Robotics        | hw.robotics.present                         | ALARM    | hw.robotics.present == 0                           |
| Robotics        | hw.robotics.status                          | WARN     | hw.robotics.status == 1                            |
| Robotics        | hw.robotics.status                          | ALARM    | hw.robotics.status == 2                            |
| Tape Drive      | hw.tape_drive.errors_total                  | ALARM    | hw.tape_drive.errors_total >= 1                    |
| Tape Drive      | hw.tape_drive.needs_cleaning                | WARN     | hw.tape_drive.needs_cleaning == 1                  |
| Tape Drive      | hw.tape_drive.needs_cleaning                | ALARM    | hw.tape_drive.needs_cleaning == 2                  |
| Tape Drive      | hw.tape_drive.present                       | ALARM    | hw.tape_drive.present == 0                         |
| Tape Drive      | hw.tape_drive.status                        | WARN     | hw.tape_drive.status == 1                          |
| Tape Drive      | hw.tape_drive.status                        | ALARM    | hw.tape_drive.status == 2                          |
| Temperature     | hw.temperature.status                       | WARN     | hw.temperature.status == 1                         |
| Temperature     | hw.temperature.status                       | ALARM    | hw.temperature.status == 2                         |
| Virtual Machine | hw.vm.status                                | WARN     | hw.vm.status == 1                                  |
| Virtual Machine | hw.vm.status                                | ALARM    | hw.vm.status == 2                                  |
| Voltage         | hw.voltage.status                           | WARN     | hw.voltage.status == 1                             |
| Voltage         | hw.voltage.status                           | ALARM    | hw.voltage.status == 2                             |

## Customizing Alert Content

You can customize the content of alerts by configuring macros in the `hardwareProblemTemplate` parameter in the `config/hws-config.yaml` file. See the procedure detailed in the [Hardware Problem Template](configuration/configure-agent.md#Hardware_Problem_Template) section.

The default alert content template is:

```
Hardware problem on ${FQDN} with ${MONITOR_NAME}.${NEWLINE}${NEWLINE}${ALERT_DETAILS}${NEWLINE}${NEWLINE}${FULLREPORT}
```

The following macros can be used to obtain more details about the problem. They will be replaced at runtime.

| Macro                   | Description                                                                                                                                                                                                                             |
| ----------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `${MONITOR_NAME}`       | Name of the monitor that triggered the alert. Example: Fan: 1.1 (CPU1)                                                                                                                                                          |
| `${MONITOR_ID}`         | Unique identifier of the monitor that triggered the alert.                                                                                                                                                                              |
| `${MONITOR_TYPE}`       | Type of the monitor that triggered the alert. Example: Physical Disk                                                                                                                                                                    |
| `${PARENT_ID}`          | Identifier of the parent that the faulty instance is attached to.                                                                                                                                                                   |
| `${METRIC_NAME}`        | Name of the metric that triggered the alert. Example: hw.battery.status                                                                                                                                                                 |
| `${METRIC_VALUE}`       | Value of the metric that triggered the alert. Example: 2 (ALARM)                                                                                                                                                                    |
| `${SEVERITY}`           | Severity of the alert (ALARM, WARN, INFO)                                                                                                                                                                                           |
| `${ALERT_RULE}`         | Alert conditions that triggered the alert. Example: hw.battery.status == 2                                                                                                                                                          |
| `${ALERT_DATE}`         | ISO date time at which the alert triggered.                                                                                                                                                                                         |
| `${CONSEQUENCE}`        | Description of the possible consequence of the detected problem. Example: The temperature of the chip, component or device that was cooled by this fan should grow quickly. This can lead to severe hardware damage and system crashes. |
| `${RECOMMENDED_ACTION}` | Recommended action to solve the problem. Example: Check if the fan is no longer cooling the system. If so, replace the fan.                                                                                                        |
| `${PROBLEM}`            | Description of the problem encountered by the monitor. Example: The speed of this fan is critically low (1503 rpm).       |
| `${ALERT_DETAILS}`      | Severity, alert rule, problem, consequence and recommended action.                                          |
| `${FULLREPORT}`         | Full hardware health report about the monitor that triggered the alert.                                                                                                                                                                 |
| `${NEWLINE}`            | Linefeed. This is useful to produce multi-line information.                                                                                                                                                                             |

## Receiving Alerts

To receive **${project.name}**'s alerts, your `Exporter` must support the OpenTelemetry `logs` pipeline and needs to be declared in the `service:pipelines:logs:exporters` list in the `config/otel-config.yaml` file:

```yaml

service:
  # ...
  pipelines:
    # ...
    logs:
      receivers: [otlp]
      processors: [memory_limiter, batch, resourcedetection]
      exporters: [logging] # List here the platform of your choice
```
