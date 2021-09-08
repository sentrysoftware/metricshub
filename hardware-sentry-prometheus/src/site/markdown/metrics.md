# Viewing Collected Metrics

Make sure to you have:

* correctly configured the:

    * [**${project.name}**](./configure.html#Hardware_Sentry_Exporter_for_Prometheus), and
    * [Prometheus Server](./configure-prometheus-server.html)

* launched **${project.name}** with the ```java -jar hardware-sentry-prometheus-<version>.jar``` command line.

Prometheus scrapes all targets listed in the configuration file and returns the collected metrics in the standard Prometheus via an http endpoint (ex:```http://<host>:8080/metrics``` or ```http://nb-docker:8080/metrics/<target>```).

```
Example:

# HELP enclosure_status Metric: Enclosure status - Unit: {0 = OK ; 1 = Degraded ; 2 = Failed}
# TYPE enclosure_status gauge
enclosure_status{id="MS_HW_DellOpenManage.connector_enclosure_ecs1-01_1",parent="ecs1-01",label="Computer: Dell PowerEdge R630",fqdn="ecs1-01.internal.sentrysoftware.net",} 0.0
# HELP enclosure_info Metric: enclosure info
# TYPE enclosure_info gauge
enclosure_info{id="MS_HW_DellOpenManage.connector_enclosure_ecs1-01_1",parent="ecs1-01",label="Computer: Dell PowerEdge R630",fqdn="ecs1-01.internal.sentrysoftware.net",deviceId="1",serialNumber="FSJR3N2",vendor="Dell",model="PowerEdge R630",biosVersion="",type="Computer",additionalInformation1="Serial Number: FSJR3N2",additionalInformation2="Alternative Serial Number: 34377965102",additionalInformation3="",} 1.0
```
