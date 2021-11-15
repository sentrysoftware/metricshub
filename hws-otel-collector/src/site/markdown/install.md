keywords: installation package
description: Where to download the installation package of the Hardware Sentry Exporter for Prometheus.

# Installation

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

## Prerequisites

**${project.name}** requires Java Runtime Environment (JRE) version 11 or higher.

The *OpenTelemetry Collector* will use the `$JAVA_HOME` environment variable to determine that path to the JRE. If `$JAVA_HOME` is not set, it will use any `java` executable found in the `$PATH`.

To verify the version installed on a Linux system, run the following commands:

```bash
$JAVA_HOME/bin/java -version
java -version
```

On Windows, run the following commands:

```batch
%JAVA_HOME%\bin\java -version
java -version
```

If needed, you can download the latest versions of the Java Runtime Environment from [Adoptium (formerly AdoptOpenJDK)](https://adoptium.net/).

## Windows or Linux?

Windows and Linux are equally supported to run **${project.name}**. However, the WMI protocol is implemented on Windows only. This means that you will need to run **Hardware Sentry** on Windows, to monitor another Windows system through WMI.

Please check the [Hardware Connector Library](https://www.sentrysoftware.com/docs/hardware-connectors/latest/index.html) documentation for more details on the required protocols, depending on the targeted platform, and whether you need WMI.

> Note: The product does not support 32-bit systems.

## On Linux

### Download

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

* **${project.artifactId}-${project.version}-linux-amd64.tar.gz**

### Install

Unzip and untar the content of **${project.artifactId}-${project.version}-linux-amd64.tar.gz** into a program directory, like **/usr/local** or **/opt**. There is no need to create a specific subdirectory for `hws-otel-collector` as the zip archive already contains an **hws-otel-collector** directory.

```shell-session
/ $ cd /usr/local
/usr/local $ sudo tar xf /tmp/${project.artifactId}-${project.version}-linux-amd64.tar.gz
```

### Configure

There are 2 configuration files:

* [**./config/otel-config.yaml**](configuration/configure-otel.md): to specify where the *OpenTelemetry Collector* should send the collected data
* [**./config/hws-config.yaml**](configuration/configure-exporter.md): to specify the hosts to monitor and their credentials

Before starting the *OpenTelemetry Collector*, make sure to configure [**./config/otel-config.yaml**](configuration/configure-otel.md), since a restart of the *Collector* is required to take into account its changes.

### Start

You can start the **${project.name}** with the below command:

```bash
/usr/local/hws-otel-collector/bin/hws-otel
```

This will start the **${project.name}** with the default *OpenTelemetry Collector* configuration file: **./config/otel-config.yaml**.

You can start the **${project.name}** with an alternate configuration file with the below command:

```bash
/usr/local/hws-otel-collector/bin/hws-otel --config <PATH>
```

`hws-otel` is just a shell script that ensures the *OpenTelemetry Collector* is run from its home directory. You can choose to execute the *OpenTelemetry Collector* binary directly with the below commands:

```shell-session
/$ cd /usr/local/hws-otel-collector
/usr/local/hws-otel-collector$ bin/hws-otel-collector --config config/my-otel-config.yaml
```

## On Windows

### Download

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

* **${project.artifactId}-${project.version}-windows-amd64.zip**

### Install

Unzip the content of **${project.artifactId}-${project.version}-windows-amd64.tar.gz** into a program folder. There is no need to create a specific subfolder for `hws-otel-collector` as the zip archive already contains an **hws-otel-collector** folder.

> Note: You will need administrative privileges to unzip into **C:\Program Files**.

### Configure

There are 2 configuration files:

* [**./config/otel-config.yaml**](configuration/configure-otel.md): to specify where the *OpenTelemetry Collector* should send the collected data
* [**./config/hws-config.yaml**](configuration/configure-exporter.md): to specify the hosts to monitor and their credentials

Before starting the *OpenTelemetry Collector*, make sure to configure [**./config/otel-config.yaml**](configuration/configure-otel.md), since a restart of the *Collector* is required to take into account its changes.

### Start

Start **${project.name}** in **CMD.EXE** or [Windows Terminal](https://www.microsoft.com/en-us/p/windows-terminal/9n0dx20hk701?activetab=pivot:overviewtab) with elevated privilages (**Run As Administrator**).

```batch
c:
cd "Program Files"
cd hws-otel-collector
bin\hws-otel
```

This will start the **${project.name}** with the default *OpenTelemetry Collector* configuration file: **.\config\otel-config.yaml**.

You can start the **${project.name}** with an alternate configuration file with the below command:

```batch
"c:\Program Files\hws-otel-collector\bin\hws-otel" --config "c:\Program Files\hws-otel-collector\bin\hws-otel\config\otel-config.yaml"
```

### Start As a Service

It is recommended to install and run the **${project.name}** as a *Windows Service* with NSSM.

Download and install the [very latest version of NSSM](https://nssm.cc/download) so that it's available in your `%PATH%` (to make things easier).

Run the below command to create the service (assuming the product has been installed in **c:\Program Files**):

```batch
nssm install hws-otel "c:\Program Files\hws-otel-collector\bin\hws-otel-collector.exe" --config "c:\Program Files\hws-otel-collector\config\otel-config.yaml"
nssm set hws-otel AppDirectory "c:\Program Files\hws-otel-collector"
nssm set hws-otel DisplayName "Hardware Sentry OpenTelemetry Collector"
nssm set hws-otel Start SERVICE_AUTO_START
```

The service will appear as below in **services.msc**:

![**${project.name}** running as a service on Windows](images/hws-otel-win-service.png)

## Verify

To verify that **${project.name}** is properly running, [read the Health Check section](troubleshooting/status.md).

## Ports and Firewalls

**${project.name}** opens several TCP ports for listening. None of these ports need to be open to the outside network, as they are used internally only.

| Component | Port | Required |
|---|---|---|
| **Hardware Sentry Exporter** | TCP/24375 | Used internally only |
| *OpenTelemetry Collector* HealthCheck | TCP/13133 | Optional, if you need to verify the status of the collector |
| *OpenTelemetry Collector* Exporter | TCP/8888 | Used internally only |
