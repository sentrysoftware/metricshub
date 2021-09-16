keywords: job, configuration, prometheus server, examples, job_name
description: How to configure the Prometheus server to pull information from the Hardware Sentry Exporter for Prometheus

# Configure a Prometheus Server

**${project.name}** collects metrics from monitored targets and converts them in a format Prometheus understands. To configure the Prometheus server to pull information from **${project.name}**, update the ``prometheus.yml`` configuration file with the following information:

```
  - job_name: hardware_sentry
    scrape_interval: <duration>
    scrape_timeout: <duration>
    static_configs:
    scheme: <http or https>
    - targets: ['<hostname:port_number>' ] 
```

Where

* `<duration>` is a duration matching the regular expression `((([0-9]+)y)?(([0-9]+)w)?(([0-9]+)d)?(([0-9]+)h)?(([0-9]+)m)?(([0-9]+)s)?(([0-9]+)ms)?|0)`. Example: 1d, 1h30m, 5m, 10s
* `<scheme>` is a string that can take the values `http` or `https`. If you are using HTTPS, you need to [enable the HTTPS protocol](./operate.html#Using_specific_arguments).

Example

```
  - job_name: hardware_sentry
    scrape_interval: 2m
    scrape_timeout: 2m
    static_configs:
    - targets: ['centos01:8080']
```

Once the Prometheus server is configured, it starts collecting metrics by connecting to `http://<hostname>:<port_number>/metrics` API endpoint. In our example, `http://centos01:8080/metrics`.

Refer to <a href="https://prometheus.io/docs/prometheus/latest/configuration/configuration/" target="_blank">Prometheus documentation</a> for detailed information about the Prometheus Server configuration.