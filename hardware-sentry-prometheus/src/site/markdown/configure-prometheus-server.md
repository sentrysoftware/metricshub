keywords: job, configuration, prometheus server, examples, job_name, metrics_path
description: How to configure single and multi jobs with Hardware Sentry Prometheus Exporter.

# Configure a Prometheus Server

Refer to [Prometheus documentation](https://prometheus.io/docs/prometheus/latest/configuration/configuration/) for detailed information.

Configure your Prometheus server,  ```prometheus.yml```, to collect metrics from targets.

Here are basic Prometheus configuration examples:

## Single job configuration

|Parameter | Description |
|---------|-----|
|job_name|The job name assigned to scraped metrics by default (`job=<job_name>`).|
|honor_timestamps|Timestamps of the metrics exposed by the target (default: true).|
|scrape_interval|How frequently to scrape targets (default: 120s).|
|scrape_timeout|How long until a scrape request times out (default: 30s).|
|metrics_path|The HTTP resource path on which to fetch metrics from targets (**mandatorily ```/metrics```)**.|
|scheme:|Configures the protocol scheme used for requests.|
|static_configs|List of labeled statically configured targets for this job.|
|targets|Target name(s) and port(s) (default 8080).|

Example:
```
  - job_name: hardware_prometheus
    honor_timestamps: true
    scrape_interval: 120s
    scrape_timeout: 30s
    metrics_path: /metrics
    scheme: http
    static_configs:
    - targets:
      - nb-docker:8080
      - rt-docker:8080
```

**${project.name}** creates a single http instance: ```http://nb-docker:8080/metrics``` to expose collected metrics.

## Multiple job configuration

To configure metrics from multiple jobs, identify each job with its ```job_name``` and define its specific ```metrics_path```.

Example:

```
  - job_name: hardware_prometheus_prod 
    honor_timestamps: true
    scrape_interval: 120s
    scrape_timeout: 30s
    metrics_path: /metrics/prod
    scheme: http
    static_configs:
    - targets:
      - nb-docker:8080

  - job_name: hardware_prometheus_test
      honor_timestamps: true
      scrape_interval: 120s
      scrape_timeout: 300s
      metrics_path: /metrics/test
      scheme: http
      static_configs:
      - targets:
        - nb-docker:8080
```

**${project.name}** creates the HTTP instances: ```http://nb-docker:8080/metrics/prod``` and ```http://nb-docker:8080/metrics/test``` to respectively expose metrics collected from *prod* and *test* hosts.

Metrics are returned once the whole collect is completed. This process may be time-consuming when a large number of targets are scrapped. To retrieve metrics for a specific target, simply specify it name in the ```job_name``` (example: ```job_name: hardware_prometheus_test```).

**${project.name}** collects metrics according to the defined ```scrape_interval``` and returns results immediately, so you get real-time data upon each collect.

