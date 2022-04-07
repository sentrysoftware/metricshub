# Hardware Sentry Agent Extension

The **Hardware Sentry Agent** is the internal component which scrapes targets, collects metrics and pushes OTLP data to the OTLP receiver of the *OpenTelemetry Collector*. The `hws_agent` extension starts the **Hardware Sentry Agent** as a child process of the *OpenTelemetry Collector*, checks that this child process is up and running and restarts it if needed.

Configure the `hws_agent` extension as follows:
```yaml
  hws_agent:
    grpc: <http|https>://<host>:<port>   # Default: https://localhost:4317
    extra_args: [ <string> ... ]         # Example: [ --config=config/alternate-configuration-file.yaml ]
    restart_delay: <duration>            # Default: 10s
    retries: <int>                       # Default: -1 (Means no limit)
```
where:
- `grpc` is the endpoint to which the **Hardware Sentry Agent** will push OpenTelemetry data. By default, the **Hardware Sentry Agent** pushes metrics to the local *OTLP receiver* using [gRPC](https://grpc.io/) on port **TCP/4317** (By default: `https://localhost:4317`).
- `extra_args` specifies a list of additional arguments to be used by the **Hardware Sentry Agent**. By default, the **Hardware Sentry Agent**'s configuration file is **./config/hws-config.yaml**, you can provide an alternate configuration file by adding a new extra argument. Example: `--config=/opt/hws-otel-collector/config/hws-config-2.yaml`.
- `restart_delay` specifies the period of time after which the **Hardware Sentry Agent** is restarted when a problem has been detected. If not set, the **Hardware Sentry Agent** will be restarted after 10 seconds.
- `retries` specifies the number of restarts to be triggered until the **Hardware Sentry Agent** is up and running again. If not set, the extension will try restarting the **Hardware Sentry Agent** until it is up and running.

## Example Config

```yaml
extensions:
  hws_agent:
    grpc: https://localhost:4317

receivers:
  otlp:
    protocols:
        grpc:

service:
  receivers: [otlp]
  extensions: [hws_agent]
```
