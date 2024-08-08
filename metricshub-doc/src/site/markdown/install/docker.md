keywords: install, upgrade, firewalls
description: How to install MetricsHub on Linux, Windows, and Docker.

# Installation

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

## Enterprise Edition 

### Download

From [MetricsHub's Web site](https://metricshub.com), download **metricshub-enterprise-debian-${enterpriseVersion}-docker.tar.gz** and copy into `/tmp`.

### Install

First, unzip and untar the content of **metricshub-enterprise-debian-${enterpriseVersion}-docker.tar.gz** into a docker directory, like **/docker**.

```shell-session
sudo mkdir -p /docker
sudo tar xzf /tmp/metricshub-enterprise-debian-${enterpriseVersion}-docker.tar.gz -C /docker
```

Then, build the docker image using the following command:

```shell-session
cd /docker/metricshub
sudo docker build -t metricshub:latest .
```
### Configure

*  In the **./lib/config/metricshub.yaml** file, located under the `/docker/metricshub` installation directory, configure the [resources to be monitored](../configuration/configure-monitoring.html#configure-resources).
* In the **./lib/otel/otel-config.yaml** file, located under the `/docker/metricshub` installation directory, specify where the _OpenTelemetry Collector_ should [send the collected data](../configuration/send-data.html#configure-the-otel-collector-28enterprise-edition-29).


To assist with the setup process, two configuration examples are provided for guidance in the installation directory (`./metricshub`):

* `./lib/config/metricshub-config-example.yaml`, a configuration example of the MetricsHub agent.
* `./lib/otel/otel-config-example.yaml`, a configuration example of the OpenTelemetry Collector.

### Start

You can start **MetricsHub** with the command below:

```shell-session
cd /docker/metricshub
sudo docker run -d --name=metricshub -p 24375:24375 -p 13133:13133 -v /docker/metricshub/lib/config:/opt/metricshub/lib/config -v /docker/metricshub/lib/otel:/opt/metricshub/lib/otel -v /docker/metricshub/lib/logs:/opt/metricshub/lib/logs -v /docker/metricshub/lib/security:/opt/metricshub/lib/security metricshub:latest
```

This will start **MetricsHub** with the default **MetricsHub Enterprise Agent** configuration file, **./lib/config/metricshub.yaml**.

**Docker Compose Example**

You can start **MetricsHub** with docker-compose:

```shell-session
sudo docker-compose up -d --build
```

Example docker-compose.yaml

```yaml
version: "2.1"
services:
  metricshub:
    build: .                                        # for image we will use ``image: sentrysoftware/metricshub:latest``
    container_name: metricshub
    ports:
      - 13133:13133                                   # OpenTelemetry Collector HealthCheck
      - 24375:24375                                   # OpenTelemetry Collector Prometheus Exporter
    volumes:
      - ./lib/logs:/opt/metricshub/lib/logs                # Mount the volume ./lib/logs into /opt/metricshub/lib/logs in the container
      - ./lib/config:/opt/metricshub/lib/config            # Mount the volume ./lib/config into /opt/metricshub/lib/config in the container
      - ./lib/otel:/opt/metricshub/lib/otel                # Mount the volume ./lib/otel into /opt/metricshub/lib/otel in the container
      - ./lib/security:/opt/metricshub/lib/security        # Mount the volume ./lib/security into /opt/metricshub/lib/security in the container
    restart: unless-stopped
```

## Community Edition

### Download

Download the Docker package, `metricshub-linux-${communityVersion}-docker.tar.gz`, from the [MetricsHub Release v${communityVersion}](https://github.com/sentrysoftware/metricshub/releases/tag/v${communityVersion}) page using the following command:

```shell-session
wget -P /tmp https://github.com/sentrysoftware/metricshub/releases/download/v${communityVersion}/metricshub-linux-${communityVersion}-docker.tar.gz
```

### Install

Unzip and untar the content of `metricshub-linux-${communityVersion}-docker.tar.gz` into a directory, like `/docker`.

```shell-session
sudo mkdir -p /docker
sudo tar xzf /tmp/metricshub-linux-${communityVersion}-docker.tar.gz -C /docker
```

### Configure

In the `./lib/config/metricshub.yaml` file, locally under the `./metricshub` installation directory, configure:

* the [resources to be monitored.](../configuration/configure-monitoring.md#configure-resources)
* the [OpenTelemetry Protocol endpoint](../configuration/send-data.html#configure-the-otlp-receiver-28community-edition-29) that will receive the MetricsHub signals.

To assist with the setup process, the configuration example `./lib/config/metricshub-example.yaml` is provided for guidance in the installation directory (`./metricshub`).

### Build the docker image

Run the following command to build the docker image:

```shell-session
cd /docker/metricshub
sudo docker build -t metricshub:latest .
```

### Start

Run the following command to start **MetricsHub** with the default configuration file, `./lib/config/metricshub.yaml`:

```shell-session
cd /docker/metricshub
sudo docker run -d --name=metricshub metricshub:latest
```

You can start **MetricsHub** with an alternate configuration file with the following command:

```shell-session
cd /docker/metricshub
sudo docker run -d --name=metricshub -v /docker/metricshub/lib/config:/opt/metricshub/lib/config -v /docker/metricshub/lib/logs:/opt/metricshub/lib/logs metricshub:latest
```

**Docker Compose Example**

You can start **MetricsHub** with docker compose:

```shell-session
sudo docker compose up -d --build
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
sudo docker stop metricshub
```

**Docker Compose**:

If you are using docker compose from the `./metricshub` directory, run the following command to stop **MetricsHub**:

```shell-session
sudo docker compose down
```

### Uninstall

To force-stop and remove the **MetricsHub** container, run the following commands:

```shell-session
cd /docker/metricshub
sudo docker stop -f metricshub
sudo docker rm -f metricshub
```

Adjust the commands to meet your specific requirements for stopping and removing the Docker container running **MetricsHub**.
