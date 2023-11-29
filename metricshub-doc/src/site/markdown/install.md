keywords: install, upgrade, firewalls
description: How to install ${solutionName} on Debian Linux, Docker, Red Hat Enterprise Linux, Windows.

# Installation

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

You can install **${solutionName}** on the operating system of your choice as they are equally supported.

## Install

### On Debian Linux

#### Download

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

* **metricshub-debian-${project.version}-amd64.deb**

#### Install

Once you have downloaded the Debian package, run the following `dpkg` command:

```shell-session
/ $ cd /usr/local
/usr/local $ sudo dpkg -i metricshub-debian-${project.version}-amd64.deb
```

When complete, the **${solutionName}**'s files are deployed in `/opt/metricshub` and the **Hardware Sentry Agent** is started as a service.

#### Configure

You need to set up 2 configuration files in the installation directory (`/opt/metricshub`):

* [**./otel/otel-config.yaml**](configuration/configure-otel.md): to specify where the _OpenTelemetry Collector_ should send the collected data
* [**./config/metricshub-config.yaml**](configuration/configure-agent.md): to specify the hosts to monitor and their credentials

There are two configuration examples in the installation directory (`/opt/metricshub`):

* **./otel/otel-config-example.yaml**, a configuration example of the _OpenTelemetry Collector_.
* **./config/metricshub-example.yaml**, a configuration example of the **MetricsHub Agent**.

Before starting **${solutionName}**, make sure to configure [**./otel/otel-config.yaml**](configuration/configure-otel.md), since a restart of the _Collector_ is required to take into account its changes.

#### Start

To start the **${solutionName}** service, run the command below:

```shell-session
systemctl start metricshub
```

This will start **${solutionName}** with:

* The default **MetricsHub Agent** configuration file: **./config/metricshub-config.yaml**.
* The default _OpenTelemetry Collector_ configuration file: **./otel/otel-config.yaml**.

<p id="debian">You can start <strong>${solutionName}</strong> in an interactive terminal with an alternate <strong>Hardware Sentry Agent</strong>'s configuration file with the command below:</p>

```shell-session
/ $ cd /opt/metricshub/bin
/opt/metricshub/bin $ ./agent --config=<PATH>
```

Example:

```shell-session
/ $ cd /opt/metricshub/bin
/opt/metricshub/bin $ ./agent --config=config/my-metricshub-config.yaml
```

#### Stop

To stop the **${solutionName}** service, run the command below:

```shell-session
systemctl stop metricshub-agent
```

#### Uninstall

To uninstall **${solutionName}**, run the command below:

```shell-session
sudo dpkg -r metricshub
```

### On Docker

#### Download

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

* **metricshub-debian-${project.version}-docker.tar.gz**

#### Install

First, unzip and untar the content of **metricshub-debian-${project.version}-docker.tar.gz** into a docker directory, like **/docker**. There is no need to create a specific subdirectory for `MetricsHub` as the archive already contains an **metricshub** directory.

```shell-session
/ $ cd /docker
/docker $ sudo tar xf /tmp/metricshub-debian-${project.version}-docker.tar.gz
```

Then, build the docker image using the following command:

```shell-session
/ $ cd /docker/metricshub
/docker/metricshub $ sudo docker build -t metricshub:latest .
```

#### Configure

You need to set up 2 configuration files in the docker image directory (`metricshub`):

* [**./lib/otel/otel-config.yaml**](configuration/configure-otel.md): to specify where the _OpenTelemetry Collector_ should send the collected data
* [**./lib/config/metricshub-config.yaml**](configuration/configure-agent.md): to specify the hosts to monitor and their credentials

There are two configuration examples in the docker image directory (`metricshub`):

* **./otel/otel-config-example.yaml**, a configuration example of the _OpenTelemetry Collector_.
* **./config/metricshub-example.yaml**, a configuration example of the **Hardware Sentry Agent**.

Before starting **${solutionName}**, make sure to configure [**./lib/otel/otel-config.yaml**](configuration/configure-otel.md), since a restart of the _Collector_ is required to take into account its changes.

#### Start

You can start **${solutionName}** with the command below:

```shell-session
/ $ cd /docker/metricshub
/docker/metricshub $ sudo docker run --name=metricshub -p 8888:8888 -p 4317:4317 -p 13133:13133 metricshub:latest
```

This will start **${solutionName}** with:

* The default **Hardware Sentry Agent** configuration file: **./lib/config/metricshub-config.yaml**.
* The default _OpenTelemetry Collector_ configuration file: **./lib/otel/otel-config.yaml**.

<p id="docker">You can start <strong>${solutionName}</strong> with an alternate configuration file path with the command below:</p>

```shell-session
/ $ cd /docker/metricshub
/docker/metricshub $ sudo docker run --name=metricshub -p 8888:8888 -p 4317:4317 -p 13133:13133 -v /docker/metricshub/lib/config:/opt/metricshub/lib/config metricshub:latest
```

