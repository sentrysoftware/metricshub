keywords: hardware, monitoring, alert, otel, log
description: How ${project.name} triggers alerts and how you can customize the alert's content.

# Alerts

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

An alert is a notification that a hardware problem has occurred, such as a critical low speed on a fan leading to an increase in CPU temperature.

**${project.name}** defines a set of conditions that trigger alerts when failures are detected. These alerts are sent as OpenTelemetry `logs` from the **Hardware Sentry Agent**'s internal `OTLP Exporter` to the **OpenTelemetry Collector**'s internal `OTLP Receiver`.

## Alert Content

The alerts report:

* the host's Fully Qualified Domain Name
* the resource's attributes
* the faulty component with its identifying information (Serial Number, Model, Manufacturer, Bios Version, Driver Version, Physical Address)
* the parent dependency and its identifying information
* the alert severity (WARN, ALARM)
* the alert rule
* the date at which the alert is triggered
* the metric that triggered the alert
* the status information of the component
* the encountered problem, consequence and recommended action
* a complete hardware health report on the faulty component

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
Alert Rule        : hw.network.up == 0

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
Metric: hw.network.up
-----------------------------------------------------------------
Current Value     : 0 (Unplugged)

=================================================================
Metric: hw.status{state="present", hw.type="network"}
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

Alert rules are sets of conditions used to identify the alert's severity and whether the alert should be triggered or not. These alert rules apply to 
 **${project.name}**:

