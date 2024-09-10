keywords: quick start, getting started
description: Short step-by-step instructions to follow for installing and configuring MetricsHub in a Linux environment.

# Quick Start - Linux

<!-- MACRO{toc|fromDepth=1|toDepth=1|id=toc} -->

This quick start guide provides step-by-step instructions for operating MetricsHub and Prometheus in a Linux environment, ensuring you can efficiently monitor your systems.

After completing this quick start, you will have:
* MetricsHub and Prometheus installed on your machine
* The **MetricsHub Agent** configured to collect hardware metrics from your local host and push data to Prometheus
* MetricsHub and Prometheus up and running
* Hardware metrics available in Prometheus.

## Step 1: Install MetricsHub

1. Download the latest package `metricshub-linux-${communityVersion}.tar.gz` using `wget` and save it under `/tmp`:
   
   ```shell
   sudo wget -O /tmp/metricshub-linux-${communityVersion}.tar.gz https://github.com/sentrysoftware/metricshub/releases/download/v${communityVersion}/metricshub-linux-${communityVersion}.tar.gz
   ```

2. Run the below command to unzip `/tmp/metricshub-linux-${communityVersion}.tar.gz` under `/opt`:

   ```shell
   sudo tar -xzvf /tmp/metricshub-linux-${communityVersion}.tar.gz -C /opt/
   ```

There is no need to create a specific subdirectory for `metricshub` as the archive already contains a `metricshub` directory.

## Step 2: Install Prometheus

1. Run the below command to download Prometheus:

   ```shell
   sudo wget -O /tmp/prometheus-{version}.linux-{architecture}.tar.gz https://github.com/prometheus/prometheus/releases/download/v{version}/prometheus-{version}.linux-{architecture}.tar.gz
   ```

   where `{version}` and `{architecture}` should be replaced by the prometheus version and processor architecture.

2. Run the below command to extract the package into `/opt/prometheus`:

   ```shell
   sudo mkdir -p /opt/prometheus && sudo tar -xzvf /tmp/prometheus-{version}.linux-{architecture}.tar.gz -C /opt/prometheus --strip-components=1
   ```

> Note: Make sure to use the corresponding Prometheus version and CPU architecture for `{version}` and `{architecture}`. For example, `prometheus-2.52.0.linux-amd64` for version `2.52.0` and `amd64` architecture. Refer to the [Prometheus download site](https://prometheus.io/download/) to find the right Prometheus package.

## Step 3: Configure the MetricsHub Agent

### Create your configuration file

Run the below command to create your configuration file: 

   ```shell
   sudo cp /opt/metricshub/lib/config/metricshub-example.yaml /opt/metricshub/lib/config/metricshub.yaml
   ```

### Configure localhost monitoring

The `metricshub-example.yaml` file you copied already contains the necessary configuration to monitor your localhost through OS Command. The relevant section should look like this:

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

Open the `/opt/metricshub/lib/config/metricshub.yaml` file and search for the above section to verify that this configuration is active.

If you wish to use a protocol other than `osCommand` (such as `HTTP`, `PING`, `SNMP`, `SSH`, `IPMI`, `WBEM` or `WinRM`), refer to the configuration examples provided in `/opt/metricshub/lib/config/metricshub.yaml`.

### Configure Prometheus to receive MetricsHub data

Add the below configuration under the `otel` section to push metrics to Prometheus:

```yaml
otel:
  otel.exporter.otlp.metrics.endpoint: http://localhost:9090/api/v1/otlp/v1/metrics
  otel.exporter.otlp.metrics.protocol: http/protobuf
```

## Step 4: Start Prometheus and MetricsHub

### Start Prometheus

1. Run the below command to access the directory where Prometheus is installed:

    ```shell
    cd "/opt/prometheus"
    ```

1. Run the below command to start Prometheus:
    ```shell
    sudo ./prometheus --config.file=prometheus.yml --web.console.templates=consoles --web.console.libraries=console_libraries --storage.tsdb.retention.time=2h --web.enable-lifecycle --web.enable-remote-write-receiver --web.route-prefix=/ --enable-feature=exemplar-storage --enable-feature=otlp-write-receiver
    ```

4. Type [localhost:9090](http://localhost:9090) in your Web browser.

### Start the Metricshub Agent

Run the below command to start the **MetricsHub Agent**:

```shell
cd /opt/metricsHub/bin
sudo ./service
```

## Step 5: Perform Last Checks

### Verify that metrics are sent to Prometheus

In [Prometheus](http://localhost:9090), search for any metrics starting with `metricshub_` or `hw_` to confirm that data is actually received.

### Check Logs

Several logs are created as soon as the **MetricsHub Agent** is started:

* a global `MetricsHub` log file
* one log file per configured host.

They are stored in `makefile /opt/metricshub/lib/logs`.

You can configure the log level in the `/opt/metricsHub/lib/config/metricshub.yaml` file by setting the `loggerLevel` parameter to:

* `info` for high level information
* `warn` for logging warning messages that indicate potential issues which are not immediately critical
* `all`, `trace`, or `debug` for more comprehensive details
* `error` or `fatal` for identifying critical issues.

The most common errors you may encounter are:

1. **Incorrect Indentation**

    An incorrect indentation in the `metricshub.yaml` file prevents the **MetricsHub Agent** from starting and  generates the following exception in the `metricshub-agent-global-error-{timestamp}.log` file:

    ```
    [2024-04-30T15:56:16,944][ERROR][o.s.m.a.MetricsHubAgentApplication] Failed to start MetricsHub Agent.
    com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.MarkedYAMLException: mapping values are not allowed here in 'reader', line 29, column 16:
        host.type:linux
    ```

2. **Wrong Host Configuration**

    The following entry will be created in the `metricshub-agent-{hostname}-{timestamp}.log` file if the host configured cannot be reached:

    ```css
    [o.s.m.e.c.h.NetworkHelper] Hostname {hostname} - Could not resolve the hostname to a valid IP address. The host is considered remote.
    ```

    If the host is correctly configured, ensure it is reachable by pinging it and testing your network.

3. **Failure to export metrics**

The following error occurs if a local OTLP receiver is unavailable to collect MetricsHub logs:

```
Feb 27, 2024 1:24:26 PM io.opentelemetry.sdk.internal.ThrottlingLogger doLog WARNING: Failed to export metrics. 
Server responded with gRPC status code 2. Error message: Failed to connect to localhost/[0:0:0:0:0:0:0:1]:4317
```

To solve this problem, ensure that the `OTLP` receiver and more specifically the `otel.exporter.otlp.metrics.endpoint` and `otel.exporter.otlp.logs.endpoint` parameters are [correctly set](../configuration/configure-agent.html#configure-the-otlp-receiver) in the `metricshub.yaml` configuration file.
