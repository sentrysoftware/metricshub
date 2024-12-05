keywords: grafana, monitor
description: How to monitor the health of a service with MetricsHub.

# Monitoring the Health of a Service

## Overview

Many online services, REST APIs, and micro-services provide an availability or health check API. Available through HTTP, they often use a `/health` or `/api/health` endpoint as a convention, but there is no official industry-wide standard that mandates this exact path.

The `/health` endpoint is a common pattern, especially in systems adhering to microservices architectures, where each service typically has a simple endpoint to report its health status. While it’s widely used, implementations vary:

1. **Common Endpoints**: `/health`, `/api/health`, `/status`, or `/ping`.
2. **Standard Protocols**: Some use the OpenAPI or JSON API specifications to define response formats, though many simply return `200 OK` or JSON objects with basic health information.
3. **Cloud-Native Standards**: Some standards like Kubernetes probes (e.g., liveness and readiness probes) use these endpoints to automate health checks and load balancing decisions.

In the example below, we create a monitor in **MetricsHub** that collects data from the [Grafana health API](https://grafana.com/docs/grafana/latest/developers/http_api/other/#returns-health-information-about-grafana) running on our demo system (`m8b-demo.metricshub.com`) and map the result to OpenTelemetry attributes and metrics.

## Procedure

To collect data from the Grafana health API:

1. In the `config/metricshub.yaml` file, we first declare the `m8b-demo-service` resource, specify its `service.name` and `host.name` attributes, following [OpenTelemetry semantic conventions](https://opentelemetry.io/docs/specs/semconv/resource/), and HTTP as the protocol to connect to this resource:

    ```yaml
        resources:
          m8b-demo-grafana:
            attributes:
              service.name: Grafana
              host.name: m8b-demo.metricshub.com
            protocols:
              http:
                https: true
                port: 443
    ```

2. We then specify the entity we monitor: `grafana-health` with a `simple` job:  

    ```yaml
            monitors:
              grafana-health:
                simple: 
    ```

3. We specify the Grafana health API as a data source as follows:

    ```yaml
                  sources:
                    # HTTP GET /api/health which returns a JSON
                    grafanaHealthApi:
                      type: http
                      path: /api/health
                      method: get
                      header: "Accept: application/json"
                      computes:
                      # Convert the JSON document into a simple CSV
                      - type: json2Csv
                        entryKey: /
                        properties: commit;database;version
                        separator: ;
                      # Translate the value of "database" in the 2nd column:
                      # - "ok" --> 1
                      # - anything else --> 0
                      - type: translate
                        column: 3
                        translationTable:
                          ok: 1
                          default: 0
    ```

4. Finally, we specify how the collected metrics are mapped to **MetricsHub**'s monitoring model:

    ```yaml
                  mapping:
                    source: ${esc.d}{source::grafanaHealthApi}
                    attributes:
                      id: $2
                      service.instance.id: $2
                      service.version: $4
                    metrics:
                      grafana.db.status{state="ok"}: $3
    ```

Here is the complete YAML configuration to be added to `config/metricshub.yaml` to monitor the health of our Grafana service:

```yaml
    resources:
      m8b-demo-grafana:
        attributes:
          service.name: Grafana
          host.name: m8b-demo.metricshub.com
        protocols:
          http:
            https: true
            port: 443
        monitors:
          grafana-health:
            simple: 
              sources:
                grafanaHealthApi:
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
                source: ${esc.d}{source::grafanaHealthApi}
                attributes:
                  id: $2
                  service.instance.id: $2
                  service.version: $4
                metrics:
                  grafana.db.status{state="ok"}: $3
```