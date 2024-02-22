keywords: debug
description: How to enable the debug mode of the MetricsHub Agent (core engine).

# Debugging

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

When you are experiencing an issue, you will have to enable the debug mode for the **MetricsHub Agent**.

The **MetricsHub Agent** (core engine) is a Java process which performs the monitoring by connecting to each of the configured resources and retrieving information about their components. Enable its debug mode if there's no data available in your observability platform to obtain these logs:

* `metricshub-agent-global-error-$timestamp.log`: fatal errors such as an application crash upon start-up
* `metricshub-agent-global-$timestamp.log`: agent global information (agent status, user, version, scheduler, yaml parser, etc.)
* `metricshub-agent-$resourceId-$timestamp.log`: resource logs.

## MetricsHub Agent

The **MetricsHub Agent** automatically sets its internal logging system to `error` to capture and record all errors that may arise while it runs. This ensures that important errors are readily available in the log files (`metricshub-agent-*-$timestamp.log` files), making it easier to diagnose and address any issues. If you wish to obtain more comprehensive details, you need to edit the **config/metricshub.yaml** file, add the `loggerLevel` property, and set `loggerlevel` to either `all`, `trace`, `debug`, `info`, `warn`, `error`, or `fatal`. Each level corresponds to a different degree of information. For example, `all`, `trace` and `debug` provide the most comprehensive details, while `error` and `fatal` focus on identifying critical issues.

The configuration can be updated as follows:

```yaml
loggerLevel: debug

resourceGroups:
  <resourceGroupKey>:
    resources:
    # [...]
```

The debug output files are saved by default in the **logs** directory located under the **MetricsHub** directory:

* On Windows, the output files are stored in the **%LOCALAPPDATA%\MetricsHub** folder of the account running the application:
  * When the Local System account starts the MetricsHub Agent service, the output files are stored under **C:\Windows\System32\config\systemprofile\AppData\Local\MetricsHub\logs**.
  * If a specific user starts the MetricsHub Agent service, the output files are stored under **C:\Users\\<username\>\AppData\Local\MetricsHub\logs**.

* On Linux, the output files are stored in the installation directory: **/opt/metricshub/logs**.

To specify a different output directory, edit the **metricshub.yaml** file and add the `outputDirectory` parameter before the `resourceGroups` section:

```yaml
loggerLevel: debug
outputDirectory: C:\Users\<username>\AppData\Local\Temp\logs2021

resourceGroups:
  <resourceGroupKey>:
    resources:
  # [...]
```

Set `loggerlevel` to `off` to disable the debug mode.