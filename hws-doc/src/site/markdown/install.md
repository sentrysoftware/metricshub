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

You need to set up 2 configuration files in the installation directory (`/opt/hws`):

* [**./otel/otel-config.yaml**](configuration/configure-otel.md): to specify where the _OpenTelemetry Collector_ should send the collected data
* [**./config/hws-config.yaml**](configuration/configure-agent.md): to specify the hosts to monitor and their credentials

There are two configuration examples in the installation directory (`/opt/hws`):

* **./otel/otel-config-example.yaml**, a configuration example of the _OpenTelemetry Collector_.
* **./config/hws-config-example.yaml**, a configuration example of the **Hardware Sentry Agent**.

Before starting **${solutionName}**, make sure to configure [**./otel/otel-config.yaml**](configuration/configure-otel.md), since a restart of the _Collector_ is required to take into account its changes.

#### Start

To start the **${solutionName}** service, run the command below:

```shell-session
systemctl start hws-agent
```

This will start **${solutionName}** with:

* The default **Hardware Sentry Agent** configuration file: **./config/hws-config.yaml**.
* The default _OpenTelemetry Collector_ configuration file: **./otel/otel-config.yaml**.

<p id="debian">You can start <strong>${solutionName}</strong> in an interactive terminal with an alternate <strong>Hardware Sentry Agent</strong>'s configuration file with the command below:</p>

```shell-session
/ $ cd /opt/hws/bin
/opt/hws/bin $ ./agent --config=<PATH>
```

Example:

```shell-session
/ $ cd /opt/hws/bin
/opt/hws/bin $ ./agent --config=config/my-hws-config.yaml
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

You need to set up 2 configuration files in the docker image directory (`hws`):

* [**./lib/otel/otel-config.yaml**](configuration/configure-otel.md): to specify where the _OpenTelemetry Collector_ should send the collected data
* [**./lib/config/hws-config.yaml**](configuration/configure-agent.md): to specify the hosts to monitor and their credentials

There are two configuration examples in the docker image directory (`hws`):

* **./otel/otel-config-example.yaml**, a configuration example of the _OpenTelemetry Collector_.
* **./config/hws-config-example.yaml**, a configuration example of the **Hardware Sentry Agent**.

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

<p id="docker">You can start <strong>${solutionName}</strong> with an alternate configuration file path with the command below:</p>

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

You need to set up 2 configuration files in the installation directory (`/opt/hws`):

* [**./otel/otel-config.yaml**](configuration/configure-otel.md): to specify where the _OpenTelemetry Collector_ should send the collected data
* [**./config/hws-config.yaml**](configuration/configure-agent.md): to specify the hosts to monitor and their credentials

There are two configuration examples in the installation directory (`/opt/hws`):

* **./otel/otel-config-example.yaml**, a configuration example of the _OpenTelemetry Collector_.
* **./config/hws-config-example.yaml**, a configuration example of the **Hardware Sentry Agent**.

Before starting **${solutionName}**, make sure to configure [**./otel/otel-config.yaml**](configuration/configure-otel.md), since a restart of the _Collector_ is required to take into account its changes.

#### Start

To start the **${solutionName}** service, run the command below:

```shell-session
systemctl start hws-agent
```

This will start **${solutionName}** with:

* The default **Hardware Sentry Agent** configuration file: **./config/hws-config.yaml**.
* The default _OpenTelemetry Collector_ configuration file: **./otel/otel-config.yaml**.

<p id="redhat"> You can start <strong>${solutionName}</strong> in an interactive terminal with an alternate <strong>Hardware Sentry Agent</strong>'s configuration file with the command below:</p>

```shell-session
/ $ cd /opt/hws/bin
/opt/hws/bin $ ./agent --config=<PATH>
```

Example:

```shell-session
/ $ cd /opt/hws/bin
/opt/hws/bin $ ./agent --config=config/my-hws-config.yaml
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

**${solutionName}** operates using the configuration located in the **ProgramData\hws** directory (`C:\ProgramData\hws`).

#### Configure

You need to set up 2 configuration files in the **ProgramData\hws** directory (`C:\ProgramData\hws`):

* [**./otel/otel-config.yaml**](configuration/configure-otel.md): to specify where the _OpenTelemetry Collector_ should send the collected data.
* [**./config/hws-config.yaml**](configuration/configure-agent.md): to specify the hosts to monitor and their credentials.

There are two configuration examples in the installation directory (`C:\Program Files\hws`):

* **./otel/otel-config-example.yaml**, a configuration example of the _OpenTelemetry Collector_.
* **./config/hws-config-example.yaml**, a configuration example of the **Hardware Sentry Agent**.

