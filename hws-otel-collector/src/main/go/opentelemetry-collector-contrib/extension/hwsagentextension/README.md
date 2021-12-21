# Hardware Sentry Agent Extension

The **Hardware Sentry Agent** is the internal component which scrapes targets, collects metrics and pushes OTLP data to the OTLP receiver of the *OpenTelemetry Collector*. The `hws_agent` extension starts the **Hardware Sentry Agent** as a child process of the *OpenTelemetry Collector*.

```yaml
  hws_agent:
    grpc: http://localhost:4317
```

The `hws_agent` extension checks that **Hardware Sentry Agent** is up and running and restarts its process if needed.

The example above shows how to configure **Hardware Sentry Agent** to push metrics to the local _OTLP receiver_ using [gRPC](https://grpc.io/) on port **TCP/4317**.
If your OTLP receiver runs on another host or uses a different protocol or port, you will need to update the `grpc` option. Format: `<http|https>://<host>:<port>`.

By default, the **Hardware Sentry Agent**'s configuration file is **config/hws-config.yaml**. You can provide an alternate configuration file using the `--config` argument in the `extra_args` section.

```yaml
  hws_agent:
    grpc: http://localhost:4317
    extra_args:
      - --config=config\hws-config-2.yaml
```

## Configuration

Eventually configure these settings:

* `grpc`: the endpoint to which the **Hardware Sentry Agent** is going to push OpenTelemetry data. Default: `http://localhost:4317`.
* `extra_args`: the additional arguments for the **Hardware Sentry Agent**, such us `--config=config\hws-config-2.yaml`.
* `restart_delay`: to indicate the period of time after which the **Hardware Sentry Agent** is restarted when a problem has been detected. If not set, the **Hardware Sentry Agent** will be restarted after 10 sec.
* `retries`: Number of restarts to be triggered until the **Hardware Sentry Agent** is up and running again. If not set, the extension will try restarting the **Hardware Sentry Agent** until it is up and running.

## Example Config

```yaml
extensions:
  hws_agent:
    grpc: http://localhost:4317

receivers:
  otlp:
    protocols:
        grpc:

service:
  receivers: [otlp]
  extensions: [hws_agent]
```