See [Ports and Firewalls](#Ports_and_Firewalls) for port details.

**Docker Compose Example**

You can start **${solutionName}** with docker-compose:

```shell-session
/docker/metricshub $ sudo docker-compose up -d --build
```

Example docker-compose.yaml

```yaml
version: "2.1"
services:
  metricshub:
    build: .                                        # for image we will use ``image: sentrysoftware/metricshub:latest``
    container_name: metricshub
    ports:
      - 8888:8888                                   # OpenTelemetry Collector Exporter
      - 4317:4317                                   # OpenTelemetry Collector gRPC Receiver
      - 13133:13133                                 # OpenTelemetry Collector HealthCheck
    volumes:
      - ./lib/logs:/opt/metricshub/lib/logs                # Mount the volume ./lib/logs into /opt/metricshub/lib/logs in the container
      - ./lib/config:/opt/metricshub/lib/config            # Mount the volume ./lib/config into /opt/metricshub/lib/config in the container
      - ./lib/otel:/opt/metricshub/lib/otel                # Mount the volume ./lib/otel into /opt/metricshub/lib/otel in the container
    restart: unless-stopped
```

### On Red Hat Enterprise Linux

#### Download

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

* **metricshub-rhel-${project.version}-1.x86_64.rpm**

#### Install

Once you have downloaded the RPM package, run the following `rpm` command:

```shell-session
/ $ cd /usr/local
/usr/local $ sudo rpm -i metricshub-rhel-${project.version}-1.x86_64.rpm
```

When complete, the **${solutionName}**'s files are deployed in `/opt/metricshub` and the **Hardware Sentry Agent** is started as a service.

#### Configure

You need to set up 2 configuration files in the installation directory (`/opt/metricshub`):

* [**./otel/otel-config.yaml**](configuration/configure-otel.md): to specify where the _OpenTelemetry Collector_ should send the collected data
* [**./config/metricshub-config.yaml**](configuration/configure-agent.md): to specify the hosts to monitor and their credentials

There are two configuration examples in the installation directory (`/opt/metricshub`):

* **./otel/otel-config-example.yaml**, a configuration example of the _OpenTelemetry Collector_.
* **./config/metricshub-example.yaml**, a configuration example of the **Hardware Sentry Agent**.

Before starting **${solutionName}**, make sure to configure [**./otel/otel-config.yaml**](configuration/configure-otel.md), since a restart of the _Collector_ is required to take into account its changes.

#### Start

To start the **${solutionName}** service, run the command below:

```shell-session
systemctl start metricshub-agent
```

This will start **${solutionName}** with:

* The default **Hardware Sentry Agent** configuration file: **./config/metricshub-config.yaml**.
* The default _OpenTelemetry Collector_ configuration file: **./otel/otel-config.yaml**.

<p id="redhat"> You can start <strong>${solutionName}</strong> in an interactive terminal with an alternate <strong>Hardware Sentry Agent</strong>'s configuration file with the command below:</p>

```shell-session
/ $ cd /opt/metricshub/bin
/opt/metricshub/bin $ ./agent --config=<PATH>
```

Example:

```shell-session
/ $ cd /opt/metricshub/bin
/opt/metricshub/bin $ ./agent --config=config/my-metricshub-config.yaml
```

#### Stop

To stop the **${solutionName}** service, run the command below:

```shell-session
systemctl stop metricshub-agent
```

#### Uninstall

To uninstall **${solutionName}**, run the command below:

```shell-session
sudo rpm -e metricshub-${project.version}-1.x86_64
```

### On Windows

#### Download

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

* **metricshub-windows-${project.version}.msi**

#### Install

Double-click the `.msi` file you previously downloaded. The Installation Wizard will automatically start and guide you through the installation process.

When complete, the **${solutionName}**'s files are deployed to the destination folder (by default under `C:\Program Files\MetricsHub`) and the **Hardware Sentry Agent** is started as a service and appears in **services.msc**.

![**${solutionName}** running as a service on Windows](images/metricshub-win-service.png)

**${solutionName}** operates using the configuration located in the **ProgramData\metricshub** directory (`C:\ProgramData\metricshub`).

#### Configure

You need to set up 2 configuration files in the **ProgramData\metricshub** directory (`C:\ProgramData\metricshub`):

* [**./otel/otel-config.yaml**](configuration/configure-otel.md): to specify where the _OpenTelemetry Collector_ should send the collected data.
* [**./config/metricshub-config.yaml**](configuration/configure-agent.md): to specify the hosts to monitor and their credentials.

There are two configuration examples in the installation directory (`C:\Program Files\MetricsHub`):

* **./otel/otel-config-example.yaml**, a configuration example of the _OpenTelemetry Collector_.
* **./config/metricshub-example.yaml**, a configuration example of the **Hardware Sentry Agent**.

Before starting **${solutionName}**, make sure to configure [**./otel/otel-config.yaml**](configuration/configure-otel.md), since a restart of the _Collector_ is required to take into account its changes.

#### Start

To start the **${solutionName}** service, open **services.msc** and start the **Hardware Sentry Agent** service.

This will start **${solutionName}** with:

* The default **Hardware Sentry Agent** configuration file: **./config/metricshub-config.yaml**.
* The default _OpenTelemetry Collector_ configuration file: **./otel/otel-config.yaml**.

<p id="windows">You can start <strong>${solutionName}</strong> in an interactive terminal (using <strong>CMD.EXE</strong> or 
<a href="https://www.microsoft.com/en-us/p/windows-terminal/9n0dx20hk701?activetab=pivot:overviewtab">Windows Terminal</a> with an alternate <strong>Hardware Sentry Agent</strong>'s configuration file using the command below:</p>

```batch
c:
cd "Program Files"
cd metricshubricsHub
agent --config="c:\ProgramData\metricshub\config\my-metricshub-config.yaml"
```

#### Uninstall

To uninstall **${solutionName}**, double-click the **metricshub-windows-${project.version}.msi** file and click **Remove** when prompted.

## Upgrade from v3.0.x to ${project.version}

### Debian Linux

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

* **metricshub-debian-${project.version}-amd64.deb**

Run the following command to stop the `metricshub-agent` service:

```shell-session
systemctl stop metricshub-agent
```

If you are running the **Hardware Sentry Agent** in an interactive terminal or as background process, stop the `/opt/metricshub/bin/agent` processes.

Run the following `dpkg` command:

```shell-session
/ $ cd /usr/local
/usr/local $ sudo dpkg -i metricshub-debian-${project.version}-amd64.deb
```

If you are running the **Hardware Sentry Agent** in an interactive terminal or as background process, start the `/opt/metricshub/bin/agent` processes using the command documented <a href="#debian">above</a>.

### Red Hat Enterprise Linux

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

* **metricshub-rhel-${project.version}-1.x86_64.rpm**

Run the following command to stop the `metricshub-agent` service:

```shell-session
systemctl stop metricshub-agent
```

If you are running the **Hardware Sentry Agent** in an interactive terminal or as background process, stop the `/opt/metricshub/bin/agent` processes.

Run the following `rpm` command to upgrade **${solutionName}**:

```shell-session
/ $ cd /usr/local
/usr/local $ sudo rpm -U metricshub-rhel-${project.version}-1.x86_64.rpm
```

If you are running the **Hardware Sentry Agent** in an interactive terminal or as background process, start the `/opt/metricshub/bin/agent` processes using the command documented <a href="#redhat">above</a>.

### Windows

From [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/), download:

* **metricshub-windows-${project.version}.msi**

Stop the **Hardware Sentry Agent** service.

If you are running the **Hardware Sentry Agent** in an interactive terminal or as background process, stop the `agent` processes (default:`C:/Program Files/MetricsHub/agent.exe`).

Double-click the `.msi` file you previously downloaded. The Installation Wizard will automatically start and guide you through the installation process.

If you are running the **Hardware Sentry Agent** in an interactive terminal or as background process, start the `agent` processes (default:`C:/Program Files/MetricsHub/agent.exe`) using the command documented <a href="#windows">above</a>.

### Configuration upgrade

3 metrics need to be renamed to ensure compatibility with the latest version of the dashboards provided by Sentry Software:

| Former Name                        | New Name                   |
| ---------------------------------- | -------------------------- |
| `hw.site.carbon_density_grams`     | `hw.site.carbon_intensity` |
| `hw.site.electricity_cost_dollars` | `hw.site.electricity_cost` |
| `hw.site.pue_ratio`                | `hw.site.pue`              |

If you have upgraded **${solutionName}** from v3.0.x, perform the actions below:

1. Open and edit `metricshub-config.yaml` (**C:\ProgramData\metricshub\config\metricshub-config.yaml** for Windows, **/opt/metricshub/config/metricshub-config.yaml** for Linux)

2. Under the `extraMetrics` section, define the new metrics and assign them the same value as the deprecated metrics as in the below example:

    ```yaml
    extraMetrics:
      hw.site.carbon_density_grams: 230  # DEPRECATED
      hw.site.carbon_intensity: 230 # in g/kWh

      hw.site.electricity_cost_dollars: 0.12  # DEPRECATED
      hw.site.electricity_cost: 0.12 # in $/kWh
      
      hw.site.pue_ratio: 1.8 # DEPRECATED
      hw.site.pue: 1.8
    ```

3. We recommend keeping the deprecated metrics for backward compatibility with existing dashboards and queries that leverage these metrics.

4. Let **${solutionName}** run for a couple weeks before updating your dashboards to use the updated metrics. Check with Sentry Software's support team to get updated dashboards for Grafana, BMC Helix or Datadog, etc.

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
