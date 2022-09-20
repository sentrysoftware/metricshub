keywords: install, upgrade
description: Where to download the installation package of the ${project.name}.

# Installation

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

You can install **${project.name}** on the operating system of your choice as they are equally supported.

> Note: Starting from v2.0.00, you can retrieve hardware information exposed by WMI from Linux and MacOs systems through the Windows Remote Management (WinRM) protocol.

## Prerequisites

**${project.name}** requires Java Runtime Environment (JRE) version 11 or higher.

The **Hardware Sentry Agent** will use the `$JAVA_HOME` environment variable to determine that path to the JRE. If `$JAVA_HOME` is not set, it will use any `java` executable found in the `$PATH`.

Run the following command to verify the version installed on:

* Docker:

  ```bash
  docker exec <containerId> java -version
  ```

* Linux or MacOS:

  ```bash
  $JAVA_HOME/bin/java -version
  ```

* On Windows:

  ```bash
  %JAVA_HOME%\bin\java -version
  ```

If needed, you can download the latest versions of the Java Runtime Environment from [Adoptium (formerly AdoptOpenJDK)](https://adoptium.net/).

## Install

### On Docker

#### Download

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

- **${project.artifactId}-${project.version}-docker.tar.gz**

#### Install

First, unzip and untar the content of **${project.artifactId}-${project.version}-docker.tar.gz** into a docker directory, like **/docker**. There is no need to create a specific subdirectory for `hws-otel-collector` as the archive already contains an **hws-otel-collector** directory.

```shell-session
/ $ cd /docker
/docker $ sudo tar xf /tmp/${project.artifactId}-${project.version}-docker.tar.gz
```

Then, build the docker image using the following command:

```shell-session
/ $ cd /docker/hws-otel-collector
/docker/hws-otel-collector $ sudo docker build -t hws-otel-collector:latest .
```

#### Configure

There are 2 configuration files:

- [**./otel/otel-config.yaml**](configuration/configure-otel.md): to specify where the _OpenTelemetry Collector_ should send the collected data
- [**./config/hws-config.yaml**](configuration/configure-agent.md): to specify the hosts to monitor and their credentials

Before starting **${project.name}**, make sure to configure [**./otel/otel-config.yaml**](configuration/configure-otel.md), since a restart of the _Collector_ is required to take into account its changes.

#### Start

You can start the **${project.name}** with the below command:

```shell-session
/$ cd /docker/hws-otel-collector
/docker/hws-otel-collector$ sudo docker run --name=hws-otel -p 8888:8888 -p 4317:4317 -p 13133:13133 hws-otel-collector:latest
```

This will start the **${project.name}** with:

* The default **Hardware Sentry Agent** configuration file: **./config/hws-config.yaml**.
* The default _OpenTelemetry Collector_ configuration file: **./otel/otel-config.yaml**.

You can start the **${project.name}** with an alternate configuration file path with the command bellow:

```shell-session
/$ cd /docker/hws-otel-collector
/docker/hws-otel-collector$ sudo docker run --name=hws-otel -p 8888:8888 -p 4317:4317 -p 13133:13133 -v /docker/hws-otel-collector/config:/opt/hws-otel-collector/config hws-otel-collector:latest
```

See [Ports and Firewalls](#Ports_and_Firewalls) for port details.

**Docker Compose Example**

You can start the **${project.name}** with docker-compose:

```shell-session
/docker/hws-otel-collector$ sudo docker-compose up -d --build
```

Example docker-compose.yaml

```yaml
version: "2.1"
services:
  hws-otel-collector:
    build: .                                        # for image we will use ``image: sentrysoftware/hws-otel-collector:latest``
    container_name: hws-otel-collector
    ports:
      - 8888:8888                                   # OpenTelemetry Collector Exporter
      - 4317:4317                                   # OpenTelemetry Collector gRPC Receiver
      - 13133:13133                                 # OpenTelemetry Collector HealthCheck
    volumes:
      - ./logs:/opt/hws-otel-collector/logs         # redirects logs to ./logs folder
      - ./config:/opt/hws-otel-collector/config     # redirects config folder to ./config
    restart: unless-stopped
```

### On Linux

#### Download

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

- **${project.artifactId}-${project.version}-linux-amd64.tar.gz**

#### Install

Unzip and untar the content of **${project.artifactId}-${project.version}-linux-amd64.tar.gz** into a program directory, like **/usr/local** or **/opt**. There is no need to create a specific subdirectory for `hws-otel-collector` as the archive already contains an **hws-otel-collector** directory.

```shell-session
/ $ cd /usr/local
/usr/local $ sudo tar xf /tmp/${project.artifactId}-${project.version}-linux-amd64.tar.gz
```

#### Configure

There are 2 configuration files:

- [**./otel/otel-config.yaml**](configuration/configure-otel.md): to specify where the _OpenTelemetry Collector_ should send the collected data
- [**./config/hws-config.yaml**](configuration/configure-agent.md): to specify the hosts to monitor and their credentials

Before starting the **${project.name}**, make sure to configure [**./otel/otel-config.yaml**](configuration/configure-otel.md), since a restart of the _Collector_ is required to take into account its changes.

#### Start

You can start the **${project.name}** with the below command:

```bash
/usr/local/hws-otel-collector/bin/hws-agent
```

This will start the **${project.name}** with:

* The default **Hardware Sentry Agent** configuration file: **./config/hws-config.yaml**.
* The default _OpenTelemetry Collector_ configuration file: **./otel/otel-config.yaml**.

You can start the **${project.name}** with an alternate **Hardware Sentry Agent**'s configuration file with the below command:

```bash
/usr/local/hws-otel-collector/bin/hws-agent --config=<PATH>
```

Example:

```shell-session
/$ cd /usr/local/hws-otel-collector
/usr/local/hws-otel-collector$ bin/hws-agent --config=config/my-hws-config.yaml
```

### On MacOS (Apple Silicon)

#### Download

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

- **${project.artifactId}-${project.version}-darwin-arm64.tar.gz**

#### Install

Unzip and untar the content of **${project.artifactId}-${project.version}-darwin-arm64.tar.gz** into a program directory, like **/Library** or **/opt**. There is no need to create a specific subdirectory for `hws-otel-collector` as the archive already contains an **hws-otel-collector** directory.

```shell-session
/ $ cd /Library
/Library $ sudo tar xf /tmp/${project.artifactId}-${project.version}-darwin-arm64.tar.gz
```

#### Configure

There are 2 configuration files:

- [**./otel/otel-config.yaml**](configuration/configure-otel.md): to specify where the _OpenTelemetry Collector_ should send the collected data
- [**./config/hws-config.yaml**](configuration/configure-agent.md): to specify the hosts to monitor and their credentials

Before starting **${project.name}**, make sure to configure [**./otel/otel-config.yaml**](configuration/configure-otel.md), since a restart of the _Collector_ is required to take into account its changes.

#### Start

You can start the **${project.name}** with the below command:

```bash
/Library/hws-otel-collector/bin/hws-agent
```

This will start the **${project.name}** with:

* The default **Hardware Sentry Agent** configuration file: **./config/hws-config.yaml**.
* The default _OpenTelemetry Collector_ configuration file: **./otel/otel-config.yaml**.

You can start the **${project.name}** with an alternate **Hardware Sentry Agent**'s configuration file with the below command:

```bash
/Library/hws-otel-collector/bin/hws-agent --config=<PATH>
```

Example:

```shell-session
/$ cd /Library/hws-otel-collector
/Library/hws-otel-collector$ bin/hws-agent --config=config/my-hws-config.yaml
```

### On MacOS (x86)

#### Download

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

- **${project.artifactId}-${project.version}-darwin-amd64.tar.gz**

#### Install

Unzip and untar the content of **${project.artifactId}-${project.version}-darwin-amd64.tar.gz** into a program directory, like **/Library** or **/opt**. There is no need to create a specific subdirectory for `hws-otel-collector` as the archive already contains an **hws-otel-collector** directory.

```shell-session
/ $ cd /Library
/Library $ sudo tar xf /tmp/${project.artifactId}-${project.version}-darwin-amd64.tar.gz
```

#### Configure

There are 2 configuration files:

- [**./otel/otel-config.yaml**](configuration/configure-otel.md): to specify where the _OpenTelemetry Collector_ should send the collected data
- [**./config/hws-config.yaml**](configuration/configure-agent.md): to specify the hosts to monitor and their credentials

Before starting **${project.name}**, make sure to configure [**./otel/otel-config.yaml**](configuration/configure-otel.md), since a restart of the _Collector_ is required to take into account its changes.

#### Start

You can start the **${project.name}** with the below command:

```bash
/Library/hws-otel-collector/bin/hws-agent
```

This will start the **${project.name}** with:

* The default **Hardware Sentry Agent** configuration file: **./config/hws-config.yaml**.
* The default _OpenTelemetry Collector_ configuration file: **./otel/otel-config.yaml**.

You can start the **${project.name}** with an alternate configuration file with the below command:

```bash
/Library/hws-otel-collector/bin/hws-agent --config=<PATH>
```

Example:

```shell-session
/$ cd /Library/hws-otel-collector
/Library/hws-otel-collector$ bin/hws-agent --config=config/my-hws-config.yaml
```

### On Windows

#### Download

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

- **${project.artifactId}-${project.version}-windows-amd64.zip**

#### Install

Unzip the content of **${project.artifactId}-${project.version}-windows-amd64.tar.gz** into a program folder. There is no need to create a specific subfolder for `hws-otel-collector` as the archive already contains an **hws-otel-collector** folder.

> Note: You will need administrative privileges to unzip into **C:\Program Files**.

#### Configure

There are 2 configuration files:

- [**./otel/otel-config.yaml**](configuration/configure-otel.md): to specify where the _OpenTelemetry Collector_ should send the collected data
- [**./config/hws-config.yaml**](configuration/configure-agent.md): to specify the hosts to monitor and their credentials

and two configuration examples:

- **./otel/otel-config-example.yaml**, a configuration example of the _OpenTelemetry Collector_.
- **./config/hws-config-example.yaml**, a configuration example of the **Hardware Sentry Agent**.

Before starting **${project.name}**, make sure to configure [**./otel/otel-config.yaml**](configuration/configure-otel.md), since a restart of the _Collector_ is required to take into account its changes.

#### Start

Start **${project.name}** in **CMD.EXE** or [Windows Terminal](https://www.microsoft.com/en-us/p/windows-terminal/9n0dx20hk701?activetab=pivot:overviewtab) with elevated privilages (**Run As Administrator**).

```batch
c:
cd "Program Files"
cd hws-otel-collector
bin\hws-agent
```

This will start the **${project.name}** with:

* The default **Hardware Sentry Agent** configuration file: **./config/hws-config.yaml**.
* The default _OpenTelemetry Collector_ configuration file: **./otel/otel-config.yaml**.

You can start the **${project.name}** with an alternate configuration file with the below command:

```batch
"c:\Program Files\hws-otel-collector\bin\hws-agent" --config="c:\Program Files\hws-otel-collector\config\my-hws-config.yaml"
```

#### Start As a Service

It is recommended to install and run the **${project.name}** as a _Windows Service_.

Download and install the [very latest version of NSSM](https://nssm.cc/download) so that it's available in your `%PATH%` (to make things easier).

Run the below command to create the service (assuming the product has been installed in **c:\Program Files**):

```batch
nssm install hws-otel "c:\Program Files\hws-otel-collector\bin\hws-agent.cmd"
nssm set hws-otel DisplayName "Hardware Sentry OpenTelemetry Collector"
nssm set hws-otel AppDirectory "c:\Program Files\hws-otel-collector"
nssm set hws-otel Start SERVICE_AUTO_START
```

The service appears in **services.msc**:

![**${project.name}** running as a service on Windows](images/hws-otel-win-service.png)

## Upgrade

> Warning: It is recommended to make a backup copy of the `hws-keystore.p12` file stored in `hws-otel-collector\security` if you previously encrypted your passwords as specified in [Encrypting Passwords](security/passwords.md).

Stop **${project.name}**, unzip the installation package into your existing folder and start **${project.name}**.

## Post-install

### Verify

Verify that **${project.name}** is properly running as explained in the [Health Check section](troubleshooting/status.md).

### Ports and Firewalls

**${project.name}** opens several TCP ports for listening. None of these ports need to be open to the outside network, as they are used internally only.

| Component                                    | Port      | Required                                                    |
| -------------------------------------------- | --------- | ----------------------------------------------------------- |
| _OpenTelemetry Collector_ OTLP gRPC Receiver | TCP/4317  | Used internally only                                        |
| _OpenTelemetry Collector_ HealthCheck        | TCP/13133 | Optional, if you need to verify the status of the collector |
| _OpenTelemetry Collector_ Exporter           | TCP/8888  | Used internally only                                        |
