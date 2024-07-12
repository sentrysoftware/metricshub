keywords: install, upgrade, firewalls
description: How to install MetricsHub on Linux, Windows, and Docker.

# Installation

<!-- MACRO{toc|fromDepth=1|toDepth=1|id=toc} -->

You can install **MetricsHub** on the operating system of your choice as they are equally supported.

## On Linux

### Download

Download the Linux package, `metricshub-linux-${communityVersion}.tar.gz`, from the [MetricsHub Release v${communityVersion}](https://github.com/sentrysoftware/metricshub/releases/tag/v${communityVersion}) page.

### Install

Unzip and untar the content of `metricshub-linux-${communityVersion}.tar.gz` into a program directory, like `/opt`. There is no need to create a specific subdirectory for `metricshub` as the archive already contains a `metricshub` directory.

```shell-session
/ $ cd /opt
/opt $ sudo tar xzf /tmp/metricshub-linux-${communityVersion}.tar.gz
```

### Configure

In the `./lib/config/metricshub.yaml` file, located under the `./metricshub` installation directory, configure:

* the [resources to be monitored](./configuration/configure-agent.html#configure-monitored-resources)
* the [OpenTelemetry Protocol endpoint](configuration/configure-agent.md#otlp-endpoint) that will receive the MetricsHub signals.

To assist with the setup process, the configuration example `./lib/config/metricshub-example.yaml` is provided for guidance in the installation directory (`./metricshub`).

### Start

To start **MetricsHub** in an interactive terminal with the default configuration file `./lib/config/metricshub.yaml`, run the command below:

```shell-session
/ $ cd /opt/metricshub/bin
/opt/metricshub/bin $ ./service
```

To start **MetricsHub** with an alternate configuration file, run the command below:

```shell-session
/ $ cd /opt/metricshub/bin
/opt/metricshub/bin $ ./service --config <PATH>
```

Example:

```shell-session
/ $ cd /opt/metricshub/bin
/opt/metricshub/bin $ ./service --config config/my-metricshub.yaml
```

To start **MetricsHub** as a **Linux service**, follow the steps below:

* **Create a systemd service file**

  Create a file (for example: `/etc/systemd/system/metricshub-service.service`) and define the **MetricsHub Service** configuration as follows:

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

  After creating the Linux service file, reload `systemd` to recognize the new service.

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

To manually stop the **MetricsHub Service** in an interactive terminal, use the keyboard shortcut `CTRL+C`. This will interrupt the running process and terminate the **MetricsHub Service**.

**Background Process**

If the **MetricsHub Service** is running in the background, follow these steps to stop it:

1. Run the `ps` command to get the **MetricsHub Service** PID:

   ```shell-session
   ps aux | grep service
   ```

2. Write down the PID associated with the **MetricsHub Service**.
3. Terminate the process using the `kill` command below:

   ```shell-session
   kill -9 <PID>
   ```
where `<PID>` should be replaced with the actual process ID.

**Service**

To stop the **MetricsHub Service** that is started as a **Linux service**, run the command below:

```shell-session
systemctl stop <metricshub-service>
```

where `<metricshub-service>` should be replaced with the actual service name. For example, `metricshub-service` if the `systemd` service file is `/etc/systemd/system/metricshub-service.service`

### Uninstall

1. Stop the **MetricsHub Service**.
2. Navigate to the directory where **MetricsHub** is located (e.g., `/opt`) and remove the entire `metricshub` directory.

  ```shell-session
  / $ cd /opt
  /opt $ rm -rf metricshub
  ```

If the **MetricsHub Service** was set up as a **Linux Service**, delete the file `/etc/systemd/system/metricshub-service.service` and run the below command to reload `systemd`:

  ```shell-session
  systemctl daemon-reload
  ```

## On Windows

### Download

Download the Windows package, `metricshub-windows-${communityVersion}.zip`, from the [MetricsHub Release v${communityVersion}](https://github.com/sentrysoftware/metricshub/releases/tag/v${communityVersion}) page.

### Install

Unzip the content of `metricshub-windows-${communityVersion}.zip` into a program folder, like `C:\Program Files`. There is no need to create a specific subdirectory for `MetricsHub` as the zip archive already contains a `MetricsHub` directory.

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

## On Docker

### Download

Download the Docker package, `metricshub-linux-${communityVersion}-docker.tar.gz`, from the [MetricsHub Release v${communityVersion}](https://github.com/sentrysoftware/metricshub/releases/tag/v${communityVersion}) page.

### Install

Unzip and untar the content of `metricshub-linux-${communityVersion}-docker.tar.gz` into a directory, like `/docker`.

```shell-session
/ $ cd /docker
/docker $ sudo tar xzf /tmp/metricshub-linux-${communityVersion}-docker.tar.gz
```

### Configure

In the `./lib/config/metricshub.yaml` file, locally under the `./metricshub` installation directory, configure:

* the [resources to be monitored](./configuration/configure-agent.html#configure-monitored-resources)
* the [OpenTelemetry Protocol endpoint](configuration/configure-agent.md#otlp-endpoint) that will receive the MetricsHub signals.

To assist with the setup process, the configuration example `./lib/config/metricshub-example.yaml` is provided for guidance in the installation directory (`./metricshub`).

### Build the docker image

Run the following command to build the docker image:

```shell-session
/ $ cd /docker/metricshub
/docker/metricshub $ sudo docker build -t metricshub:latest .
```

### Start

Run the following command to start **MetricsHub** with the default configuration file, `./lib/config/metricshub.yaml`:

```shell-session
/ $ cd /docker/metricshub
/docker/metricshub $ sudo docker run --name=metricshub metricshub:latest
```

You can start **MetricsHub** with an alternate configuration file with the following command:

```shell-session
/ $ cd /docker/metricshub
/docker/metricshub $ sudo docker run --name=metricshub -v /docker/metricshub/lib/config:/opt/metricshub/lib/config metricshub:latest
```

**Docker Compose Example**

You can start **MetricsHub** with docker compose:

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

To stop **MetricsHub** started as a docker container, run the following command:

```shell-session
/docker/metricshub $ sudo docker stop metricshub
```

**Docker Compose**:

If you are using docker compose from the `./metricshub` directory, run the following command to stop **MetricsHub**:

```shell-session
/docker/metricshub $ sudo docker compose down
```

### Uninstall

To force-stop and remove the **MetricsHub** container, run the following commands:

```shell-session
/docker/metricshub $ sudo docker stop -f metricshub
/docker/metricshub $ sudo docker rm -f metricshub
```

Adjust the commands to meet your specific requirements for stopping and removing the Docker container running **MetricsHub**.
