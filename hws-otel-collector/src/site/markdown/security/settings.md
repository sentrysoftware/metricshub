keywords: security, password, encrypt, key, certificate, tls, authentication
description: How to configure ${project.name} security settings.

# Security Settings

<!-- MACRO{toc|fromDepth=1|toDepth=3|id=toc} -->

## Customizing TLS Certificates

If required, you can replace the default TLS certificate of the `OTLP gRPC Receiver`. After you replace the certificate, the data transmission that operates between the **Hardware Sentry Agent** and the **OpenTelemetry Collector** will have encryption provided by your own certificate.

### Prerequisites

- The new certificate file must be in PEM format and can contain one or more certificate chains. The first certificate compatible with the client's requirements is automatically selected.

- The new private key must be unencrypted and provided in PEM format.

- Since the internal communications operates only on `localhost` and by default, the **Hardware Sentry Agent**'s `OTLP Exporter` performs the hostname verification, the new certificate must include the `subjectAltName` extension indicating `DNS:localhost,IP:127.0.0.1`.

### Procedure

1. Generate your new private key and certificate files. E.g. `my-otel.key` and `my-otel.crt`.

2. Copy the generated certificate and private key files into the `security` directory.

3. Update the `tls:cert_file` and `tls:key_file` attributes of the `OTLP gRPC Receiver` in the `config/otel-config.yaml` file:

    ```yaml
    receivers:
      otlp:
        protocols:
          grpc:
            endpoint: localhost:4317
            tls:
              cert_file: security/my-otel.crt  # Your new certificate file.
              key_file: security/my-otel.key   # Your new private key file.
            auth:
              authenticator: basicauth
    ```

4. Set your new certificate (`security/my-otel.crt`) as `trustedCertificatesFile` in the `OTLP Exporter` configuration located in the `config/hws-config.yaml` file:

    ```yaml
    exporter:
      otlp:
        trustedCertificatesFile: security/my-otel.crt # Your new OTLP gRPC Receiver certificate.

    targets: # ...
    ```

5. Restart the ${project.name}. Refer to the [Installation](../install.md) page to see how to start the ${project.name}.

#### Generating a Self-Signed Certificate with OpenSSL (Example)

OpenSSL is a command line tool that helps you generate your X.509 certificates, you can use this tool to generate your Self-Signed Certificates.

> This is an example on how to generate a server certificate using the OpenSSL utility on a Linux machine. Your organization may define its own security policy to handle certificates and private keys. Before proceeding further, make sure that this procedure is right for your organization.

1. Create a private key for the Certificate Authority (CA):

   ```batch
   $ openssl genrsa 2048 > ca.key
   ```

2. Generate the X.509 certificate for the CA:

   ```batch
   $ openssl req -new -x509 -nodes -days 365000 \
      -key ca.key \
      -out ca.crt
   ```

3. Generate the private key and certificate request:

   ```batch
   $ openssl req -newkey rsa:2048 -nodes -days 365000 \
      -keyout my-otel.key \
      -out my-otel.req
   ```

4. Generate the X.509 certificate for the `OTLP gRPC Receiver`:
   
   ```batch
   $ openssl x509 -req -days 365000 -set_serial 01 \
     -in my-otel.req \
     -out my-otel.crt \
     -CA ca.crt \
     -CAkey ca.key \
     -extfile cert.conf -extensions req_ext
   ```

   Where the `cert.conf` file defines the extension to add to your certificate:

   ```
   [ req ]

   req_extensions = req_ext

   [ req_ext ]

   subjectAltName = DNS:localhost,IP:127.0.0.1
   ```

5. Your certificate (`my-otel.crt`) and private key (`my-otel.key`) are now generated in PEM format. You can verify your certificate as follows:

   ```batch
    $ openssl verify -CAfile ca.crt \
       ca.crt \
       my-otel.crt
   ```

## Customizing OTLP Authentication Password

If needed, you can update the password of the **OpenTelemetry Collector**'s `OTLP gRPC Receiver`. After you update the password, the `OTLP gRPC Receiver` will authenticate any received request using your new password.

### Prerequisites

