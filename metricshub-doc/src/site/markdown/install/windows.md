keywords: install, enterprise, community
description: How to install MetricsHub on Windows.

# Installation

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

## Enterprise Edition

### Download

From [MetricsHub's Web site](https://metricshub.com), download **metricshub-enterprise-windows-1.0.00.msi**.

### Install
Double-click the `.msi` file you previously downloaded. The Installation Wizard will automatically start and guide you through the installation process.

When complete, the MetricsHub's files are deployed to the destination folder (by default under `C:\Program Files\MetricsHub`) and the MetricsHubEnterprise Agent is started as a service and appears in services.msc.

MetricsHub operates using the configuration located in the `ProgramData\MetricsHub` directory

### Configure

In the `C:\ProgramData\MetricsHub\config\metricshub.yaml` file, configure:

* the [resources to be monitored](./configuration/configure-agent.html#configure-monitored-resources)
* the [OpenTelemetry Protocol endpoint](configuration/configure-agent.md#otlp-endpoint) that will receive the MetricsHub signals.

To assist with the setup process, the configuration example `.\config\metricshub-example.yaml` is provided for guidance in the installation directory (typically, `C:\Program Files\MetricsHub`).

### Start

To start the **MetricsHub Enterprise** service, open **services.msc** and start the **MetricsHub Enterprise** service.

### Uninstall

To uninstall **MetricsHub Enterprise**, double-click the **metricshub-enterprise-windows-1.0.00.msi** file and click **Remove** when prompted.

## Community Edition

### Download

Download the Windows package, `metricshub-windows-${project.version}.zip`, from the [MetricsHub Release v${project.version}](https://github.com/sentrysoftware/metricshub/releases/tag/v${project.version}) page.

### Install

Unzip the content of `metricshub-windows-${project.version}.zip` into a program folder, like `C:\Program Files`. There is no need to create a specific subdirectory for `MetricsHub` as the zip archive already contains a `MetricsHub` directory.

> Note: You will need administrative privileges to unzip into `C:\Program Files`.

### Configure

In the `C:\ProgramData\MetricsHub\config\metricshub.yaml` file, configure:

* the [resources to be monitored](./configuration/configure-agent.html#configure-monitored-resources)
* the [OpenTelemetry Protocol endpoint](configuration/configure-agent.md#otlp-endpoint) that will receive the MetricsHub signals.

To assist with the setup process, the configuration example `.\config\metricshub-example.yaml` is provided for guidance in the installation directory (typically, `C:\Program Files\MetricsHub`).

### Start

To start **MetricsHub Service** in `CMD.EXE` or [`Windows Terminal`](https://www.microsoft.com/en-us/p/windows-terminal/9n0dx20hk701?activetab=pivot:overviewtab), run the command below:

```shell-session
c:\> cd "c:\Program Files\MetricsHub"
c:\Program Files\MetricsHub> MetricsHubServiceManager
```

> Note: Run `CMD.EXE` or `Windows Terminal` with elevated privileges (**Run As Administrator**).

This will start **MetricsHub** with the default configuration file `C:\ProgramData\MetricsHub\config\metricshub.yaml`.

Run the command below to start **MetricsHub** with an alternate configuration file:

```shell-session
c:\> cd "c:\Program Files\MetricsHub"
c: \Program Files\MetricsHub> MetricsHubServiceManager --config <PATH>
```

Example:

```shell-session
c:\> cd "c:\Program Files\MetricsHub"
c:\Program Files\MetricsHub> MetricsHubServiceManager --config C:\ProgramData\MetricsHub\config\my-metricshub.yaml
```

To start **MetricsHub** as a **Windows service**, run the following commands under the installation folder (assuming the product has been installed in `C:\Program Files`):

```shell-session
c:\> cd "c:\Program Files\MetricsHub"
c:\Program Files\MetricsHub>
c:\Program Files\MetricsHub> service-installer install MetricsHub "c:\Program Files\MetricsHub\MetricsHubServiceManager.exe"
c:\Program Files\MetricsHub> service-installer set MetricsHub AppDirectory "c:\Program Files\MetricsHub"
c:\Program Files\MetricsHub> service-installer set MetricsHub DisplayName MetricsHub
c:\Program Files\MetricsHub> service-installer set MetricsHub Start SERVICE_AUTO_START
```

The service will appear as `MetricsHub` in the `services.msc` console.

### Stop

**Interactive Terminal**

To stop the **MetricsHub Service** manually, use the keyboard shortcut `CTRL+C`. This will interrupt the running process and terminate the **MetricsHub Service**.

**Background Process**

If the **MetricsHub Service** is running in the background, execute the `taskkill` command as follows:

```batch
taskkill /F /IM MetricsHubServiceManager.exe
```

**Service**

To stop the **MetricsHub Service** started as a **Windows service**:

1. Run `services.msc` to access all the Windows services.
2. In the Services window, locate the `MetricsHub`service you manually created.
3. Right-click the `MetricsHub` service and click **Stop**.

### Uninstall

1. Stop the **MetricsHub Service**.
2. Navigate to the folder where **MetricsHub** is installed (e.g., `C:\Program Files`) and delete the entire `MetricsHub` folder.

If the **MetricsHub Service** was set up as a **Windows Service**, run the following command to remove it:

  ```batch
  sc delete MetricsHub
  ```