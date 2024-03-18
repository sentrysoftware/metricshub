keywords: develop, connector, yaml, syntax, community, custom
description: Master connector creation for MetricsHub. Dive into monitoring, from basic to advanced concepts. Perfect for MetricsHub enthusiasts and community contributors.

# Connector Design Guide

<div class="alert alert-warning"><span class="fa-solid fa-person-digging"></span> Documentation under construction...</div>

<!-- MACRO{toc|fromDepth=1|toDepth=1|id=toc} -->

## Introduction

This guide is designed to empower developers with the knowledge and best practices necessary to create effective connectors for **MetricsHub**, leveraging OpenTelemetry to monitor resources efficiently.

## Target Audience

This guide is intended for advanced users of **MetricsHub** and contributors to the [MetricsHub Community Connectors](https://github.com/sentrysoftware/metricshub-community-connectors) project. Readers are expected to have a foundational understanding of monitoring concepts and YAML syntax. The content is tailored for those looking to extend **MetricsHub**'s monitoring capabilities to resources not covered by its built-in connectors.

## What is a Connector?

A connector is a `YAML` file that defines the necessary parameters and logic to monitor specific resources, whether in infrastructure or any environment reachable via the network. It specifies how to collect data, what data to collect, and how to process that data. Through connectors, **MetricsHub** can be extended to support new resources, enriching the monitoring landscape.

## Purpose of a Connector

The primary purposes of a connector are to:

* Accurately identify and detect the resources to be monitored, such as Operating Systems, Hosts, Devices, Containers, Kubernetes clusters, and more.
* Outline the techniques for data collection from these resources, utilizing local commands, network queries, or other relevant methods.
* Establish the format of the gathered data, including metrics and attributes, and detail the processes for transforming this data prior to submission to OpenTelemetry.

## Key Components of a Connector

A connector `YAML` file comprises several sections, each serving a distinct purpose:

* Metadata: Includes information like display name, version, platforms, and dependencies.
* Detection: Defines how to identify the target resource and includes criteria for detection.
* Monitors: Details the monitoring jobs, their types, and how they collect or compute data.
* Sources and Computes: Describe the data sources and computations performed on the collected data.
* Mapping: Describes how to create attributes and metrics
![Connector Overview](../images/connector-overview.png)

## Developing a Connector

The process of developing a connector encompasses several key steps:

* **Resource Identification**: Determining the specific resource and its components you intend to monitor.
* **Protocol Selection**: Opting for the appropriate data collection method from a range of supported protocols such as `HTTP`, `SNMP`, `IPMI`, `WinRM`, `WMI`, `WBEM`, and `SSH`.
* **Connector Configuration**: Creating the YAML file to specify the detection logic, metrics to be collected, and the data processing approach.

By the end of this guide, developers will be equipped with the knowledge to create connectors for **MetricsHub**, enabling the monitoring of a wider range of resources and contributing to the **MetricsHub** community.