Before starting **${solutionName}**, make sure to configure [**./otel/otel-config.yaml**](configuration/configure-otel.md), since a restart of the _Collector_ is required to take into account its changes.

#### Start

To start the **${solutionName}** service, open **services.msc** and start the **Hardware Sentry Agent** service.

This will start **${solutionName}** with:

* The default **Hardware Sentry Agent** configuration file: **./config/hws-config.yaml**.
* The default _OpenTelemetry Collector_ configuration file: **./otel/otel-config.yaml**.

<p id="windows">You can start <strong>${solutionName}</strong> in an interactive terminal (using <strong>CMD.EXE</strong> or 
<a href="https://www.microsoft.com/en-us/p/windows-terminal/9n0dx20hk701?activetab=pivot:overviewtab">Windows Terminal</a> with an alternate <strong>Hardware Sentry Agent</strong>'s configuration file using the command below:</p>

```batch
c:
cd "Program Files"
cd hws
agent --config="c:\ProgramData\hws\config\my-hws-config.yaml"
```

#### Uninstall

To uninstall **${solutionName}**, double-click the **hws-windows-${project.version}.msi** file and click **Remove** when prompted.

## Migrate

If you are migrating from v2.0.00 or older, perform the actions below before installing the latest version of **${solutionName}**:

1. Stop the service:
   * On Windows:
     * if you installed **${solutionName}** as a service, stop and remove the service
     * if you are running the collector in an interactive terminal, stop the `hws-otel-collector.exe` process
   * On Linux: stop `hws-otel-collector`
2. Make a backup copy of:
   * the configuration files `otel-config.yaml` and `hws-config.yaml` stored by default in **hws-otel-collector/config**
   * the security keystore `hws-encrypt.p12` stored by default in **hws-otel-collector/security** if you previously encrypted your passwords
3. Install the latest version of **${solutionName}**
4. Paste your backup copy of:
   * `otel-config.yaml` in **C:\ProgramData\hws\otel** for Windows, **/opt/hws/otel** for Linux
   * `hws-config.yaml` in **C:\ProgramData\hws\config** for Windows, **/opt/hws/config** for Linux
   * `hws-keystore.p12` in **C:\ProgramData\hws\security** for Windows, **/opt/hws/security** for Linux.
5. Open and edit `otel-config.yaml`:
   
     * In the `extensions` section, remove the `hws_agent` extension:

        ```yaml
          extensions:

            # healthcheck
            # https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/extension/healthcheckextension
            health_check:

            # zPages
            # https://github.com/open-telemetry/opentelemetry-collector/tree/main/extension/zpagesextension
            zpages:

            # Remove hwsagent extension
            # hws_agent: # Extension not supported starting from version 3.0.00
          ```
        
      * In the `service` section, remove `hws_agent` in the list of `extensions`:

         ```yaml
         service:
         telemetry:
           logs:
             level: info 
           metrics:
             address: localhost:8888
             level: basic
         extensions: [health_check, basicauth, zpages ] # Remove hws_agent from this list
         # ...
         ```

     * If you specified an alternate configuration file using the `extra_arg` `--config` in version 2.0.00, you need to start **${solutionName}** in an interactive terminal using the command documented above for <a href="#debian">Debian Linux</a>, <a href="#docker">Docker</a>, <a href="#redhat">Red Hat</a>, and <a href="#windows">Windows</a>. 

     * In the `receivers` section, update the security file paths `cert_file` and `key_file` in `otlp:protocols:grpc:tls` as follows:

         ```yaml
         receivers:
           # OTLP
           # Receives data via gRPC or HTTP using OTLP format. For additional information on the OTLP receiver:
           # https://github.com/open-telemetry/opentelemetry-collector/tree/main/receiver/otlpreceiver
           otlp:
             protocols:
               grpc:
                 endpoint: localhost:4317
                 tls:
                   cert_file: ../security/otel.crt # updated security file path
                   key_file: ../security/otel.key # updated security file path
                 auth:
                   authenticator: basicauth
         ```

      * In the `extensions` section, update the security file path in `basicauth:htpasswd:file` as follows:

         ```yaml
         extensions:

           # healthcheck
           # https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/extension/healthcheckextension
           health_check:

           # zPages
           # https://github.com/open-telemetry/opentelemetry-collector/tree/main/extension/zpagesextension
           zpages:

           # basicauth
           # https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/extension/basicauthextension
           basicauth:
             htpasswd:
               file: ../security/.htpasswd # updated security file path
          ```

      * In the `exporters` section, update the configuration of the `logging` exporter as follows:

         ```yaml  
         # logging
         # https://github.com/open-telemetry/opentelemetry-collector/tree/main/exporter/loggingexporter
         logging:
           verbosity: detailed
          ```
6. Restart **${solutionName}**.

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
