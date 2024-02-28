keywords: install, upgrade, firewalls
description: How to install ${solutionName} on Debian Linux, Docker, Red Hat Enterprise Linux, Windows.

# Installation

<!-- MACRO{toc|fromDepth=1|toDepth=1|id=toc} -->

You can install **${solutionName}** on the operating system of your choice as they are equally supported.

## On Linux

### Download

From [MetricsHub Release v${project.version}](https://github.com/sentrysoftware/metricshub/releases/tag/v${project.version}), download:
* **metricshub-linux-${project.version}.tar.gz**

### Install

Unzip and untar the content of `metricshub-linux-${project.version}.tar.gz` into a program directory, like `/opt`. There is no need to create a specific subdirectory for `metricshub` as the zip archive already contains a `metricshub` directory.

```shell-session
/ $ cd /opt
/opt $ sudo tar xf /tmp/metricshub-linux-${project.version}.tar.gz
```

### Configure

Under the installation directory `./metricshub`, configure the [`./lib/config/metricshub.yaml`](configuration/configure-agent.md) file to define the resources (hosts) that need to be monitored, along with their respective credentials, and set the [OpenTelemetry Protocol endpoint](configuration/configure-agent.md#otlp-endpoint) for receiving MetricsHub signals.

Find a configuration example for the **MetricsHub Service** at `./lib/config/metricshub-example.yaml` within the installation directory (`./metricshub`).

### Start

To start **${solutionName}** in an interactive terminal, run the command below:

```shell-session
/ $ cd /opt/metricshub/bin
/opt/metricshub/bin $ ./service
```

This will start **${solutionName}** with the default **MetricsHub Service**'s configuration file: `./lib/config/metricshub.yaml`.

You can start **${solutionName}** with an alternate **MetricsHub Service**'s configuration file with the command below:

```shell-session
/ $ cd /opt/metricshub/bin
/opt/metricshub/bin $ ./service --config <PATH>
```

Example:

```shell-session
/ $ cd /opt/metricshub/bin
/opt/metricshub/bin $ ./service --config config/my-metricshub.yaml
```

To start **${solutionName}** as a **Linux service**, follow the steps below:  

* **Create a systemd service file**

  Create a file, e.g., `/etc/systemd/system/metricshub-service.service`, and define the **MetricsHub Service** configuration as the following:

  ```
  [Unit]
  Description=MetricsHub Service

  [Service]
  ExecStart=/opt/metricshub/bin/service
  Restart=on-failure

  [Install]
  WantedBy=multi-user.target
  ```
* **Reload systemd**

  After creating the Linux service file, reload systemd to recognize the new service.

  ```shell-session
  systemctl daemon-reload
  ```
* **Start the MetricsHub Service**

  ```shell-session
  systemctl start metricshub-service
  ```

  To enable the Linux service to start on boot, run the command below:

  ```shell-session
  systemctl enable metricshub-service
  ```

* **Check status**

  To verify that the **MetricsHub Service** is running without errors, run the command below:

  ```shell-session
  systemctl status metricshub-service
  ```

  This will give you information about the current status of your service.

### Stop

**Interactive Terminal**

To stop the **MetricsHub Service** manually in an interactive terminal, use the keyboard shortcut `CTRL+C`. This will interrupt the running process and terminate the **MetricsHub Service**.

**Background Process**

If the **MetricsHub Service** is running in the background, follow these steps to stop it:

1. Identify the process ID (PID) of the MetricsHub Service using the `ps` command:
   ```shell-session
   ps aux | grep service
   ```
2. Note the PID associated with the MetricsHub Service.
3. Terminate the process using the kill command, replacing `<PID>` with the actual process ID:
   ```shell-session
   kill -9 <PID>
   ```

**Service**

To stop the **MetricsHub Service** that is started as a **Linux service**, run the command below (assuming the systemd service file is `/etc/systemd/system/metricshub-service.service`):

```shell-session
systemctl stop metricshub-service
```

### Uninstall

* Stop the **MetricsHub Service**.
* Navigate to the directory where **${solutionName}** is located (e.g., `/opt`) and remove the entire `metricshub` directory.

  ```shell-session
  / $ cd /opt
  /opt $ rm -rf metricshub
  ```
* To remove the **MetricsHub Service** if it is set up as a **Linux Service**, delete the file `/etc/systemd/system/metricshub-service.service` and then reload systemd using the following command:

  ```shell-session
  systemctl daemon-reload
  ```

## On Windows

### Download

From [MetricsHub Release v${project.version}](https://github.com/sentrysoftware/metricshub/releases/tag/v${project.version}), download:
* **metricshub-windows-${project.version}.zip**

### Install

Unzip the content of `metricshub-windows-${project.version}.zip` into a program folder, like `C:\Program Files`. There is no need to create a specific subdirectory for `MetricsHub` as the zip archive already contains a `MetricsHub` directory.

> Note: You will need administrative privileges to unzip into `C:\Program Files`.

### Configure

Configure the [`C:\ProgramData\MetricsHub\config\metricshub.yaml`](configuration/configure-agent.md) file to define the resources (hosts) that need to be monitored, along with their respective credentials, and set the [OpenTelemetry Protocol endpoint](configuration/configure-agent.md#otlp-endpoint) for receiving MetricsHub signals.

Find a configuration example for the **MetricsHub Service** at `.\config\metricshub-example.yaml` within the installation directory (e.g., `C:\Program Files\MetricsHub`).

### Start

To start **MetricsHub Service** in `CMD.EXE` or [`Windows Terminal`](https://www.microsoft.com/en-us/p/windows-terminal/9n0dx20hk701?activetab=pivot:overviewtab), run the command below:

```shell-session
c:\> cd "c:\Program Files\MetricsHub"
c:\Program Files\MetricsHub> MetricsHubServiceManager
```

> Note: Run `CMD.EXE` or `Windows Terminal` with elevated privileges (**Run As Administrator**).

This will start **${solutionName}** with the default **MetricsHub Service**'s configuration file: `C:\ProgramData\MetricsHub\config\metricshub.yaml`.

You can start **${solutionName}** with an alternate **MetricsHub Service**'s configuration file with the command below:

```shell-session
c:\> cd "c:\Program Files\MetricsHub"
c: \Program Files\MetricsHub> MetricsHubServiceManager --config <PATH>
```

Example:

```shell-session
c:\> cd "c:\Program Files\MetricsHub"
c:\Program Files\MetricsHub> MetricsHubServiceManager --config C:\ProgramData\MetricsHub\config\my-metricshub.yaml
```

To start **${solutionName}** as a **Windows service**, run the following commands under the installation folder (assuming the product has been installed in `C:\Program Files`):

```shell-session
c:\> cd "c:\Program Files\MetricsHub"
c:\Program Files\MetricsHub>
c:\Program Files\MetricsHub> service-installer install MetricsHub "c:\Program Files\MetricsHub\MetricsHubServiceManager.exe"
c:\Program Files\MetricsHub> service-installer set MetricsHub AppDirectory "c:\Program Files\MetricsHub"
c:\Program Files\MetricsHub> service-installer set MetricsHub DisplayName MetricsHub
c:\Program Files\MetricsHub> service-installer set MetricsHub Start SERVICE_AUTO_START
```

The service will be named `MetricsHub` and will be visible in the `services.msc` console.

### Stop

**Interactive Terminal**

To stop the **MetricsHub Service** manually, use the keyboard shortcut `CTRL+C`. This will interrupt the running process and terminate the **MetricsHub Service**.

**Background Process**

If the **MetricsHub Service** is running in the background, use the command prompt to stop the process using the `taskkill` command. Open the command prompt and run:

```batch
taskkill /F /IM MetricsHubServiceManager.exe
```

**Service**

To stop the **MetricsHub Service** started as a **Windows service**, follow these steps:

1. Open the Services console by running `services.msc`.
2. In the Services window, locate the manually created service named, for example, `MetricsHub`.
3. Right-click on the MetricsHub service.
4. Select *Stop* from the context menu.

This action will terminate the execution of the **MetricsHub Service** running as a Windows service.

### Uninstall

* Stop the **MetricsHub Service**.
* Navigate to the folder where **${solutionName}** is installed (e.g., `C:\Program Files`) and remove the entire `MetricsHub` folder.
* To remove the **MetricsHub Service** if it is set up as a **Windows Service**, run the following command:
  ```batch
  sc delete MetricsHub
  ```

## On Docker

### Download

From [MetricsHub Release v${project.version}](https://github.com/sentrysoftware/metricshub/releases/tag/v${project.version}), download:
* **metricshub-linux-${project.version}-docker.tar.gz**

### Install

Unzip and untar the content of `metricshub-linux-${project.version}-docker.tar.gz` into a directory, like `/docker`.

```shell-session
/ $ cd /docker
/docker $ sudo tar xf /tmp/metricshub-linux-${project.version}-docker.tar.gz
```

Then, build the docker image using the following command:

```shell-session
/ $ cd /docker/metricshub
/docker/metricshub $ sudo docker build -t metricshub:latest .
```

### Configure

Under the **./metricshub** directory, configure the [`./lib/config/metricshub.yaml`](configuration/configure-agent.md) file to define the resources (hosts) that need to be monitored, along with their respective credentials, and set the [OpenTelemetry Protocol endpoint](configuration/configure-agent.md#otlp-endpoint) for receiving MetricsHub signals.

Find a configuration example for the **MetricsHub Service** at `./lib/config/metricshub-example.yaml` within the image directory (`./metricshub`).

### Start

You can start **${solutionName}** with the command below:

```shell-session
/ $ cd /docker/metricshub
/docker/metricshub $ sudo docker run --name=metricshub metricshub:latest
```

This will start **${solutionName}** with the default **MetricsHub Service**'s configuration file: `./lib/config/metricshub.yaml`.

You can start **${solutionName}** with an alternate **MetricsHub Service**'s configuration file with the command below:

```shell-session
/ $ cd /docker/metricshub
/docker/metricshub $ sudo docker run --name=metricshub -v /docker/metricshub/lib/config:/opt/metricshub/lib/config metricshub:latest
```

**Docker Compose Example**

You can start **${solutionName}** with docker compose:

```shell-session
/docker/metricshub $ sudo docker compose up -d --build
```

Example (`docker-compose.yaml`):

```yaml
version: "2.1"
services:
  metricshub:
    # for image we will use ``image: sentrysoftware/metricshub:latest``
    build: .
    container_name: metricshub
    volumes:
      # Mount the volume ./lib/logs into /opt/metricshub/lib/logs in the container
      - ./lib/logs:/opt/metricshub/lib/logs
      # Mount the volume ./lib/config into /opt/metricshub/lib/config in the container
      - ./lib/config:/opt/metricshub/lib/config
    restart: unless-stopped
```

### Stop

To stop **${solutionName}** started as docker container, run the following command:

```shell-session
/docker/metricshub $ sudo docker stop metricshub
```

**Docker Compose**:

If you are using docker compose, from the `./metricshub` directory, run the following command to stop **${solutionName}**:

```shell-session
/docker/metricshub $ sudo docker compose down
```

### Uninstall

If you want to forcefully stop and remove the **${solutionName}** container, run the following commands:

```shell-session
/docker/metricshub $ sudo docker stop -f metricshub
/docker/metricshub $ sudo docker rm -f metricshub
```

Adjust the commands based on your specific requirements for stopping and removing the Docker container running **${solutionName}**.