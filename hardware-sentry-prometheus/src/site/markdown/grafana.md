# Loading the Dashboards in Grafana

## Installing the Dashboards
First, download the lastest version of **hardware-dashboards-for-grafana.zip** or **hardware-dashboards-for-grafana.tar.gz** from [Sentry Software's Web site](https://www.sentrysoftware.com/downloads/products-for-prometheus.html). The package contains:

* the dashboards (.json files)
* the provisioning files (.yml files)

### On Windows

Uncompress the *provisioning* folder in the **hardware-dashboards-for-grafana.zip** file to the "*grafana\conf*" folder on the Grafana server (ex: "C:\Program Files\GrafanaLabs\grafana\conf").

Uncompress the *sustainable_IT_by_sentry_software* folder to any directory on the Grafana server (ex:  "C:\Program Files\GrafanaLabs\grafana\public\dashboards").

### On Linux and UNIX

Uncompress the *provisioning* folder in the **hardware-dashboards-for-grafana.tar.gz** file to the *grafana* folder on the Grafana server (ex: "/etc/grafana").

Uncompress the *sustainable_IT_by_sentry_software* folder to any directory on the Grafana server (ex:  "/var/lib/grafana/dashboards").

## Configuring the Dashboard Provider

In the "**provisioning/dashboards**" directory on the Grafana server, open the file called *hardware-sentry.yml*. Find the line containing ```path: ''```, specify the path where your uncompressed the dashboards (ex: path: 'C:/Program Files/GrafanaLabs/grafana/public/dashboards"') and save the changes.

Note: the path should point to the folder directly above the *sustainable_IT_by_sentry_software* folder. This folder should only contain dashboards for Grafana.

## Configuring the Data Source

The dashboards for Grafana query the Prometheus server to display the status of the hardware components. A Prometheus data source needs to be configured on the Grafana server.

In the "**provisioning/datasource**" directory, open the *hardware-sentry-prometheus.yml*. Enter the required settings to connect to your Prometheus server and save the changes. This will create a new data source called **hardware_sentry_prometheus** in Grafana.

Restart the Grafana server. The dashboard are now loaded in Grafana.