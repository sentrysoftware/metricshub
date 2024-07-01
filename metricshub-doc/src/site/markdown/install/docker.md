keywords: install, upgrade, firewalls
description: How to install MetricsHub on Linux, Windows, and Docker.

# Installation

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

## Enterprise Edition 

### Download

From [MetricsHub's Web site](https://metricshub.com), download **metricshub-enterprise-debian-1.0.00-docker.tar.gz**

### Install

First, unzip and untar the content of **metricshub-enterprise-debian-1.0.00-docker.tar.gz** into a docker directory, like **/docker**.

```shell-session
/ $ cd /docker
/docker $ sudo tar xf /tmp/metricshub-enterprise-debian-1.0.00-docker.tar.gz
```

Then, build the docker image using the following command:

```shell-session
/ $ cd /docker/hws
/docker/hws $ sudo docker build -t metricshub:latest 
```
### Configure

In the `./lib/config/metricshub.yaml` file, located under the `./metricshub` installation directory, configure:

* the [resources to be monitored](./configuration/configure-agent.html#configure-monitored-resources)
* the [OpenTelemetry Protocol endpoint](configuration/configure-agent.md#otlp-endpoint) that will receive the MetricsHub signals.

To assist with the setup process, the configuration example `./lib/config/metricshub-example.yaml` is provided for guidance in the installation directory (`./metricshub`).

### Start

You can start **MetricsHub** with the command below:

```shell-session
/ $ cd /docker/metricshub
/docker/metricshub $ sudo docker run --name=metricshub -p 8888:8888 -p 4317:4317 -p 13133:13133 -v /docker/metricshub/lib/config:/opt/metricshub/lib/config -v /docker/metricshub/lib/otel:/opt/metricshub/lib/otel hws:latest
```

This will start **MetricsHub** with the default **MetricsHub Enterprise Agent** configuration file, **./lib/config/etricshub.yaml**.

**Docker Compose Example**

You can start **MetricsHub** with docker-compose:

```shell-session
/docker/metricshub $ sudo docker-compose up -d --build
```

Example docker-compose.yaml

```yaml
version: "2.1"
services:
  hws:
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

## Community Edition

### Download

Download the Docker package, `metricshub-linux-${project.version}-docker.tar.gz`, from the [MetricsHub Release v${project.version}](https://github.com/sentrysoftware/metricshub/releases/tag/v${project.version}) page.

### Install

Unzip and untar the content of `metricshub-linux-${project.version}-docker.tar.gz` into a directory, like `/docker`.

```shell-session
/ $ cd /docker
/docker $ sudo tar xzf /tmp/metricshub-linux-${project.version}-docker.tar.gz
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
