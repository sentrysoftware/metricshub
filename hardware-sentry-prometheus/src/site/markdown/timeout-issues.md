keywords: prometheus server, timeout, issues 
description: How to configure a job for each target in the prometheus.yml configuration file to solve timeout issues.

## Resolving Timeout Issues

Because metrics are returned once the whole collect is completed, you may be facing timeout issues if you configured a large number of targets in the `prometheus.yml` configuration file. To solve these issues, you can configure a job for each target to be scraped as illustrated in the example below:

```
  - job_name: hardware_sentry_host001
    scrape_interval: 2m
    scrape_timeout: 2m
    metrics_path: /metrics/host001
    static_configs:
    - targets:['centos01:8080']

  - job_name: hardware_sentry_host002
    scrape_interval: 2m
    scrape_timeout: 2m
    metrics_path: /metrics/host002
    static_configs:
    - targets:['centos01:8080']
```

In this example, **${project.name}** creates the HTTP instances: `http://centos01:8080/metrics/host001` and `http://centos01:8080/metrics/host002` to respectively expose metrics collected from the `host001` and `host002` hosts.