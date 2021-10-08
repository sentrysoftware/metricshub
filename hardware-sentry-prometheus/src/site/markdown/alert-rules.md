keywords: alerts, rules, thresholds
description: How to configure and use alert rules and get notified when a problem occurs.

# Prerequisites

You must have downloaded **${project.artifactId}-${project.version}.zip** or **${project.artifactId}-${project.version}.tar.gz** from [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/products-for-prometheus.html). 

The package includes the `hardware-sentry-rules.yml` file that contains alert rules for all the monitored hardware component types. Save this file in the folder of your choice, typically where the `hardware-sentry-config.yml` is located.

**${project.name}** rules consists in positioning static or dynamic thresholds that trigger alerts when their conditions are met. Rules are exposed as metrics as shown in the exemples below.

## Static Thresholds

A static threshold consists in a hard limit a metric should not breach. The limit can be a single value or a range. As static threshold do not change over time, it helps you define critical boundaries of normal operation.

For example, the possible statuses of a battery charge can typically be:

    0 (OK) when the battery charge is over 50 % (0.5)
    1 (WARN) when the battery charge is below 50 % (0.5) and above 30 % (0.3)
    2 (ALARM) when the battery charge is below 30 % (0.3)

Example:

```
- name: Battery-Charge
  rules:
  - alert: Battery-Charge-critical
    expr: hw_battery_charge_ratio >= 0 AND hw_battery_charge_ratio < 0.3
    labels:
      severity: 'critical'
  - alert: Battery-Charge-warn
    expr: hw_battery_charge_ratio >= 0 AND hw_battery_charge_ratio <= 0.5
    labels:
      severity: 'warn'
```

**${project.name}** compares the collected value of the battery charge with the predefined thresholds set for this type of device. If the threshold is violated, an alert of the appropriate severity is triggered in Prometheus

## Dynamic Thresholds

A dynamic threshold consists in a limit based on manufacturer's device settings and incoming data. Alerts are dynamically generated when these thresholds are exceeded.

**${project.name}** collects 3 temperature metrics:

hw_temperature_celsius - the current temperature of the device/component
hw_temperature_celsius_warning - the threshold from the manufacturer that triggers a WARN-severity alert when exceed.
hw_temperature_celsius_alarm - the threshold from the manufacturer that triggers a CRITICAL-severity alert when exceed.

The example below shows the rule that enables  **${project.name}** to trigger an alert by comparing the 3 temperature metrics. 

```
- name: Temperature-Temperature
  rules:
  - alert: Temperature-Temperature-warn
    expr:  hw_temperature_celsius >= hw_temperature_celsius_warning
    labels:
      severity: 'warn'
  - alert: Temperature-Temperature-critical
    expr:  hw_temperature_celsius >= hw_temperature_celsius_alarm
    labels:
      severity: 'critical'
```
