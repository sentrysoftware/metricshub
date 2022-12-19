keywords: password, encrypt, master, key, security
description: ${solutionName} lets you encrypt the passwords stored in its YAML configuration files using a simple CLI.

# Password Encryption

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

## Encrypting Passwords

To encrypt a password, run the `hws-encrypt` command in an interactive terminal:

```shell-session
/$ cd /opt/hws/bin
/opt/hws/bin$ ./hws-encrypt
Enter the password to encrypt: <type the password>
GkwzG6bx8cUhoeQW+/1ERI+2LOyB
```

## Using Encrypted Passwords

Once a password has been encrypted, it can be used in a `hws-config.yaml` configuration file instead of a readable password:

```yaml
hosts:

  - host:
      hostname: myhost-01
      type: storage
    http:
      https: true
      port: 443
      username: myusername
      password: GkwzG6bx8cUhoeQW+/1ERI+2LOyB
```

## The *Master Password*

On first use, the `hws-encrypt` command will create the **security/hws-keystore.p12** file to store a unique and random *master password*. This *master password* is used to encrypt passwords with `hws-encrypt`, and decrypt them from **config/hws-config.yaml**.

The **hws-keystore.p12** file must not be modified, as this would prevent decryption from working. Any password encrypted with a given **hws-keystore.p12** *master password* must be decrypted with the exact same **hws-encrypt.p12** file.

The **hws-keystore.p12** file can be shared across several hosts so that a password encrypted on one system can be decrypted on another. Simply copy the **hws-keystore.p12** file to the **security** directory. This will make **${solutionName}** able to decrypt the passwords.

> **Note**: On Windows, the **security** directory is located under the **ProgramData\hws** directory (`C:\ProgramData\hws\security`) and accessible to *Administrators* only. On Linux, the **security** directory is located under the installation directory (`/opt/hws/security`).