| Monitor         | Metric Name                            | Severity | Default Alert Conditions                       | Attributes                                             |
| --------------- | -------------------------------------- | -------- | ---------------------------------------------- | ------------------------------------------------------ |
| Connector       | hardware_sentry.connector.status       | ALARM    | hardware_sentry.connector.status == 1          | state = `failed`                                       |
| Host            | hardware_sentry.host.up                | ALARM    | hardware_sentry.host.up == 0                   | protocol = `http`                                      |
| Host            | hardware_sentry.host.up                | ALARM    | hardware_sentry.host.up == 0                   | protocol = `ipmi`                                      |
| Host            | hardware_sentry.host.up                | ALARM    | hardware_sentry.host.up == 0                   | protocol = `snmp`                                      |
| Host            | hardware_sentry.host.up                | ALARM    | hardware_sentry.host.up == 0                   | protocol = `ssh`                                       |
| Host            | hardware_sentry.host.up                | ALARM    | hardware_sentry.host.up == 0                   | protocol = `wbem`                                      |
| Host            | hardware_sentry.host.up                | ALARM    | hardware_sentry.host.up == 0                   | protocol = `wmi`                                       |
| Battery         | hw.battery.charge                      | WARN     | hw.battery.charge <= 0.5                       |                                                        |
| Battery         | hw.battery.charge                      | ALARM    | hw.battery.charge <= 0.3                       |                                                        |
| Battery         | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `battery` state = `present`                  |
| Battery         | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `battery` state = `degraded`                 |
| Battery         | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `battery` state = `failed`                   |
| Blade           | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `blade` state = `present`                    |
| Blade           | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `blade` state = `degraded`                   |
| Blade           | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `blade` state = `failed`                     |
| CPU             | hw.errors                              | ALARM    | hw.errors >= 1                                 | hw.type = `cpu`                                        |
| CPU             | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `cpu` state = `predicted_failure`            |
| CPU             | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `cpu` state = `present`                      |
| CPU             | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `cpu` state = `degraded`                     |
| CPU             | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `cpu` state = `failed`                       |
| CPU Core        | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `cpu_core` state = `present`                 |
| CPU Core        | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `cpu_core` state = `degraded`                |
| CPU Core        | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `cpu_core` state = `failed`                  |
| Disk Controller | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `disk_controller` battery_state = `degraded` |
| Disk Controller | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `disk_controller` battery_state = `failed`   |
| Disk Controller | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `disk_controller` state = `degraded`         |
| Disk Controller | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `disk_controller` state = `failed`           |
| Disk Controller | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `disk_controller` state = `present`          |
| Enclosure       | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `enclosure` state = `open`                   |
| Enclosure       | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `enclosure` state = `present`                |
| Fan             | hw.fan.speed                           | ALARM    | hw.fan.speed == 0                              |                                                        |
| Fan             | hw.fan.speed                           | WARN     | hw.fan.speed <= 500                            |                                                        |
| Fan             | hw.fan.speed_ratio                     | ALARM    | hw.fan.speed_ratio == 0                        |                                                        |
| Fan             | hw.fan.speed_ratio                     | WARN     | hw.fan.speed_ratio <= 0.05                     |                                                        |
| Fan             | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `fan` state = `present`                      |
| Fan             | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `fan` state = `degraded`                     |
| Fan             | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `fan` state = `failed`                       |
| GPU             | hw.errors                              | ALARM    | hw.errors >= 1                                 | hw.type = `gpu` type = `corrected`                     |
| GPU             | hw.errors                              | ALARM    | hw.errors >= 1                                 | hw.type = `gpu` type = `all`                           |
| GPU             | hw.gpu.memory.utilization              | WARN     | hw.gpu.memory.utilization >= 0.9               |                                                        |
| GPU             | hw.gpu.memory.utilization              | ALARM    | hw.gpu.memory.utilization >= 0.95              |                                                        |
| GPU             | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `gpu` state = `predicted_failure`            |
| GPU             | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `gpu` state = `present`                      |
| GPU             | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `gpu` state = `degraded`                     |
| GPU             | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `gpu` state = `failed`                       |
| LED             | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `led` state = `degraded`                     |
| LED             | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `led` state = `failed`                       |
| Logical Disk    | hw.errors                              | ALARM    | hw.errors >= 1                                 | hw.type = `logical_disk`                               |
| Logical Disk    | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `logical_disk` state = `present`             |
| Logical Disk    | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `logical_disk` state = `degraded`            |
| Logical Disk    | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `logical_disk` state = `failed`              |
| LUN             | hw.lun.paths                           | ALARM    | hw.lun.paths < 1                               | type = `available`                                     |
| LUN             | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `lun` state = `present`                      |
| LUN             | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `lun` state = `degraded`                     |
| LUN             | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `lun` state = `failed`                       |
| Memory Module   | hw.errors                              | ALARM    | hw.errors >= 1                                 | hw.type = `memory`                                     |
| Memory Module   | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `memory` state = `predicted_failure`         |
| Memory Module   | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `memory` state = `present`                   |
| Memory Module   | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `memory` state = `degraded`                  |
| Memory Module   | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `memory` state = `failed`                    |
| Network Card    | hw.network.bandwidth.utilization       | WARN     | hw.network.bandwidth.utilization >= 0.8        |                                                        |
| Network Card    | hw.network.error_ratio                 | WARN     | hw.network.error_ratio >= 0.2                  |                                                        |
| Network Card    | hw.network.error_ratio                 | ALARM    | hw.network.error_ratio >= 0.3                  |                                                        |
| Network Card    | hw.network.up                          | WARN     | hw.network.up == 0                             |                                                        |
| Network Card    | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `network` state = `present`                  |
| Network Card    | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `network` state = `degraded`                 |
| Network Card    | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `network` state = `failed`                   |
| Other           | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `other_device` state = `present`             |
| Other           | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `other_device` state = `degraded`            |
| Other           | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `other_device` state = `failed`              |
| Physical Disk   | hw.physical_disk.endurance_utilization | WARN     | hw.physical_disk.endurance_utilization <= 0.05 | state = `remaining`                                    |
| Physical Disk   | hw.physical_disk.endurance_utilization | ALARM    | hw.physical_disk.endurance_utilization <= 0.02 | state = `remaining`                                    |
| Physical Disk   | hw.errors                              | ALARM    | hw.errors >= 1                                 | hw.type = `physical_disk`                              |
| Physical Disk   | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `physical_disk` state = `predicted_failure`  |
| Physical Disk   | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `physical_disk` state = `present`            |
| Physical Disk   | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `physical_disk` state = `degraded`           |
| Physical Disk   | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `physical_disk` state = `failed`             |
| Power Supply    | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `power_supply` state = `present`             |
| Power Supply    | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `power_supply` state = `degraded`            |
| Power Supply    | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `power_supply` state = `failed`              |
| Power Supply    | hw.power_supply.utilization            | WARN     | hw.power_supply.utilization >= 0.9             |                                                        |
| Power Supply    | hw.power_supply.utilization            | ALARM    | hw.power_supply.utilization >= 0.99            |                                                        |
| Robotics        | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `robotics` state = `present`                 |
| Robotics        | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `robotics` state = `degraded`                |
| Robotics        | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `robotics` state = `failed`                  |
| Tape Drive      | hw.errors                              | ALARM    | hw.errors >= 1                                 | hw.type = `tape_drive`                                 |
| Tape Drive      | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `tape_drive` state = `needs_cleaning`        |
| Tape Drive      | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `tape_drive` state = `needs_cleaning`        |
| Tape Drive      | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `tape_drive` state = `present`               |
| Tape Drive      | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `tape_drive` state = `degraded`              |
| Tape Drive      | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `tape_drive` state = `failed`                |
| Temperature     | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `temperature` state = `present`              |
| Temperature     | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `temperature` state = `degraded`             |
| Temperature     | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `temperature` state = `failed`               |
| Virtual Machine | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `vm` state = `present`                       |
| Virtual Machine | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `vm` state = `degraded`                      |
| Virtual Machine | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `vm` state = `failed`                        |
| Voltage         | hw.status                              | ALARM    | hw.status == 0                                 | hw.type = `voltage` state = `present`                  |
| Voltage         | hw.status                              | WARN     | hw.status == 1                                 | hw.type = `voltage` state = `degraded`                 |
| Voltage         | hw.status                              | ALARM    | hw.status == 1                                 | hw.type = `voltage` state = `failed`                   |

