keywords: concepts
description: Concepts.

# General Concepts

## Attributes
<!--Previously called extraMetrics-->

## Connectors

Connectors are yaml files which describe how to collect metrics about a specific resource (a host, an application, a service, etc.). There are two types of connectors:

* The **Community Connectors** which are open-source and bring basic monitoring coverage,
* The **Enterprise Connectors** which are a proprietary library of connectors developed by [Sentry Software](https://sentrysoftware.com) which offers extended hardware monitoring.

Anyone is encouraged to create their own connectors and share them with the community.

## Connectors Variables

## Metrics

## Monitors

Custom connector, set in `metricshub.yaml`, which defines how to collect metrics about a specific resource. A monitor defines a job which executes sources, computes, and collects metrics.

## OTLP Receiver

An OTLP receiver acts as an entry point for metrics into an observability back-end or OpenTelemetry Collector.

## Resource Group

Resource group helps organize and manage assets in a structured manner.

## Resource

Generic term to cover anything monitored by **MetricsHub**. A resource can be a host, a service, an application, etc.
