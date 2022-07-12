keywords: security, password, encrypt, key, certificate, tls, authentication
description: Security mechanisms to encrypt passwords and secure the configuration files and its internal network communications.

# Security

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

**${project.name}** provides a set of security mechanisms to secure:

* The **Hardware Sentry Agent**'s passwords stored in the `config/hws-config.yaml` file
* The communications instantiated between the **Hardware Sentry Agent** and the **OpenTelemetry Collector**.

## Encryption

Use the `hws-encrypt`  command to encrypt the passwords specified in the `config/hws-config.yaml` file. See [Passwords Encryption](passwords.md#Passwords_Encryption) for more details.

## OpenTelemetry Collector Security

### Receiver security

To prevent malicious attacks, the `gRPC` listener is by default only opened on `localhost`:

```yaml
  otlp:
    protocols:
      grpc:
        endpoint: localhost:4317
```

### Transport security

**${project.name}** secures the communications instantiated from the **Hardware Sentry Agent**'s internal `OTLP Exporter` to the **OpenTelemetry Collector**'s internal `OTLP gRPC Receiver` through TLS.

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

### Requests authentication

Once TLS is established, the `OTLP gRPC Receiver` uses the `basicauth` authenticator to authenticate any incoming request:

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