# Configuration

## Prometheus Server

Refer to [Prometheus documentation](https://prometheus.io/docs/prometheus/latest/configuration/configuration/) for detailed information.

Configure your Prometheus server,  ```(YAML file)```, to collect metrics from targets.

Save the following basic Prometheus configuration as a file named ```prometheus.yml```:

```
Example 1:

  - job_name: hardware_prometheus   # Name added as a label `job=<job_name>` to any timeseries scraped from this config.
    honor_timestamps: true   # Timestamps of the metrics exposed by the target (default).
    scrape_interval: 120s   # Scrape targets every 120 seconds (default).
    scrape_timeout: 30s   # Scrape request times out after 30 seconds (default)
    metrics_path: /metrics   # Path on which to fetch metrics from targets (mandatory)
    scheme: http   # Protocol scheme used for requests
    static_configs:
    - targets:
      - nb-docker:8080   # Target name and port (default 8080)
      - rt-docker:8080
```

**${project.name}** creates the following http endpoint: ```http://nb-docker:8080/metrics``` to expose collected metrics.

```
Example 2:

  - job_name: hardware_prometheus_bacon   # Name added as a label `job=<job_name>` to any timeseries scraped from this config.
    honor_timestamps: true   # Timestamps of the metrics exposed by the target (default).
    scrape_interval: 120s   # Scrape targets every 120 seconds (default).
    scrape_timeout: 30s   # Scrape request times out after 30 seconds (default)
    metrics_path: /metrics/bacon   # Path on which to fetch metrics from targets (mandatory)
    scheme: http   # Protocol scheme used for requests
    static_configs:
    - targets:
      - nb-docker:8080   # Target name and port (default 8080)

  - job_name: hardware_prometheus_ankara   # Name added as a label `job=<job_name>` to any timeseries scraped from this config.
      honor_timestamps: true   # Timestamps of the metrics exposed by the target (default).
      scrape_interval: 120s   # Scrape targets every 120 seconds (default).
      scrape_timeout: 30s   # Scrape request times out after 30 seconds (default)
      metrics_path: /metrics/ankara   # Path on which to fetch metrics from targets (mandatory)
      scheme: http   # Protocol scheme used for requests
      static_configs:
      - targets:
        - nb-docker:8080   # Target name and port (default 8080)
```

**${project.name}** creates the following http endpoints: ```http://nb-docker:8080/metrics/bacon``` and ```http://nb-docker:8080/metrics/ankara``` to respectively expose metrics from collected *bacon* and *ankara* servers.

Metrics are returned once the whole collect is completed. This may take time when your monitored environment contains a large number of targets. To retrieve metrics for a specific target, simply add its hostname to the ```job_name``` argument (ex: ```job_name: hardware_prometheus_ankara```).

**${project.name}** collects metrics according to the defined scrape_interval configuration and returns results immediately so you get real-time data upon each collect.

## Web Server

Run the following command line to launch the Web server:

```
"%JDK11_HOME%"\bin\java.exe -jar hardware-sentry-prometheus-<version>.jar
```
## Hardware Sentry Exporter for Prometheus

Create the configuration file ```hardware-sentry-config.yml``` to monitor one or several targets.

The ```hardware-sentry-config.yml``` configuration file must be saved in ???

The format, intentation and syntax must be strictly respected for **${project.name}** to operate properly. See example below.
 
### Example:

```

targets:

  - target:
      hostname: "ecs1-01"      # Hostname of the target
      type: "LINUX"   # Type of the system (OS or platform type)
    snmp:   # Protocol and credentials used to access the target
      version: "V1"
      community: "public"
      port: 161
      timeout: 120
    selectedConnectors:
      - "MS_HW_DellOpenManage.hdf"   # Name of the Hardware Connector used to scrape the target. Leave empty to enable the automatic detection.
    excludedConnectors: []   # List of connectors that are NOT used to scrape the target (optional)
    unknownStatus: "WARN"   # Status level to apply (OK, WARN or ALARM) when the target' status is unknown and cannot be translated. Default is WARN.
  - target:
      hostname: "ecs1-02"      # Hostname of the target
      type: "STORAGE"   
    http:
      https: "true"
      community: "public"
      port: 443
      timeout: 120
      username: admin
      password: admin
    selectedConnectors:
      - "MS_HW_DellCompellentStorageManager.hdf"
    excludedConnectors: []
    unknownStatus: "WARN"

```