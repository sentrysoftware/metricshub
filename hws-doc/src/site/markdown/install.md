keywords: install, upgrade, firewalls
description: How to install ${solutionName} on Debian Linux, Docker, Red Hat Enterprise Linux, Windows.

# Installation

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

You can install **${solutionName}** on the operating system of your choice as they are equally supported.

> Note: Starting from v2.0.00, you can retrieve hardware information exposed by WMI from Linux and MacOs systems through the Windows Remote Management (WinRM) protocol.

## Install

### On Debian Linux

#### Download

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

* **hws-debian-${project.version}-amd64.deb**

#### Install

Once you have downloaded the Debian package, run the following `dpkg` command:

```shell-session
/ $ cd /usr/local
/usr/local $ sudo dpkg -i hws-debian-${project.version}-amd64.deb
```

When complete, the **${solutionName}**'s files are deployed in `/opt/hws` and the **Hardware Sentry Agent** is started as a service.

#### Configure

There are 2 configuration files:

* [**./otel/otel-config.yaml**](configuration/configure-otel.md): to specify where the _OpenTelemetry Collector_ should send the collected data
* [**./config/hws-config.yaml**](configuration/configure-agent.md): to specify the hosts to monitor and their credentials

Before starting **${solutionName}**, make sure to configure [**./otel/otel-config.yaml**](configuration/configure-otel.md), since a restart of the _Collector_ is required to take into account its changes.

#### Start

To start the **${solutionName}** service, run the command below:

```shell-session
systemctl start hws-agent
```

This will start **${solutionName}** with:

* The default **Hardware Sentry Agent** configuration file: **./config/hws-config.yaml**.
* The default _OpenTelemetry Collector_ configuration file: **./otel/otel-config.yaml**.

You can start **${solutionName}** in an interactive terminal with an alternate **Hardware Sentry Agent**'s configuration file with the command below:

```shell-session
/ $ cd /opt/hws/bin
/opt/hws/bin $ agent --config=<PATH>
```

Example:

```shell-session
/ $ cd /opt/hws/bin
/opt/hws/bin $ agent --config=config/my-hws-config.yaml
```

#### Stop

To stop the **${solutionName}** service, run the command below:

```shell-session
systemctl stop hws-agent
```

#### Uninstall

To uninstall **${solutionName}**, run the command below:

```shell-session
sudo dpkg -r hws
```

### On Docker

#### Download

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

* **hws-debian-${project.version}-docker.tar.gz**

#### Install

First, unzip and untar the content of **hws-debian-${project.version}-docker.tar.gz** into a docker directory, like **/docker**. There is no need to create a specific subdirectory for `hws` as the archive already contains an **hws** directory.

```shell-session
/ $ cd /docker
/docker $ sudo tar xf /tmp/hws-debian-${project.version}-docker.tar.gz
```

Then, build the docker image using the following command:

```shell-session
/ $ cd /docker/hws
/docker/hws $ sudo docker build -t hws:latest .
```

#### Configure

There are 2 configuration files:

* [**./lib/otel/otel-config.yaml**](configuration/configure-otel.md): to specify where the _OpenTelemetry Collector_ should send the collected data
* [**./lib/config/hws-config.yaml**](configuration/configure-agent.md): to specify the hosts to monitor and their credentials

Before starting **${solutionName}**, make sure to configure [**./lib/otel/otel-config.yaml**](configuration/configure-otel.md), since a restart of the _Collector_ is required to take into account its changes.

#### Start

You can start **${solutionName}** with the command below:

```shell-session
/ $ cd /docker/hws
/docker/hws $ sudo docker run --name=hws -p 8888:8888 -p 4317:4317 -p 13133:13133 hws:latest
```

This will start **${solutionName}** with:

* The default **Hardware Sentry Agent** configuration file: **./lib/config/hws-config.yaml**.
* The default _OpenTelemetry Collector_ configuration file: **./lib/otel/otel-config.yaml**.

You can start **${solutionName}** with an alternate configuration file path with the command below:

```shell-session
/ $ cd /docker/hws
/docker/hws $ sudo docker run --name=hws -p 8888:8888 -p 4317:4317 -p 13133:13133 -v /docker/hws/lib/config:/opt/hws/lib/config hws:latest
```

