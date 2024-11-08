keywords: grafana, monitor
description: How to collect data from the Grafana health API with MetricsHub.

# Monitoring a Grafana Service

## Overview

If the connectors available with **MetricsHub** do not collect the metrics you need, you can configure one or several monitors to obtain this data from your resource and specify its corresponding attributes and metrics in **MetricsHub**.

In the example below, we created a monitor that collects data from the Grafana health API and map the output to **MetricsHub**'s attributes and metrics. 

## Procedure

To collect data from the Grafana health API:

1. In the `config/metricshub.yaml` file, we first created the `grafana-service` service group, specified its attributes and the protocol to use to connect to this service group:

    ```yaml
    service-group:  
      grafana-service:
        attributes:
          service.name: Grafana
          host.name: hws-demo.sentrysoftware.com
        protocols:
          http:
            https: true
            port: 443
    ```

2. We then specified the job to be performed. In our example, we opted for `simple` for straightforward monitoring tasks:  

    ```yaml
      monitors:
        grafana:
          simple: 
    ```
3. We specified the  Grafana health API data source as follows:

    ```yaml
              sources:
                grafanaHealth:
                  type: http
                  path: /api/health
                  method: get
                  header: "Accept: application/json"
                  computes:
                  - type: json2Csv
                    entryKey: /
                    properties: commit;database;version
                    separator: ;
                  - type: translate
                    column: 3
                    translationTable:
                      ok: 1
                      default: 0
    ```
4. Finally, we specified how the collected metrics are mapped to **MetricsHub**'s monitoring model:

    ```yaml
    service-group:  
      grafana-service:
        attributes:
          service.name: Grafana
          host.name: hws-demo.sentrysoftware.com
        protocols:
          http:
            https: true
            port: 443
        monitors:
          grafana:
            simple: 
              sources:
                grafanaHealth:
                  type: http
                  path: /api/health
                  method: get
                  header: "Accept: application/json"
                  computes:
                  - type: json2Csv
                    entryKey: /
                    properties: commit;database;version
                    separator: ;
                  - type: translate
                    column: 3
                    translationTable:
                      ok: 1
                      default: 0
              mapping:
                source: ${esc.d}{source::grafanaHealth}
                attributes:
                  id: $2
                  service.instance.id: $2
                  service.version: $4
                metrics:
                  grafana.db.state: $3
    ```

We came up to this version of the `config/metricshub.yaml` configuration file:

```yaml
service-group:  
  grafana-service:
    attributes:
      service.name: Grafana
      host.name: hws-demo.sentrysoftware.com
    protocols:
      http:
        https: true
        port: 443
    monitors:
      grafana:
        simple: 
          sources:
            grafanaHealth:
              type: http
              path: /api/health
              method: get
              header: "Accept: application/json"
              computes:
              - type: json2Csv
                entryKey: /
                properties: commit;database;version
                separator: ;
              - type: translate
                column: 3
                translationTable:
                  ok: 1
                  default: 0
          mapping:
            source: ${esc.d}{source::grafanaHealth}
            attributes:
              id: $2
              service.instance.id: $2
              service.version: $4
            metrics:
              grafana.db.state: $3
```