- Have access to the `htpasswd` tool. 
   - On a Linux system, you can install the `httpd-tools` package.
   - On a Windows system, the `htpasswd` utility is embedded in one of the packages listed in the [*Downloading Apache for Windows*](https://httpd.apache.org/docs/2.4/platform/windows.html#down) page.


### Procedure

1. Create a new `.htpasswd-otel` file using your username and password:

   ```shell-session
   $ htpasswd -cbB .htpasswd-otel myUsername myPassword
   Adding password for user myUsername
   ```

2. Copy the `.htpasswd-otel` file into the `security` directory.

3. Update the `file` attribute of the `basicauth` extension in the `config/otel-config.yaml` file:

   ```yaml
   extensions:

     # ...

     basicauth:
       htpasswd:
         file: security/.htpasswd-otel  # Your new htpasswd file
   ```

4. Make sure the `basicauth` is declared as a service extension in the `config/otel-config.yaml` file:

   ```yaml
   service:

     # ...

     extensions: [health_check, basicauth, hws_agent] # basicauth is added to the extensions list
     pipelines:
     
     # ...
   ```

5. Make sure the `basicauth` extension is declared as `OTLP gRPC Receiver` *authenticator* in the `config/otel-config.yaml` file:

   ```yaml
   receivers:
     otlp:
       protocols:
         grpc:
           # ...
           auth:
             authenticator: basicauth
    ```

6. Generate a `base64` string using the same credentials that you have provided to generate the `.htpasswd-otel` file. 
   Join your username and password with a colon `myUsername:myPassword`, and then encode the resulting string in `base64`.

   ```shell-session
   $ echo -n 'myUsername:myPassword' | base64
   bXlVc2VybmFtZTpteVBhc3N3b3Jk
   ```

7. Add a new `Authorization` header under the `exporter:otlp:headers` section in the `config/hws-config.yaml` file:

   ```yaml
   exporter:
     otlp:
       headers:
        # ...

        Authorization: Basic bXlVc2VybmFtZTpteVBhc3N3b3Jk # Basic <base64-credentials>
   ```
   The `Authorization` header must be provided as `Basic <base64-credentials>`, where `<base64-credentials>` is the `base64` value you have generated in the previous step (6).

8. Restart the ${project.name}. Refer to the [Installation](../install.md) page to see how to start the ${project.name}.

## Disabling TLS (Not recommended)

When you disable TLS on ${project.name}, the communications between the **Hardware Sentry Agent** and the **OpenTelemetry Collector** moves to `HTTP` (unencrypted) instead of `HTTPS`.

1. Remove or comment out the `tls` section from the `OTLP gRPC Receiver` configuration in the `config/otel-config.yaml` file:

    ```yaml
    receivers:
      otlp:
        protocols:
          grpc:
            endpoint: localhost:4317
            #tls:                              # No TLS
            #  cert_file: security/my-otel.crt
            #  key_file: security/my-otel.key
            auth:
              authenticator: basicauth
    ```

2. In the `config/otel-config.yaml` file, update the `grpc` endpoint of the `hws-agent` extension to enable `HTTP`:

    ```yaml
    extensions:

      # ...

      # hwsagent
      # Starts the Hardware Sentry Agent as a child process of the OpenTelemetry Collector
      hws_agent:
        grpc: http://localhost:4317
      # ...
    ```

3. Remove or comment out the `trustedCertificatesFile` attribute of the `OTLP Exporter` in the `config/hws-config.yaml` file:

    ```yaml
    exporter:
      otlp:
        # trustedCertificatesFile: security/otel.crt

    targets: # ...
    ```

4. Restart the ${project.name}.

## Disabling Authentication (Not Recommended)

By disabling the authentication on ${project.name}, the **OpenTelemetry Collector**'s `OTLP gRPC Receiver` will skip the authentication step when intercepting metric requests.

1. Remove or comment out the `auth` section from the `OTLP gRPC Receiver` configuration in the `config/otel-config.yaml` file:

    ```yaml
    receivers:
      otlp:
        protocols:
          grpc:
            endpoint: localhost:4317
            tls:
              cert_file: security/my-otel.crt
              key_file: security/my-otel.key
            # auth:
              # authenticator: basicauth   # No authentication
    ```

2. Remove the `basicauth` extension from the service extensions list in the `config/otel-config.yaml` file:

   ```yaml
   service:

     # ...

     extensions: [health_check, hws_agent] # basicauth is not added to the extensions list
     pipelines:
     
     # ...
   ```

3. Remove or comment out the `Authorization` header from the `OTLP Exporter` configuration in the `config/hws-config.yaml` file:

    ```yaml
    exporter:
      otlp:
        trustedCertificatesFile: security/otel.crt
        headers:
          # Authorization: Basic bXlVc2VybmFtZTpteVBhc3N3b3Jk # Basic <base64-credentials>

    targets: # ...
    ```

4. Restart the ${project.name}.
