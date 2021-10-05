keywords: configuration, prometheus server, examples, helix, bmc

description: How to integrate the hardware metrics collected by **${project.name}** into BMC Helix Operations Management

# Configure a VictoriaMetrix vmagent

**${project.name}** collects metrics from monitored targets and exposes the collected information. The VictoriaMetrix vmagent can be configured to scrape this data and remotely write it to BMC Helix Operations Management.

## Install and Configure the vmagent

Go to the the VictoriaMetrix vmagent [release page](https://github.com/VictoriaMetrics/VictoriaMetrics/releases) and download the `vmutils` package appropriate to your system.

Unpack the archive and create a `hardware-sentry.yml` file in the folder where the `vmagent` was unpacked.

The `hardware-sentry.yml` file should have the following content:

```
global:

scrape_configs:
  - job_name: 'hardware-sentry'
    static_configs:
      - targets: ['<url to the **${project.name}**>']
```

Example
```
global:

scrape_configs:
  - job_name: 'hardware-sentry'
    static_configs:
      - targets: ['http://localhost:8080']
```

## Getting the BMC Helix APY Key
An APY key is required to push the data to BMC Helix Operations Management.

Connect to your BMC Helix Operations Management Portal, go to the repository page and click on the **Copy APY Key** button.

![Copy APY Key](images/copy_apy_key.png)

Save the API key and take note of your BMC Helix Portal URL.

## Running the vmagent

Execute the following commande-line, using the appropriate executable name for your system:

```
vmagent -promscrape.config hardware-sentry.yml -remoteWrite.url https://<BMC Helix URL>/metrics-gateway-service/api/v1.0/prometheus -remoteWrite.bearerToken=<APY Key>
```

Replace `<BMC Helix URL>` with your BMC Helix Portal URL, and `<APY Key>` with the APY key copied from the BMC Helix Operations Management Portal.