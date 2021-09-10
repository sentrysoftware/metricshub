keywords: prometheus exporter, hardware, metrics, output
description: How Hardware Sentry Exporter for Prometheus exposes hardware metrics in a Prometheus format.

# Viewing Collected Metrics

**${project.name}** scrapes all targets listed in the configuration file and returns the collected metrics in the standard Prometheus format via an HTTP endpoint.

Use```http://<host>:8080/metrics``` to view the metrics of all monitored targets, or
```http://nb-docker:8080/metrics/myhostname``` to view the metrics of a specific target.

```
Example:

# HELP hw_target_heating_margin_celsius Metric: hw_target_heating_margin_celsius - Unit: celsius
# TYPE hw_target_heating_margin_celsius gauge
hw_target_heating_margin_celsius{id="ilo-dev-vm-01",parent="",label="ilo-dev-vm-01",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 22.0
# HELP hw_target_info Metric: hw_target_info
# TYPE hw_target_info gauge
hw_target_info{id="ilo-dev-vm-01",parent="",label="ilo-dev-vm-01",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",location="remote",} 1.0
# HELP hw_connector_status Metric: hw_connector_status - Unit: {0 = OK ; 1 = Degraded ; 2 = Failed}
# TYPE hw_connector_status gauge
hw_connector_status{id="ilo-dev-vm-01@MS_HW_CiscoUCSBlade.connector",parent="ilo-dev-vm-01",label="MS_HW_CiscoUCSBlade.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 0.0
hw_connector_status{id="ilo-dev-vm-01@MS_HW_CpMgServNT.connector",parent="ilo-dev-vm-01",label="MS_HW_CpMgServNT.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 0.0
hw_connector_status{id="ilo-dev-vm-01@MS_HW_CpqDriveArrayNT.connector",parent="ilo-dev-vm-01",label="MS_HW_CpqDriveArrayNT.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 0.0
hw_connector_status{id="ilo-dev-vm-01@MS_HW_CpMgSm2.connector",parent="ilo-dev-vm-01",label="MS_HW_CpMgSm2.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 0.0
hw_connector_status{id="ilo-dev-vm-01@MS_HW_CpqHeResMem2.connector",parent="ilo-dev-vm-01",label="MS_HW_CpqHeResMem2.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 0.0
# HELP hw_connector_info Metric: hw_connector_info
# TYPE hw_connector_info gauge
hw_connector_info{id="ilo-dev-vm-01@MS_HW_CiscoUCSBlade.connector",parent="ilo-dev-vm-01",label="MS_HW_CiscoUCSBlade.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",displayName="Cisco UCS Manager (Fabric Interconnect Switch)",fileName="MS_HW_CiscoUCSBlade.connector",description="This connector provides hardware monitoring for Cisco UCS Blade chassis (as well as the Cisco Fabric Interconnect Switch) through the UCS Manager (running on the Fabric Interconnect Switch).",} 1.0
hw_connector_info{id="ilo-dev-vm-01@MS_HW_CpMgServNT.connector",parent="ilo-dev-vm-01",label="MS_HW_CpMgServNT.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",displayName="HP Insight Management Agent - Server",fileName="MS_HW_CpMgServNT.connector",description="This connector provides hardware monitoring through the HP Insight Manager (Server Agent) which supports almost all HP ProLiant and Integrity servers under Windows and Linux, as well as Tru64 servers.",} 1.0
hw_connector_info{id="ilo-dev-vm-01@MS_HW_CpqDriveArrayNT.connector",parent="ilo-dev-vm-01",label="MS_HW_CpqDriveArrayNT.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",displayName="HP Insight Management Agent - Drive Array",fileName="MS_HW_CpqDriveArrayNT.connector",description="This connector monitors the HP/Compaq Drive Arrays by connecting to the Storage Management SNMP sub-agent of the HP Insight Manager agent.",} 1.0
hw_connector_info{id="ilo-dev-vm-01@MS_HW_CpMgSm2.connector",parent="ilo-dev-vm-01",label="MS_HW_CpMgSm2.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",displayName="HP Insight Management Agent - iLO",fileName="MS_HW_CpMgSm2.connector",description="This connector provides hardware monitoring of the HP iLO card in HP ProLiant servers through the HP Insight Manager (Server Agent) which supports almost all HP ProLiant and Integrity servers under Windows and Linux, as well as Tru64 servers.",} 1.0
hw_connector_info{id="ilo-dev-vm-01@MS_HW_CpqHeResMem2.connector",parent="ilo-dev-vm-01",label="MS_HW_CpqHeResMem2.connector",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",displayName="HP Insight Management Agent (v8.25 or higher) - Memory",fileName="MS_HW_CpqHeResMem2.connector",description="This connector provides Memory Information through the HP Insight Manager (Newer Server Agents).",} 1.0
# HELP hw_enclosure_info Metric: hw_enclosure_info
# TYPE hw_enclosure_info gauge
hw_enclosure_info{id="MS_HW_CpMgServNT.connector_enclosure_ilo-dev-vm-01_0",parent="ilo-dev-vm-01",label="Computer: HP ProLiant DL360 Gen9",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",deviceId="0",serialNumber="CZJ6190871",vendor="HP",model="ProLiant DL360 Gen9",biosVersion="",type="Computer",additionalInformation1="Part Number: 755259-B21",additionalInformation2="Alternative Serial Number: ",additionalInformation3="",} 1.0
# HELP hw_cpu_current_speed_hertz Metric: hw_cpu_current_speed_hertz - Unit: hertz
# TYPE hw_cpu_current_speed_hertz gauge
hw_cpu_current_speed_hertz{id="MS_HW_CpMgServNT.connector_cpu_ilo-dev-vm-01_0",parent="MS_HW_CpMgServNT.connector_enclosure_ilo-dev-vm-01_0",label="0 (Intel - Xeon - 1.60 GHz)",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 1.6E9
hw_cpu_current_speed_hertz{id="MS_HW_CpMgServNT.connector_cpu_ilo-dev-vm-01_1",parent="MS_HW_CpMgServNT.connector_enclosure_ilo-dev-vm-01_0",label="1 (Intel - Xeon - 1.60 GHz)",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 1.6E9
# HELP hw_cpu_predicted_failure Metric: hw_cpu_predicted_failure - Unit: {0 = OK ; 1 = Failure Predicted}
# TYPE hw_cpu_predicted_failure gauge
hw_cpu_predicted_failure{id="MS_HW_CpMgServNT.connector_cpu_ilo-dev-vm-01_0",parent="MS_HW_CpMgServNT.connector_enclosure_ilo-dev-vm-01_0",label="0 (Intel - Xeon - 1.60 GHz)",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 1.0
hw_cpu_predicted_failure{id="MS_HW_CpMgServNT.connector_cpu_ilo-dev-vm-01_1",parent="MS_HW_CpMgServNT.connector_enclosure_ilo-dev-vm-01_0",label="1 (Intel - Xeon - 1.60 GHz)",fqdn="ilo-dev-vm-01.internal.sentrysoftware.net",} 1.0
# HELP hw_cpu_present Metric: hw_cpu_present - Unit: {0 = Missing ; 1 = Present}
# TYPE hw_cpu_present gauge
...
```
