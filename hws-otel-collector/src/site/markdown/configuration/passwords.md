keywords: password, encrypt, master, key, security
description: ${project.name} lets you encrypt the passwords stored in its YAML configuration files using a simple CLI.

# Password Encryption

## Encrypting Passwords

To encrypt a password:
1. Run the encryption script corresponding to your environment, located in `hws-exporter/bin` folder of the installation package.
    * On Windows systems: use the `hws-encrypt.cmd` script
    * On Linux systems: use the `hws-encrypt` script
2. Type the password to encrypt. The console will print the encrypted password.

**The encryption script will use a** `hws-keystore.p12` **file located in a** `security` **folder to store encrypted passwords.**

**If no such file is available, a new one will be created.**

## Using Encrypted Passwords

Once a password has been encrypted, it can be used in a `hws-config.yaml` configuration file instead of a readable password.

**For the password to be decrypted by ${project.name}, the keystore file previously created will be needed. Do not move, rename or remove this file or the** `security` **folder.**

### Example

```yaml
targets:

  - target:
      hostname: myhost-01
      type: storage
    http:
      https: true
      port: 443
      username: myusername
      password: GkwzG6bx8cUhoeQW+/1ERI+2LOyB
```
