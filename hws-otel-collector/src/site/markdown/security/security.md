keywords: security, password, encrypt, key, certificate, tls, authentication
description: Security mechanisms to encrypt passwords and secure the configuration files and its internal network communications.

# Security

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

## Overview

The **${project.name}** provides a set of security mechanisms to secure:

- The **Hardware Sentry Agent**'s passwords stored in the YAML configuration file
- The communications instantiated between the **Hardware Sentry Agent** and the **OpenTelemetry Collector**.

## Encryption

The **${project.name}** includes the `hws` CLI  CLI which can be used to encrypt the passwords configured in the `config/hws-config.yaml` file using the `hws-encrypt`  command to prevent sensitive data from being exposed. See [Passwords Encryption](passwords.md#Passwords_Encryption) for more details.

## OpenTelemetry Collector Security

### Receiver Security

By default, the internal `OTLP gRPC Receiver` of **${project.name}** only opens the `gRPC` listener on `localhost` to prevent malicious attacks.

The endpoint for the `OTLP gRPC Receiver` is configured as followed in the `config/otel-config.yaml` file:

```yaml
  otlp:
    protocols:
      grpc:
        endpoint: localhost:4317
```

### Transport Security

The **${project.name}** secures the communications instantiated from the **Hardware Sentry Agent**'s internal `OTLP Exporter` to the **OpenTelemetry Collector**'s internal `OTLP gRPC Receiver` through TLS.

The `OTLP gRPC Receiver` is configured as follows in the `config/otel-config.yaml` file:

```yaml
  otlp:
    protocols:
      grpc:
        endpoint: localhost:4317
        tls:
          cert_file: security/otel.crt
          key_file: security/otel.key

```

To customize the default TLS settings, see [Customizing TLS Certificates](settings.md#Customizing_TLS_Certificates).

### Requests Authentication

Once TLS is established, the `OTLP gRPC Receiver` authenticates any incoming request by using the `basicauth` authenticator.

The `OTLP gRPC Receiver` is configured as follows in the `config/otel-config.yaml` file:

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

To customize the default authentication secret, see [Customizing OTLP Authentication Password](settings.md#Customizing_OTLP_Authentication_Password).