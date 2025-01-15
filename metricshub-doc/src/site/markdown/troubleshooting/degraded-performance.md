keywords: self-monitoring, performance, job duration, troubleshooting
description: How to track MetricsHub's own performance.

## Degraded Performance

If you observe delays in data collection, missing data points, or timeouts, enable the self-monitoring feature as described in the [Monitoring Configuration](../configuration/configure-monitoring.md#self-monitoring) page. This feature provides detailed metrics about job execution times, helping you identify inefficiencies such as misconfigurations, bottlenecks, or performance issues in specific components.

When self-monitoring is enabled, the `metricshub.job.duration` metric provides insights into task execution times. Key attributes include:

* **`job.type`**: The operation performed by **MetricsHub**. Possible values are:
  * `discovery`: Identifies and registers components.
  * `collect`: Gathers telemetry data from monitored components.
  * `simple`: Executes a straightforward task.
  * `beforeAll` or `afterAll`: Performs preparatory or cleanup operations.
* **`monitor.type`**: The component being monitored, such as:
  * *Hardware metrics*: `cpu`, `memory`, `physical_disk`, or `disk_controller`.
  * *Environmental metrics*: `temperature` or `battery`.
  * *Logical entities*: `connector`.
* **`connector_id`**: The unique identifier for the connector, such as HPEGen10IloREST for the HPE Gen10 iLO REST connector.

These metrics can be viewed in Prometheus/Grafana or in the `metricshub-agent-$resourceId-$timestamp.log`  file.

### Example

Example of metrics emitted for the `HPEGen10IloREST` connector:

```bash
metricshub.job.duration{job.type="discovery", monitor.type="enclosure", connector_id="HPEGen10IloREST"} 0.020
metricshub.job.duration{job.type="discovery", monitor.type="cpu", connector_id="HPEGen10IloREST"} 0.030
metricshub.job.duration{job.type="discovery", monitor.type="temperature", connector_id="HPEGen10IloREST"} 0.025
metricshub.job.duration{job.type="discovery", monitor.type="connector", connector_id="HPEGen10IloREST"} 0.015
metricshub.job.duration{job.type="collect", monitor.type="cpu", connector_id="HPEGen10IloREST"} 0.015
```

In this example:

* during `discovery`:
  * The `enclosure` monitor takes `0.020` s.
  * The `cpu` monitor takes `0.030` s.
  * The `temperature` monitor takes `0.025` s.
  * The `connector` monitor takes `0.015` s.
* during `collect`, the `cpu` metrics collection takes `0.015` s.

These metrics indicate that **MetricsHub** is functioning as expected, with task durations well within acceptable ranges.

If task durations are above 5 seconds, consider the following:

* **Verify resource availability**: Ensure the monitored system has sufficient CPU, memory, and storage resources to handle monitoring tasks.
* **Check MetricsHub configuration**: Review your configuration to ensure **MetricsHub** is set up correctly .
* **Restart services**: If configurations appear correct, try restarting relevant services.
* **Inspect network configurations**: Check for network latency or connectivity issues between **MetricsHub** and the monitored resources, and ensure network settings (e.g., firewalls or proxies) are not causing delays.
* **Examine logs**: Look for warnings or errors in the [MetricsHub logs](./metricshub-logs.md) or the monitored system's logs to identify potential problems.
* **Review timeouts**: Ensure timeout settings are appropriate for the environment to prevent unnecessary delays or retries.
