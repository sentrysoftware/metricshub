keywords: quick start, getting started
description: Short step-by-step instructions to follow for installing and configuring MetricsHub

# Quick Start - Windows

This quick start guide walks you through the step-by-step instructions you should complete for collecting metrics and pushing them to your observability back-end on a Windows OS.

After completing this tutorial, you'll be able to:
- Download and launch MetricsHub.
- Download and launch Prometheus.
- Configure localhost and Prometheus in MetricsHub.
- Collect data from localhost on MetricsHub and receive it on Prometheus.

## Download MetricsHub

1. Download the `metricshub-windows-${project.version}.zip` latest version from [MetricsHub Releases](https://github.com/sentrysoftware/metricshub/releases/).

2. Unzip `metricshub-windows-${project.version}.zip` under `C:\Program Files`.
    - MetricsHub will be installed under `C:\Program Files\MetricsHub`.

## Download Prometheus

1. Download [Prometheus](https://prometheus.io/download/).

2. Extract it in `C:\Program Files`.
    - Prometheus will be installed under `C:\Program Files\Prometheus`.

## MetricsHub Agent Configuration

### Create a Configuration File

1. From `C:\Program Files\MetricsHub\`, copy `MetricsHub/config/metricshub-example.yaml` to `C:\ProgramData\metricshub\config`.
2. Rename `C:\ProgramData\metricshub\config\metricshub-example.yaml` to `C:\ProgramData\metricshub\config\metricshub.yaml`.

### Host Configuration

The configuration file `C:\ProgramData\metricshub\config\metricshub.yaml` contains resource configuration examples for various protocols such as Http, Snmp, Ssh, Ipmi, Wbem, Wmi, WinRm. For example, you can configure your localhost using the Wmi protocol under `resources` as follows:

```yaml
resources:
  localhost:
    attributes:
      host.name: localhost
      host.type: windows
    protocols:
      wmi:
        timeout: 120
```
### Prometheus Configuration on `metricshub.yaml`

To enable MetricsHub to stream metrics to Prometheus, add the following configuration lines in `C:\ProgramData\metricshub\config\metricshub.yaml` under the `otel` section:

```yaml
otel:
  otel.exporter.otlp.metrics.endpoint: http://localhost:9090/api/v1/otlp/v1/metrics
  otel.exporter.otlp.metrics.protocol: http/protobuf
```

## Launch Prometheus and MetricsHub
### Launch Prometheus

1. Open a CLI **as an Administrator**:
    - Press `Windows + R` on your keyboard to open the Run dialog.
    - In the Run dialog box, type `cmd` and press `Ctrl + Shift + Enter`.
    ![CMD](images/cmd.png)
    - Confirm the prompt by clicking "OK" to proceed.

2. Navigate to the Prometheus directory:
    - Once the CLI is open, use the `cd` command to access the directory where Prometheus is installed:

    ```shell
    cd "C:\Program Files\Prometheus"
    ```

3. Run Prometheus:
    - In the Prometheus directory, run the following command to launch Prometheus:
    ```shell
    prometheus.exe --config.file=prometheus.yml --web.console.templates=consoles --web.console.libraries=console_libraries --storage.tsdb.retention.time=10m --storage.tsdb.path=/ --web.enable-lifecycle --web.enable-remote-write-receiver --web.route-prefix=/ --enable-feature=exemplar-storage --enable-feature=otlp-write-receiver
    ```

4. Open Prometheus UI:
    - Open your browser and go to the following URL: [localhost:9090](http://localhost:9090).

### Launch Metricshub Agent

To start the MetricsHub service, open `CMD.EXE` and run the following commands:

```shell
cd "C:\Program Files\MetricsHub"
MetricsHubServiceManager.exe
```

![MetricsHub Service Manager](images/metricshub-exe.png)

**Note:** Run `CMD.EXE` or Windows Terminal with elevated privileges (Run As Administrator).

This will start MetricsHub with the configuration file located at `C:\ProgramData\MetricsHub\config\metricshub.yaml`.

### Find metrics on Prometheus
To check whether the metrics are correctly received in Prometheus, search for any metric that starts with `metricshub_` in the expression search bar, then click on execute.

   ![Prometheus interface](images/prometheus-interface.png)

### Check Logs

After launching the Agent, a log file is created for each configured host, in addition to a global MetricsHub log file. The logs can be found at:

```makefile
C:\Users\{Username}\AppData\Local\metricshub\logs
```

Replace {Username} with your local system username.

#### Example:

```makefile
C:\Users\BertrandMartin\AppData\Local\metricshub\logs
```

**Note:** The `AppData` folder is hidden by default. To access it on windows 11, follow these steps:

1. Open File Explorer.
2. Click on the "View" tab.
4. Check on Show > "Hidden items".

![Show hidden items](images/show-hidden-items.png)

The log level can be configured in the `C:\ProgramData\metricshub\config\metricshub.yaml` file. 

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
