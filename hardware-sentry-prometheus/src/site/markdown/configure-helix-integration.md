keywords: configuration, prometheus server, examples, helix, bmc
description: How to integrate the hardware metrics collected by **${project.name}** into BMC Helix Operations Management

# Integrating With BMC Helix Operations Management

**${project.name}** integrates with BMC Helix Operations Management to expose hardware health and performance metrics into Helix dashboards.



## Configure a VictoriaMetrics vmagent

**${project.name}** collects metrics from monitored targets and exposes the collected information. The VictoriaMetrics vmagent can be configured to scrape this data and remotely write it to BMC Helix Operations Management.

## Install and Configure the vmagent

Go to the VictoriaMetrics vmagent [release page](https://github.com/VictoriaMetrics/VictoriaMetrics/releases) and download the `vmutils` package appropriate to your system.

Unpack the archive. Create a `hardware-sentry.yml` file in the folder where the `vmagent` was unpacked.

The `hardware-sentry.yml` file should have the following content:

```
global:
  scrape_interval: 2m

scrape_configs:
  - job_name: 'hardware-sentry'
    static_configs:
      - targets: ['<hostname:port_number>']
```

Where

* `hostname` is the name of the server where **${project.name}** is running.

* `port_number` is the port number of **${project.name}**.

The `scrape_interval` is the `vmagent` scrape interval and be customized. It should be equal to or higher than the exporter's collect interval. Example: 1d, 1h30m, 5m.

Example:

```
global:

  scrape_interval: 2m

scrape_configs:
  - job_name: 'hardware-sentry'
    static_configs:
      - targets: ['localhost:8080']

```

### Getting the BMC Helix API Key

An API key is required to push the data to BMC Helix Operations Management.

Connect to your BMC Helix Operations Management Portal, go to the repository page, and click on the **Copy API Key** button.

![Copy API Key](images/copy_api_key.png)

Save the API key and take note of your BMC Helix Portal URL.

### Running the vmagent

Execute the following commande-line, using the appropriate executable name for your system:

```
vmagent -promscrape.config hardware-sentry.yml -remoteWrite.url https://<BMC Helix URL>/metrics-gateway-service/api/v1.0/prometheus -remoteWrite.bearerToken=<API Key>
```

Replace `<BMC Helix URL>` with your BMC Helix Portal URL, and `<API Key>` with the API key copied from the BMC Helix Operations Management Portal.

If the command is successfull, you should see an output similar to this:

```
info    VictoriaMetrics/app/vmagent/remotewrite/client.go:145   initialized client for -remoteWrite.url="1:secret-url"
info    VictoriaMetrics/app/vmagent/main.go:113 started vmagent in 0.006 seconds
info    VictoriaMetrics/lib/promscrape/scraper.go:86    reading Prometheus configs from "hardware-sentry.yml"
info    VictoriaMetrics/lib/httpserver/httpserver.go:82 starting http server at http://:8429/
info    VictoriaMetrics/lib/httpserver/httpserver.go:83 pprof handlers are exposed at http://:8429/debug/pprof/
info    VictoriaMetrics/lib/promscrape/config.go:68     starting service discovery routines...
info    VictoriaMetrics/lib/promscrape/config.go:74     started service discovery routines in 0.001 seconds
info    VictoriaMetrics/lib/promscrape/scraper.go:367   static_configs: added targets: 1, removed targets: 0; total targets: 1
```

## Import **${project.name}** Dashboards into your Helix Environment

### Load Dashboards in Helix

* the dashboards (.json files)
* the provisioning files (.yml files)

#### On Windows

Uncompress the *provisioning* folder in the **hardware-dashboards-for-helix.zip** file to the "*grafana\conf*" folder on the Grafana server (ex: "C:\Program Files\GrafanaLabs\grafana\conf").

Uncompress the *sustainable_IT_by_sentry_software* folder to any directory on the Grafana server (ex: "C:\Program Files\GrafanaLabs\grafana\public\dashboards").

#### On Linux and UNIX

Uncompress the *provisioning* folder in the **hardware-dashboards-for-helix.tar.gz** file to the *grafana* folder on the Grafana server (ex: "/etc/grafana").

Uncompress the *sustainable_IT_by_sentry_software* folder to any directory on the Grafana server (ex: "/var/lib/grafana/dashboards").

### Configure the Dashboard Provider

In the "**provisioning/dashboards**" directory on the Grafana server, open the file called *hardware-sentry.yml*. Find the line containing ```path: ''```, specify the path where your uncompressed the dashboards (ex: path: 'C:/Program Files/GrafanaLabs/grafana/public/dashboards"') and save the changes.

Note: the path should point to the folder directly above the *sustainable_IT_by_sentry_software* folder. This folder should only contain dashboards for Grafana.

### Configure the Data Source

The dashboards for Helix query the Prometheus server to display the status of the hardware components. A Prometheus data source needs to be configured on the Grafana server.

In the "**provisioning/datasource**" directory, open the *hardware-sentry-prometheus.yml*. Enter the required settings to connect to your Prometheus server and save the changes. This will create a new data source called **hardware_sentry_prometheus** in Grafana.

Restart. The dashboard are now loaded in Helix Operations Management.

![**${project.name}** Sustainable IT Dashboard](./images/dashboard_all_zones.png)
hardware-dashboards-for-helix.zip

## Add Targets

Refer to the [Configuration](./configure.html) section for details.
