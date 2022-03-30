keywords: security, password, encrypt, key, certificate, tls, authentication
description: ${project.name} delivers several security mechanisms to help secure its passwords stored in the YAML configuration files and its internal network communications.

# Security

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

## Overview

The ${project.name} provides a set of security aspects to help securing:
- The **Hardware Sentry Agent**'s passwords stored in the YAML configuration file.
- The communications instantiated between the **Hardware Sentry Agent** and the **OpenTelemetry Collector**.

## Encryption

The ${project.name} wraps the `hws-encrypt` CLI which can be used to encrypt the passwords configured in the `config/hws-config.yaml` file. This protects the loss of sensitive data if your configuration file is exposed.

To understand how the `hws-encrypt` CLI works, review the procedure defined in the [Passwords Encryption](passwords.md#Passwords_Encryption) page.

## OpenTelemetry Collector Security

### Receiver Security

To avoid any incorrect data feed, by default, the ${project.name}'s internal `OTLP gRPC Receiver` ensures to open its `gRPC` listener on `localhost` only.

Here is how the endpoint is configured for the `OTLP gRPC Receiver` (`config/otel-config.yaml`):

```yaml
  otlp:
    protocols:
      grpc:
        endpoint: localhost:4317
```

### Transport Security

The ${project.name} secures the communications instantiated from the **Hardware Sentry Agent**'s internal `OTLP Exporter` to the **OpenTelemetry Collector**'s internal `OTLP gRPC Receiver` through TLS.

Here is the `OTLP gRPC Receiver` configuration including TLS (`config/otel-config.yaml`):

```yaml
  otlp:
    protocols:
      grpc:
        endpoint: localhost:4317
        tls:
          cert_file: security/otel.crt
          key_file: security/otel.key

```

To customize the default TLS settings, see [Customize TLS Certificates](settings.md#Customize_TLS_Certificates).
### Requests Authentication

Once TLS is established, each metric request moves to the authentication step. That's why the `OTLP gRPC Receiver` is configured to authenticate any incoming request using the `basicauth` authenticator.

Here is the `OTLP gRPC Receiver` configuration including the authenticator (`config/otel-config.yaml`):

```yaml
  otlp:
    protocols:
      grpc:
        endpoint: localhost:4317
        tls:
          cert_file: security/otel.crt
          key_file: security/otel.key
        auth:
          authenticator: basicauth
```

To customize the default authentication secret, see [Customize OTLP Authentication Password](settings.md#Customize_OTLP_Authentication_Password).