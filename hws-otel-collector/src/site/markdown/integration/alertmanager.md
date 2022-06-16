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

Example of the `hw_temperature_celsius` metric:

* a `warn` alert is active when the temperature is greater than the value of `hw_temperature_limit_celsius{limit_type="high.degraded"}`
* a `critical` alert is active when the temperature is greater than the value of `hw_temperature_limit_celsius{limit_type="high.critical"}`

```yaml
- name: Temperature
  rules:
  - alert: Temperature-High-warn
    expr: hw_temperature_celsius >= ignoring(limit_type) hw_temperature_limit_celsius{limit_type="high.degraded"}
    labels:
      severity: 'warn'
  - alert: Temperature-High-critical
    expr: hw_temperature_celsius >= ignoring(limit_type) hw_temperature_limit_celsius{limit_type="high.critical"}
    labels:
      severity: 'critical'
```

The table below summarizes the metrics that should be compared to their corresponding **dynamic threshold metrics**:

| Base Metric | Dynamic Threshold Metrics |
|---|---|
| `rate(hw_cpu_errors_total[1h])` | `ignoring(limit_type) hw_cpu_errors_limit{limit_type="degraded"}` <br/> `ignoring(limit_type) hw_cpu_errors_limit{limit_type="critical"}` |
| `hw_fan_speed_rpm` | `ignoring(limit_type) hw_fan_speed_limit_rpm{limit_type="low.degraded"}` <br/> `ignoring(limit_type) hw_fan_speed_limit_rpm{limit_type="low.critical"}` |
| `hw_fan_speed_ratio` | `ignoring(limit_type) hw_fan_speed_ratio_limit{limit_type="low.degraded"}` <br/> `ignoring(limit_type) hw_fan_speed_ratio_limit{limit_type="low.critical"}` |
| `rate(hw_logical_disk_errors_total[1h])` | `ignoring(limit_type) hw_logical_disk_errors_limit{limit_type="degraded"}` <br/> `ignoring(limit_type) hw_logical_disk_errors_limit{limit_type="critical"}` |
| `hw_lun_paths{type="available"}` | `ignoring(limit_type) hw_lun_paths_limit{limit_type="low.degraded"}` |
| `rate(hw_memory_errors_total[1h])` | `ignoring(limit_type) hw_memory_errors_limit{limit_type="degraded"}` <br/> `ignoring(limit_type) hw_memory_errors_limit{limit_type="critical"}` |
| `hw_network_error_ratio` | `ignoring(limit_type) hw_network_error_ratio_limit{limit_type="degraded"}` <br/> `ignoring(limit_type) hw_network_error_ratio_limit{limit_type="critical"}` |
| `hw_other_device_uses` | `ignoring(limit_type) hw_other_device_uses_limit{limit_type="degraded"}` <br/> `ignoring(limit_type) hw_other_device_uses_limit{limit_type="critical"}` |
| `hw_other_device_value` | `ignoring(limit_type) hw_other_device_value_limit{limit_type="degraded"}` <br/> `ignoring(limit_type) hw_other_device_value_limit{limit_type="critical"}` |
| `rate(hw_physical_disk_errors_total[1h])` | `ignoring(limit_type) hw_physical_disk_errors_limit{limit_type="degraded"}` <br/> `ignoring(limit_type) hw_physical_disk_errors_limit{limit_type="critical"}` |
| `rate(hw_robotics_errors_total[1h])` | `ignoring(limit_type) hw_robotics_errors_limit{limit_type="degraded"}` <br/> `ignoring(limit_type) hw_robotics_errors_limit{limit_type="critical"}` |
| `rate(hw_tape_drive_errors_total[1h])` | `ignoring(limit_type) hw_tape_drive_errors_limit{limit_type="degraded"}` <br/> `ignoring(limit_type) hw_tape_drive_errors_limit{limit_type="critical"}` |
| `hw_temperature_celsius` | `ignoring(limit_type) hw_temperature_limit_celsius{limit_type="high.degraded"}` <br/> `ignoring(limit_type) hw_temperature_limit_celsius{limit_type="high.critical"}` |
| `hw_voltage_volts` | `ignoring(limit_type) hw_voltage_limit_volts{limit_type="low.critical"}` <br/> `ignoring(limit_type) hw_voltage_limit_volts{limit_type="high.critical"}` |

## Install

Copy this file in your `Prometheus` installation folder. In the `prometheus.yaml` file, add the alerting configuration, as shown below:

```yaml
rule_files:
  - hardware-sentry-rules.yaml
```

Restart your Prometheus server to taken the new rules into account.
