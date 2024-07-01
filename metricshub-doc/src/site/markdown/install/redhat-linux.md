keywords: install, enterprise, community
description: How to install MetricsHub on RedHat Enterprise Linux.

# Installation

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

## Enterprise Edition

### Download

From [MetricsHub's Web site](https://metricshub.com), download **metricshub-enterprise-rhel-1.0.00.x86_64.rpm**.

### Install

Once you have downloaded the RPM package, run the following `rpm` command:

```shell-session
/ $ cd /usr/local
/usr/local $ sudo rpm -i metricshub-enterprise-rhel-1.0.00.x86_64.rpm
```
When complete, the **MetricsHub**'s files are deployed in `/opt/metricshub` and the **MetricsHubEnterprise Agent** is started as a service.

### Configure

In the `./lib/config/metricshub.yaml` file, located under the `./metricshub` installation directory, configure:

* the [resources to be monitored](./configuration/configure-agent.html#configure-monitored-resources)
* the [OpenTelemetry Protocol endpoint](configuration/configure-agent.md#otlp-endpoint) that will receive the MetricsHub signals.

To assist with the setup process, the configuration example `./lib/config/metricshub.yaml` is provided for guidance in the installation directory (`./metricshub`).

### Start

To start the **MetricsHub Enterprise** service, run the command below:

```shell-session
systemctl start metricshub-enterprise-service
```
This will start **MetricsHub** with the default **MetricsHub Enterprise Agent** configuration file, **./config/metricshub.yaml**.

<p id="redhat"> You can start <strong>MetricsHub</strong> in an interactive terminal with an alternate <strong>MetricsHub Agent</strong>'s configuration file with the command below:</p>

```shell-session
/ $ cd /opt/metricshub/bin
/opt/metricshub/bin $ ./agent --config=<PATH>
```
Example:

```shell-session
/ $ cd /opt/metricshub/bin
/opt/hws/bin $ ./agent --config=config/my-metricshub-config.yaml
```

### Stop

To stop the **MetricsHub Enterprise** service, run the command below:

```shell-session
systemctl stop metricshub-enterprise-service
```

### Uninstall

To uninstall **MetricsHub Enterprise**, run the command below:

```shell-session
sudo rpm -e metricshub-enterprise-rhel-1.0.00.x86_64
```

## Community Edition

### Download

Download the Linux package, `metricshub-linux-${project.version}.tar.gz`, from the [MetricsHub Release v${project.version}](https://github.com/sentrysoftware/metricshub/releases/tag/v${project.version}) page.

### Install

Unzip and untar the content of `metricshub-linux-${project.version}.tar.gz` into a program directory, like `/opt`. There is no need to create a specific subdirectory for `metricshub` as the archive already contains a `metricshub` directory.

```shell-session
/ $ cd /opt
/opt $ sudo tar xzf /tmp/metricshub-linux-${project.version}.tar.gz
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