keywords: self-monitoring, performance, job duration, troubleshooting
description: How to track MetricsHub's own performance.

## MetricsHub self-monitoring

 



### Enabling self-monitoring

Refer to [Monitoring Configuration](../configuration/configure-monitoring.md#self-monitoring) page to know how to enable the self-monitoring feature.

### 

Monitoring MetricsHub’s own performance ensures that your observability stack runs efficiently, enabling proactive troubleshooting and optimization. Use the self-monitoring feature described in the [Monitoring Configuration](../configuration/configure-monitoring.md#self-monitoring) page to access detailed metrics. 

When self-monitoring is enabled, the `metricshub.job.duration` metric provides insights into task execution times. Key tags include:

* **`job.type`**: Operation performed by **MetricsHub**. Possible values are:
  * `discovery`: Identifies and registers components.
  * `collect`: Gathers telemetry data from monitored components.
  * `simple`: Executes a straightforward task.
  * `beforeAll` or `afterAll`: Performs preparatory or cleanup operations.
* **`monitor.type`**: Component being monitored. Examples:
    * Hardware metrics: `cpu`, `memory`, `physical_disk`, or `disk_controller`.
    * Environmental metrics: `temperature` or `battery`.
    * Logical entities: `connector`.
- **`connector_id`**: Unique identifier for the connector, such as HPEGen10IloREST for the HPE Gen10 iLO REST connector.

Example:

```
metricshub.job.duration{job.type="discovery", monitor.type="enclosure", connector_id="HPEGen10IloREST"} 0.020
metricshub.job.duration{job.type="discovery", monitor.type="cpu", connector_id="HPEGen10IloREST"} 0.030
metricshub.job.duration{job.type="discovery", monitor.type="temperature", connector_id="HPEGen10IloREST"} 0.025
metricshub.job.duration{job.type="discovery", monitor.type="connector", connector_id="HPEGen10IloREST"} 0.015
metricshub.job.duration{job.type="collect", monitor.type="cpu", connector_id="HPEGen10IloREST"} 0.015
```
