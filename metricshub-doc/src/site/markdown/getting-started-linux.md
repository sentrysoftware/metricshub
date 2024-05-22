keywords: quick start, getting started
description: Short step-by-step instructions to follow for installing and configuring MetricsHub

# Quick Start - Linux

This quick start guide walks you through the step-by-step instructions you should complete for collecting metrics and pushing them to your observability back-end on a Linux OS.

After completing this tutorial, you'll be able to:
- Download and launch MetricsHub.
- Download and launch Prometheus.
- Configure localhost and Prometheus in MetricsHub.
- Collect data from localhost on MetricsHub and receive it on Prometheus.

## Download MetricsHub

1. Download the `metricshub-linux-${project.version}.tar.gz` latest version from [MetricsHub Releases](https://github.com/sentrysoftware/metricshub/releases/).

2. Open the terminal using `Ctrl + alt + t` 
3. Unzip `metricshub-linux-${project.version}.tar.gz` under `/opt` using the following commands.
    ```shell
    sudo tar -xf ~/Downloads/metricshub-linux-${project.version}.tar.gz -C /opt/
    ```
    - MetricsHub will be installed under `/opt/metricshub`.

## Download Prometheus

1. Download Prometheus using the following command:

   ```shell
   cd /opt && sudo curl -LO url -LO https://github.com/prometheus/prometheus/releases/download/v{version}/prometheus-{version}.linux-{architecture}.tar.gz
   ```

2. Extract it in `/opt/prometheus` using the following command.

   ```shell
   sudo mkdir -p /opt/prometheus && sudo tar -xzvf prometheus-{version}.linux-{architecture}.tar.gz -C prometheus --strip-components=1
   ```
   - Make sure to replace `{version}` and `{architecture}` by the prometheus version and your processor architecture.
   - Prometheus will be installed under `/opt/prometheus`.

## MetricsHub Agent Configuration

### Create a Configuration File

1. Create a configuration file using the following command:
   ```shell
   sudo cp /opt/metricshub/lib/config/metricshub-example.yaml /opt/metricshub/lib/config/metricshub.yaml
   ```

### Host Configuration

The configuration file `/opt/metricshub/lib/config/metricshub.yaml` contains resource configuration examples for various protocols such as Http, Snmp, Ssh, Ipmi, Wbem, Wmi, WinRm. For example, you can configure your localhost using the Wmi protocol under `resources` as follows:

```yaml
resources:
  localhost:
    attributes:
      host.name: localhost
      host.type: linux
    protocols:
      osCommand:
        timeout: 120
```
### Prometheus Configuration on `metricshub.yaml`

To enable MetricsHub to stream metrics to Prometheus, add the following configuration lines in `/opt/metricshub/lib/config/metricshub.yaml` under the `otel` section:

```yaml
otel:
  otel.exporter.otlp.metrics.endpoint: http://localhost:9090/api/v1/otlp/v1/metrics
  otel.exporter.otlp.metrics.protocol: http/protobuf
```

## Launch Prometheus and MetricsHub
### Launch Prometheus

1. Navigate to the Prometheus directory using terminal:
    - Open the CLI using the shortcut Ctrl+Alt+t
    - Once the CLI is open, use the `cd` command to access the directory where Prometheus is installed:

    ```shell
    cd "/opt/prometheus"
    ```

1. Run Prometheus:
    - In the Prometheus directory, run the following command to launch Prometheus:
    ```shell
    sudo ./prometheus --config.file=prometheus.yml --web.console.templates=consoles --web.console.libraries=console_libraries --storage.tsdb.retention.time=10m --storage.tsdb.path=/ --web.enable-lifecycle --web.enable-remote-write-receiver --web.route-prefix=/ --enable-feature=exemplar-storage --enable-feature=otlp-write-receiver
    ```

4. Open Prometheus UI:
    - Open your browser and go to the following URL: [localhost:9090](http://localhost:9090).

### Launch Metricshub Agent

To start the MetricsHub service, open the terminal and run the following commands:

```shell
cd /opt/metricsHub/bin
sudo ./service
```

This will start MetricsHub with the configuration file located at `/opt/metricsHub/lib/config/metricshub.yaml`.

### Find metrics on Prometheus
To check whether the metrics are correctly received in Prometheus, search for any metric that starts with `metricshub_` or `hw_` in the expression search bar, then click on execute.

   ![Prometheus interface](images/prometheus-interface.png)

### Check Logs

After launching the Agent, a log file is created for each configured host, in addition to a global MetricsHub log file. The logs can be found at:

```makefile
/opt/metricshub/lib/logs
```

The log level can be configured in the `/opt/metricsHub/lib/config/metricshub.yaml` file. 

![Logging level configuration](images/log-level.png)

Set the `loggerLevel` parameter to:
- `all`, `trace`, or `debug` for more comprehensive details.
- `error` or `fatal` to focus on identifying critical issues.
- Other logging levels such as `info` or `warn`.

### Common Errors

1. **Wrong Indentation**

    If the indentation is not respected in the `metricshub.yaml` configuration file, the MetricsHub agent will not start.

    ![Wrong Indentation](images/wrong-indentation.png)

    An error log file `metricshub-agent-global-error-{timestamp}.log` will be generated in the logs directory.

    ![MetricsHub Agent global error log file](images/metricshub-agent-global-error-log.png)

    This will include the following exception:

    ![Wrong Indentation Exception](images/wrong-indentation-exception.png)

2. **Wrong Host Configuration**

    If a non-existent or unreachable host is configured, a log file will be generated for it named `metricshub-agent-{hostname}-{timestamp}.log`. Within the logs, a specific log entry will indicate:

    ```css
    [o.s.m.e.c.h.NetworkHelper] Hostname {hostname} - Could not resolve the hostname to a valid IP address. The host is considered remote.
    ```

    If the host is correct, make sure to test your network, and check if it is reachable by pinging it.


3. **Problem During Connectors Deserialization**

    If you modified a connector, or tried to create a new connector that does not parse, MetricsHub will not be able to run for any host. The global log file named `metricshub-agent-global--{timestamp}.log` will include the following exception:

    ![Deserialization Exception](images/deserialization-exception.png)

    To remedy this, make sure to use only the original connectors that parse correctly.