## Customizing Alert Content

You can customize the content of alerts by addinf macros in the `hardwareProblemTemplate` parameter in the `config/hws-config.yaml` file. See the procedure detailed in the [Hardware Problem Template](configuration/configure-agent.md#Alert_Settings) section.

The default alert content template is:

```
Hardware problem on ${FQDN} with ${MONITOR_NAME}.${NEWLINE}${NEWLINE}${ALERT_DETAILS}${NEWLINE}${NEWLINE}${FULLREPORT}
```

The following macros can be used to obtain more details about the problem. They will be replaced at runtime.

| Macro                   | Description                                                                                                                                                                                                                             |
| ----------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `${MONITOR_NAME}`       | Name of the monitor that triggered the alert. Example: Fan: 1.1 (CPU1)                                                                                                                                                                  |
| `${MONITOR_ID}`         | Unique identifier of the monitor that triggered the alert.                                                                                                                                                                              |
| `${MONITOR_TYPE}`       | Type of the monitor that triggered the alert. Example: Physical Disk                                                                                                                                                                    |
| `${PARENT_ID}`          | Identifier of the parent that the faulty instance is attached to.                                                                                                                                                                       |
| `${METRIC_NAME}`        | Name of the metric that triggered the alert. Example: hw.status{state="failed", hw.type = "battery"}                                                                                                                                    |
| `${METRIC_VALUE}`       | Value of the metric that triggered the alert. Example: 1 (Failed)                                                                                                                                                                       |
| `${SEVERITY}`           | Severity of the alert (ALARM, WARN)                                                                                                                                                                                                     |
| `${ALERT_RULE}`         | Alert conditions that triggered the alert. Example: hw.status{state="failed", hw.type = "battery"} == 1                                                                                                                                 |
| `${ALERT_DATE}`         | ISO date time at which the alert triggered.                                                                                                                                                                                             |
| `${CONSEQUENCE}`        | Description of the possible consequence of the detected problem. Example: The temperature of the chip, component or device that was cooled by this fan should grow quickly. This can lead to severe hardware damage and system crashes. |
| `${RECOMMENDED_ACTION}` | Recommended action to solve the problem. Example: Check if the fan is no longer cooling the system. If so, replace the fan.                                                                                                             |
| `${PROBLEM}`            | Description of the problem encountered by the monitor. Example: The speed of this fan is critically low (1503 rpm).                                                                                                                     |
| `${ALERT_DETAILS}`      | Severity, alert rule, problem, consequence and recommended action.                                                                                                                                                                      |
| `${FULLREPORT}`         | Full hardware health report about the monitor that triggered the alert.                                                                                                                                                                 |
| `${NEWLINE}`            | Linefeed. This is useful to produce multi-line information.                                                                                                                                                                             |

## Receiving Alerts

To receive **${project.name}**'s alerts, your observability platlform (`Exporter`) must support the OpenTelemetry `logs` pipeline and needs to be declared in the `service:pipelines:logs:exporters` list in the `config/otel-config.yaml` file:

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
