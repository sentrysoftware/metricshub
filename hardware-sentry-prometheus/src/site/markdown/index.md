keywords: about, hardware connector library, grafana
description: Hardware Sentry Exporter for Prometheus collects health and performance metrics of more than 250 platforms and exposes them in a format Prometheus understands.

# What is **${project.name}**?

**${project.name}** is designed to collect the health and performance metrics from almost any server and external storage device available on the market. This impressive technical achievement is possible thanks to the **Hardware Connector Library**, a built-in library of more than 250 connectors that describe how to discover hardware components and detect failures.

![**${project.name}** Architecture](./images/mat_prom_architecture_diagram.png)

Once installed and configured, **${project.name}** collects metrics from the monitored servers and storage devices using different protocols (HTTP, SSH, SNMP, OS Commands, WBEM, WMI, etc.) and exposes them in a format Prometheus understands.

Any platform that includes built-in support for Prometheus (Grafana, BMC Helix Operations Management, DataDog, Splunk, etc.) can leverage the information collected by **${project.name}**. As an integration example, Sentry Software provides <a href="https:www.sentrysoftware.com/downloads/products-for-prometheus.html" target="_blank">Sustainable IT dashboards</a> for Grafana.