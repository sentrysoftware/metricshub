keywords: alerts, rules, thresholds, prometheus
description: ${project.name} ships with pre-made alert rules for Prometheus AlertManager, to trigger alerts when a hardware problem is detected.

# Prometheus AlertManager

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

When **${project.name}** is integrated with [Prometheus Server](prometheus.md), it is recommended to configure *Alert Rules* if you use [Prometheus AlertManager](https://prometheus.io/docs/alerting/latest/alertmanager/).

## Default Rules

**${project.name}** includes the `config/hardware-sentry-rules.yaml` file that contains alert rules for [Prometheus AlertManager](https://prometheus.io/docs/alerting/latest/alertmanager/), for all relevant metrics. These default rules ensure alerts are triggered whenever a problem with the monitored hardware is detected.

## Static vs Dynamic Thresholds

Most of these default alert rules compare the value of a metric to a **static hardcoded value**.

Example of the `hw_battery_charge_ratio` metric which represents the charge percentage of a battery (between 0 and 1):

* a `warn` alert is active when the battery charge is **below 0.5** (50%)
* a `critical` alert is active when the battery charge is **below 0.3** (30%)

> Note: When the value of `hw_battery_charge_ratio` falls below 0.3, both `warn` and `critical` alerts are active, since the condition matches **both** above alert rules.

However, some alerts cannot be configured with hardcoded values, like for temperature sensors. For such metrics, 2 additional metrics have been added to represent the *warning* and *alarm* thresholds. The default alert rules compare the **base metric** to the corresponding **threshold metrics**.

Example of the `hw_temperature_temperature_celsius` metric:

* a `warn` alert is active when the temperature is greater than the value of `hw_temperature_temperature_celsius_warning`
* a `critical` alert is active when the temperature is greater than the value of `hw_temperature_temperature_celsius_alarm`

```yaml
- name: Temperature-Temperature
  rules:
  - alert: Temperature-Temperature-warn
    expr:  hw_temperature_temperature_celsius >= hw_temperature_temperature_celsius_warning
    labels:
      severity: 'warn'
  - alert: Temperature-Temperature-critical
    expr:  hw_temperature_temperature_celsius >= hw_temperature_temperature_celsius_alarm
    labels:
      severity: 'critical'
```

The table below summarizes the metrics that should be compared to their corresponding **dynamic threshold metrics**:

| Base Metric | Dynamic Threshold Metrics |
|---|---|
| `rate(hw_cpu_errors_total[1h])` | `hw_cpu_corrected_errors_warning` <br/> `hw_cpu_corrected_errors_alarm` |
| `hw_fan_speed_rpm` | `hw_fan_speed_rpm_warning` <br/> `hw_fan_speed_rpm_alarm` |
| `hw_fan_speed_ratio` | `hw_fan_speed_ratio_warning` <br/> `hw_fan_speed_ratio_alarm` |
| `rate(hw_logical_disk_errors_total[1h])` | `hw_logical_disk_errors_warning` <br/> `hw_logical_disk_errors_alarm` |
| `hw_lun_available_paths` | `hw_lun_available_paths_warning` |
| `rate(hw_memory_errors_total[1h])` | `hw_memory_errors_warning` <br/> `hw_memory_errors_alarm` |
| `hw_network_card_error_ratio` | `hw_network_card_error_ratio_warning` <br/> `hw_network_card_error_ratio_alarm` |
| `hw_other_device_usage_times` | `hw_other_device_usage_times_warning` <br/> `hw_other_device_usage_times_alarm` |
| `hw_other_device_value` | `hw_other_device_value_warning` <br/> `hw_other_device_value_alarm` |
| `rate(hw_physical_disk_errors_total[1h])` | `hw_physical_disk_errors_warning` <br/> `hw_physical_disk_errors_alarm` |
| `rate(hw_robotics_errors_total[1h])` | `hw_robotics_errors_warning` <br/> `hw_robotics_errors_alarm` |
| `rate(hw_tape_drive_errors_total[1h])` | `hw_tape_drive_errors_warning` <br/> `hw_tape_drive_errors_alarm` |
| `hw_temperature_temperature_celsius` | `hw_temperature_temperature_celsius_warning` <br/> `hw_temperature_temperature_celsius_alarm` |
| `hw_voltage_voltage_volts` | `hw_voltage_voltage_volts_lower` <br/> `hw_voltage_voltage_volts_upper` |

## Install

Copy this file in your `Prometheus` installation folder. In the `prometheus.yaml` file, add the alerting configuration, as shown below:

```yaml
rule_files:
  - hardware-sentry-rules.yaml
```

Restart your Prometheus server to taken the new rules into account.