See [Ports and Firewalls](#Ports_and_Firewalls) for port details.

**Docker Compose Example**

You can start **${solutionName}** with docker-compose:

```shell-session
/docker/hws $ sudo docker-compose up -d --build
```

Example docker-compose.yaml

```yaml
version: "2.1"
services:
  hws:
    build: .                                        # for image we will use ``image: sentrysoftware/hws:latest``
    container_name: hws
    ports:
      - 8888:8888                                   # OpenTelemetry Collector Exporter
      - 4317:4317                                   # OpenTelemetry Collector gRPC Receiver
      - 13133:13133                                 # OpenTelemetry Collector HealthCheck
    volumes:
      - ./lib/logs:/opt/hws/lib/logs                # Mount the volume ./lib/logs into /opt/hws/lib/logs in the container
      - ./lib/config:/opt/hws/lib/config            # Mount the volume ./lib/config into /opt/hws/lib/config in the container
      - ./lib/otel:/opt/hws/lib/otel                # Mount the volume ./lib/otel into /opt/hws/lib/otel in the container
    restart: unless-stopped
```

### On Red Hat Enterprise Linux

#### Download

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

* **hws-rhel-${project.version}-1.x86_64.rpm**

#### Install

Once you have downloaded the RPM package, run the following `rpm` command to install **${solutionName}**:

```shell-session
/ $ cd /usr/local
/usr/local $ sudo rpm -i hws-rhel-${project.version}-1.x86_64.rpm
```

When complete, the **${solutionName}**'s files are deployed in `/opt/hws` and the **Hardware Sentry Agent** is started as a service.

#### Configure

There are 2 configuration files:

* [**./otel/otel-config.yaml**](configuration/configure-otel.md): to specify where the _OpenTelemetry Collector_ should send the collected data
* [**./config/hws-config.yaml**](configuration/configure-agent.md): to specify the hosts to monitor and their credentials

Before starting **${solutionName}**, make sure to configure [**./otel/otel-config.yaml**](configuration/configure-otel.md), since a restart of the _Collector_ is required to take into account its changes.

#### Start

To start the **${solutionName}** service, run the command below:

```shell-session
systemctl start hws-agent
```

This will start **${solutionName}** with:

* The default **Hardware Sentry Agent** configuration file: **./config/hws-config.yaml**.
* The default _OpenTelemetry Collector_ configuration file: **./otel/otel-config.yaml**.

You can start **${solutionName}** in an interactive terminal with an alternate **Hardware Sentry Agent**'s configuration file with the command below:

```shell-session
/ $ cd /opt/hws/bin
/opt/hws/bin $ agent --config=<PATH>
```

Example:

```shell-session
/ $ cd /opt/hws/bin
/opt/hws/bin $ agent --config=config/my-hws-config.yaml
```

#### Stop

To stop the **${solutionName}** service, run the command below:

```shell-session
systemctl stop hws-agent
```

#### Uninstall

To uninstall **${solutionName}**, run the command below:

```shell-session
sudo rpm -e hws-${project.version}-1.x86_64
```

### On Windows

#### Download

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

* **hws-windows-${project.version}.msi**

#### Install

Double-click the `.msi` file you previously downloaded. The Installation Wizard will automatically start and guide you through the installation process.

When complete, the **${solutionName}**'s files are deployed to the destination folder (by default under `C:\Program Files\hws`) and the **Hardware Sentry Agent** is started as a service and appears in **services.msc**.

![**${solutionName}** running as a service on Windows](images/hws-win-service.png)

#### Configure

There are 2 configuration files:

* [**./otel/otel-config.yaml**](configuration/configure-otel.md): to specify where the _OpenTelemetry Collector_ should send the collected data
* [**./config/hws-config.yaml**](configuration/configure-agent.md): to specify the hosts to monitor and their credentials

and two configuration examples:

* **./otel/otel-config-example.yaml**, a configuration example of the _OpenTelemetry Collector_.
* **./config/hws-config-example.yaml**, a configuration example of the **Hardware Sentry Agent**.

Before starting **${solutionName}**, make sure to configure [**./otel/otel-config.yaml**](configuration/configure-otel.md), since a restart of the _Collector_ is required to take into account its changes.

#### Start

To start the **${solutionName}** service, open **services.msc** and start the **Hardware Sentry Agent** service.

This will start **${solutionName}** with:

* The default **Hardware Sentry Agent** configuration file: **./config/hws-config.yaml**.
* The default _OpenTelemetry Collector_ configuration file: **./otel/otel-config.yaml**.

You can start **${solutionName}** in an interactive terminal (using **CMD.EXE** or [Windows Terminal](https://www.microsoft.com/en-us/p/windows-terminal/9n0dx20hk701?activetab=pivot:overviewtab)) with an alternate **Hardware Sentry Agent**'s configuration file using the command below:

```batch
c:
cd "Program Files"
cd hws
agent --config="c:\Program Files\hws\config\my-hws-config.yaml"
```

#### Uninstall

To uninstall **${solutionName}**, double-click the **hws-windows-${project.version}.msi** file and click **Remove** when prompted.

## Upgrade

> **Warning**: It is highly recommended to make a backup copy of the `hws-keystore.p12` file stored in `hws/security` if you previously encrypted your passwords as specified in [Encrypting Passwords](security/passwords.md).

If you are upgrading from v2.0.00, perform the actions below before installing **Hardware Sentry** v3.0.00:

**On Windows**:

* If you installed the version 2.0.00 as a Windows service, stop and remove the service before installing **Hardware Sentry** v3.0.00
* If you are running the collector in an interactive terminal, stop the collector process (`hws-otel-collector.exe`) before installing **Hardware Sentry** v3.0.00.

**On Linux**, stop the `hws-otel-collector`.

## Post-install

### Verify

Verify that **${solutionName}** is properly running as explained in the [Health Check section](troubleshooting/status.md).

### Ports and Firewalls

**${solutionName}** opens several TCP ports for listening. None of these ports need to be open to the outside network, as they are used internally only.

| Component                                    | Port      | Required                                                    |
| -------------------------------------------- | --------- | ----------------------------------------------------------- |
| _OpenTelemetry Collector_ OTLP gRPC Receiver | TCP/4317  | Used internally only                                        |
| _OpenTelemetry Collector_ HealthCheck        | TCP/13133 | Optional, if you need to verify the status of the collector |
| _OpenTelemetry Collector_ Exporter           | TCP/8888  | Used internally only                                        |
