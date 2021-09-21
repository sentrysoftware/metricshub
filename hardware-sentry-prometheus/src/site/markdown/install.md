keywords: installation package
description: Where to download the installation package of the Hardware Sentry Exporter for Prometheus.

# Installing ${project.name}

## Download

First, download **${project.artifactId}-${project.version}.zip** or **${project.artifactId}-${project.version}.tar.gz** from [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/products-for-prometheus.html#hardware-sentry-exporter-0.9). The package contains:

* the user documentation
* the license agreement
* the `${project.artifactId}-${project.version}.jar` file
* the default **${project.description}** configuration file  (`hardware-sentry-config.yml`).

## On Windows

Unzip the content of **${project.artifactId}-${project.version}.zip** into a program folder. There is no need to create a specific subfolder for `hws-exporter` as the zip archive already contains an **hws-exporter** folder.

> Note: You will need administrative privileges to unzip into **C:\Program Files**.

## On Linux (and UNIX)

Untar the content of **${project.artifactId}-${project.version}.tar.gz** into a program folder, like **/usr/local** or **/opt**.

> Note: There is no need to create a specific subfolder for `hws-exporter` as the zip archive already contains an **hws-exporter** folder. However, you may need to ensure everyone's right to access the **hws-exporter** directory with the `chmod 755 hws-exporter` command.

```bash
/:> cd /usr/local
/usr/local:> sudo tar xvf /tmp/${project.artifactId}-${project.version}.tar.gz
/usr/local:> sudo chmod 755 hws-exporter
```