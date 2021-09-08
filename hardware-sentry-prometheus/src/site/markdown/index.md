# Getting Started

**${project.name}** is designed to collect health and performance metrics from any hardware devices. It allies the monitoring performance of more that 250 hardware data connectors with the monitoring expertise of Hardware Sentry.

![**${project.name}** Architecture](./images/mat_prom_architecture_diagram.png)

Prometheus needs endpoints or targets to scrape. Each individual target is an instance that provides metric data in a format Prometheus understands. All the targets dump their respective data in the format Prometheus can read from the /metrics location based on the scrape interval setting. A collection of instances that serve the same purpose is called a job.

**${project.name}** exposes hardware metrics in a Prometheus format. A Prometheus server then collects those metrics via HTTP requests and saves them with timestamps in a database.

## Prerequisites

### Install the Java package

**${project.name}** requires Java Runtime Environment (JRE) version 11 or higher to be installed on the server where the ```hardware-sentry-prometheus-<version>.jar``` is running.

To verify the version installed on your system, run the following command:

```batch
java -version
```
You can download the latest versions of the Java Runtime Environment from <a href="https://adoptium.net/" target="_blank">Adoptium (formerly AdoptOpenJDK)</a>.

### Install the ${project.name} package

Download the ```hardware-sentry-prometheus-<version>.jar``` from the Sentry Software’s Website.