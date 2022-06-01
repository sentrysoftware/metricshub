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
Alert Rule        : hw.network_card.up == 0

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
Metric: hw.network_card.up
-----------------------------------------------------------------
Current Value     : 0 (Unplugged)

=================================================================
Metric: hw.network_card.status
-----------------------------------------------------------------
Current Value     : 1 (Present)

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

| Monitor         | Metric                                 | Severity | Default Alert Conditions                       | Attribute                        |
| --------------- | -------------------------------------- | -------- | ---------------------------------------------- | -------------------------------- |
| Connector       | hardware_sentry.connector.status       | ALARM    | hardware_sentry.connector.status == 1          | state=failed                     |
| Host            | hardware_sentry.host.up                | ALARM    | hardware_sentry.host.up == 0                   | protocol=http                    |
| Host            | hardware_sentry.host.up                | ALARM    | hardware_sentry.host.up == 0                   | protocol=ipmi                    |
| Host            | hardware_sentry.host.up                | ALARM    | hardware_sentry.host.up == 0                   | protocol=snmp                    |
| Host            | hardware_sentry.host.up                | ALARM    | hardware_sentry.host.up == 0                   | protocol=ssh                     |
| Host            | hardware_sentry.host.up                | ALARM    | hardware_sentry.host.up == 0                   | protocol=wbem                    |
| Host            | hardware_sentry.host.up                | ALARM    | hardware_sentry.host.up == 0                   | protocol=wmi                     |
| Battery         | hw.battery.charge                      | WARN     | hw.battery.charge <= 0.5                       |                                  |
| Battery         | hw.battery.charge                      | ALARM    | hw.battery.charge <= 0.3                       |                                  |
| Battery         | hw.battery.status                      | ALARM    | hw.battery.status == 0                         | state=present                    |
| Battery         | hw.battery.status                      | WARN     | hw.battery.status == 1                         | state=degraded                   |
| Battery         | hw.battery.status                      | ALARM    | hw.battery.status == 1                         | state=failed                     |
| Blade           | hw.blade.status                        | ALARM    | hw.blade.status == 0                           | state=present                    |
| Blade           | hw.blade.status                        | WARN     | hw.blade.status == 1                           | state=degraded                   |
| Blade           | hw.blade.status                        | ALARM    | hw.blade.status == 1                           | state=failed                     |
| CPU             | hw.cpu.errors                          | ALARM    | hw.cpu.errors >= 1                             |                                  |
| CPU             | hw.cpu.status                          | WARN     | hw.cpu.status == 1                             | state=predicted_failure          |
| CPU             | hw.cpu.status                          | ALARM    | hw.cpu.status == 0                             | state=present                    |
| CPU             | hw.cpu.status                          | WARN     | hw.cpu.status == 1                             | state=degraded                   |
| CPU             | hw.cpu.status                          | ALARM    | hw.cpu.status == 1                             | state=failed                     |
| CPU Core        | hw.cpu_core.status                     | ALARM    | hw.cpu_core.status == 0                        | state=present                    |
| CPU Core        | hw.cpu_core.status                     | WARN     | hw.cpu_core.status == 1                        | state=degraded                   |
| CPU Core        | hw.cpu_core.status                     | ALARM    | hw.cpu_core.status == 1                        | state=failed                     |
| Disk Controller | hw.disk_controller.status              | WARN     | hw.disk_controller.status == 1                 | battery_state=degraded           |
| Disk Controller | hw.disk_controller.status              | ALARM    | hw.disk_controller.status == 1                 | battery_state=failed             |
| Disk Controller | hw.disk_controller.status              | WARN     | hw.disk_controller.status == 1                 | state=degraded                   |
| Disk Controller | hw.disk_controller.status              | ALARM    | hw.disk_controller.status == 1                 | state=failed                     |
| Disk Controller | hw.disk_controller.status              | ALARM    | hw.disk_controller.status == 0                 | state=present                    |
| Enclosure       | hw.enclosure.status                    | ALARM    | hw.enclosure.status == 1                       | state=open                       |
| Enclosure       | hw.enclosure.status                    | ALARM    | hw.enclosure.status == 0                       | state=present                    |
| Fan             | hw.fan.status                          | ALARM    | hw.fan.status == 0                             | state=present                    |
| Fan             | hw.fan.speed                           | ALARM    | hw.fan.speed == 0                              |                                  |
| Fan             | hw.fan.speed                           | WARN     | hw.fan.speed <= 500                            |                                  |
| Fan             | hw.fan.speed_ratio                     | ALARM    | hw.fan.speed_ratio == 0                        |                                  |
| Fan             | hw.fan.speed_ratio                     | WARN     | hw.fan.speed_ratio <= 0.05                     |                                  |
| Fan             | hw.fan.status                          | WARN     | hw.fan.status == 1                             | state=degraded                   |
| Fan             | hw.fan.status                          | ALARM    | hw.fan.status == 1                             | state=failed                     |
| GPU             | hw.gpu.errors                          | ALARM    | hw.gpu.errors >= 1                             | type=corrected                   |
| GPU             | hw.gpu.errors                          | ALARM    | hw.gpu.errors >= 1                             | type=all                         |
| GPU             | hw.gpu.memory.utilization              | WARN     | hw.gpu.memory.utilization >= 0.9               |                                  |
| GPU             | hw.gpu.memory.utilization              | ALARM    | hw.gpu.memory.utilization >= 0.99              |                                  |
| GPU             | hw.gpu.status                          | WARN     | hw.gpu.status == 1                             | state=predicted_failure          |
| GPU             | hw.gpu.status                          | ALARM    | hw.gpu.status == 0                             | state=present                    |
| GPU             | hw.gpu.status                          | WARN     | hw.gpu.status == 1                             | state=degraded                   |
| GPU             | hw.gpu.status                          | ALARM    | hw.gpu.status == 1                             | state=failed                     |
| LED             | hw.led.status                          | WARN     | hw.led.status == 1                             | state=degraded                   |
| LED             | hw.led.status                          | ALARM    | hw.led.status == 1                             | state=failed                     |
| Logical Disk    | hw.logical_disk.errors                 | ALARM    | hw.logical_disk.errors >= 1                    |                                  |
| Logical Disk    | hw.logical_disk.status                 | WARN     | hw.logical_disk.status == 1                    | state=degraded                   |
| Logical Disk    | hw.logical_disk.status                 | ALARM    | hw.logical_disk.status == 1                    | state=failed                     |
| LUN             | hw.lun.paths                           | ALARM    | hw.lun.paths < 1                               | type=available                   |
| LUN             | hw.lun.status                          | WARN     | hw.lun.status == 1                             | state=degraded                   |
| LUN             | hw.lun.status                          | ALARM    | hw.lun.status == 1                             | state=failed                     |
| Memory Module   | hw.memory.errors                       | ALARM    | hw.memory.errors >= 1                          |                                  |
| Memory Module   | hw.memory.status                       | WARN     | hw.memory.status == 1                          | state=predicted_failure          |
| Memory Module   | hw.memory.status                       | ALARM    | hw.memory.status == 0                          | state=present                    |
| Memory Module   | hw.memory.status                       | WARN     | hw.memory.status == 1                          | state=degraded                   |
| Memory Module   | hw.memory.status                       | ALARM    | hw.memory.status == 1                          | state=failed                     |
| Network Card    | hw.network.bandwidth.utilization       | WARN     | hw.network.bandwidth.utilization >= 0.8        |                                  |
| Network Card    | hw.network.error_ratio                 | WARN     | hw.network.error_ratio >= 0.1                  |                                  |
| Network Card    | hw.network.error_ratio                 | ALARM    | hw.network.error_ratio >= 0.3                  |                                  |
| Network Card    | hw.network.up                          | WARN     | hw.network.up == 1                             |                                  |
| Network Card    | hw.network.status                      | ALARM    | hw.network.status == 0                         | state=present                    |
| Network Card    | hw.network.status                      | WARN     | hw.network.status == 1                         | state=degraded                   |
| Network Card    | hw.network.status                      | ALARM    | hw.network.status == 1                         | state=failed                     |
| Other           | hw.other_device.status                 | ALARM    | hw.other_device.status == 0                    | state=present                    |
| Other           | hw.other_device.status                 | WARN     | hw.other_device.status == 1                    | state=degraded                   |
| Other           | hw.other_device.status                 | ALARM    | hw.other_device.status == 1                    | state=failed                     |
| Physical Disk   | hw.physical_disk.endurance_utilization | WARN     | hw.physical_disk.endurance_utilization <= 0.05 | state=remaining                  |
| Physical Disk   | hw.physical_disk.endurance_utilization | ALARM    | hw.physical_disk.endurance_utilization <= 0.02 | state=remaining                  |
| Physical Disk   | hw.physical_disk.errors                | ALARM    | hw.physical_disk.errors >= 1                   |                                  |
| Physical Disk   | hw.physical_disk.status                | WARN     | hw.physical_disk.status == 1                   | state=predicted_failure          |
| Physical Disk   | hw.physical_disk.status                | ALARM    | hw.physical_disk.status == 0                   | state=present                    |
| Physical Disk   | hw.physical_disk.status                | WARN     | hw.physical_disk.status == 1                   | state=degraded                   |
| Physical Disk   | hw.physical_disk.status                | ALARM    | hw.physical_disk.status == 1                   | state=failed                     |
| Power Supply    | hw.power_supply.status                 | ALARM    | hw.power_supply.status == 0                    | state=present                    |
| Power Supply    | hw.power_supply.status                 | WARN     | hw.power_supply.status == 1                    | state=degraded                   |
| Power Supply    | hw.power_supply.status                 | ALARM    | hw.power_supply.status == 1                    | state=failed                     |
| Power Supply    | hw.power_supply.utilization            | WARN     | hw.power_supply.utilization >= 0.9             |                                  |
| Power Supply    | hw.power_supply.utilization            | ALARM    | hw.power_supply.utilization >= 0.99            |                                  |
| Robotics        | hw.robotics.status                     | ALARM    | hw.robotics.status == 0                        | state=present                    |
| Robotics        | hw.robotics.status                     | WARN     | hw.robotics.status == 1                        | state=degraded                   |
| Robotics        | hw.robotics.status                     | ALARM    | hw.robotics.status == 1                        | state=failed                     |
| Tape Drive      | hw.tape_drive.errors                   | ALARM    | hw.tape_drive.errors >= 1                      |                                  |
| Tape Drive      | hw.tape_drive.status                   | WARN     | hw.tape_drive.status == 1                      | state=no_needs_cleaning          |
| Tape Drive      | hw.tape_drive.status                   | ALARM    | hw.tape_drive.status == 1                      | state=needs_cleaning_immediately |
| Tape Drive      | hw.tape_drive.status                   | ALARM    | hw.tape_drive.status == 0                      | state=present                    |
| Tape Drive      | hw.tape_drive.status                   | WARN     | hw.tape_drive.status == 1                      | state=degraded                   |
| Tape Drive      | hw.tape_drive.status                   | ALARM    | hw.tape_drive.status == 1                      | state=failed                     |
| Temperature     | hw.temperature.status                  | WARN     | hw.temperature.status == 1                     | state=degraded                   |
| Temperature     | hw.temperature.status                  | ALARM    | hw.temperature.status == 1                     | state=failed                     |
| Virtual Machine | hw.vm.status                           | WARN     | hw.vm.status == 1                              | state=degraded                   |
| Virtual Machine | hw.vm.status                           | ALARM    | hw.vm.status == 1                              | state=failed                     |
| Voltage         | hw.voltage.status                      | WARN     | hw.voltage.status == 1                         | state=degraded                   |
| Voltage         | hw.voltage.status                      | ALARM    | hw.voltage.status == 1                         | state=failed                     |
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
| `${SEVERITY}`           | Severity of the alert (ALARM, WARN)                                                                                                                                                                                           |
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
