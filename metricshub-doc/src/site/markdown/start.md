keywords: quick start
description: Short step-by-step instructions to follow for installing and configuring MetricsHub

# Quick Start

This quick start guide walks you through the step-by-step instructions you should complete for collecting metrics and pushing them to your observability back-end.

## Step 1: Install MetricsHub

[Install MetricsHub](./install.html) on a system that has network access to the resources to monitor (physical servers, switches, storage systems, application, etc.).

## Step 2: Configure the resources to be monitored

[Add and define the resources](./configuration/configure-agent.html#configure-resources) you wish to monitor in the **config/metricshub.yaml** file.

## Step 3: Send the metrics to an OTLP Receiver

Configure **${solutionName}** to send the collected metrics to the [OTLP receiver](./configuration/configure-agent.html#configure-the-otlp-receiver) of your choice (Datadog Agent, New Relic, Prometheus, and more) or to an OpenTelemetry collector through the `gRPC` or `http/protobuf` protocol.
