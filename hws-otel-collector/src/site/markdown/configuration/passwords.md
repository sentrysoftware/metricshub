keywords: password, encrypt, master, key
description: ${project.name} lets you encrypt the passwords stored in its YAML configuration files using a simple CLI.

# Password Encryption
To encrypt a password, use the `hws-encrypt` script on Unix systems or the `hws-encrypt.cmd` script on Windows systems.
You will be asked to type the password to encryt and the console will then print the encrypted password.
The encryption script will use a `hws-keystore.p12` file situated in a `security` folder to store encrypted passwords. If no such file is available, a new file will be created.
The keystore file himself is encrypted, don't modify it yourself or it won't be readable anymore.

# Using Encrypted Passwords
Once a password has been encrypted, it can be used in a `hws-config.yaml` configuration file instead of a real password.
For the password to be decrypted by the exporter, the keystore file previously created will be needed.
Do not move, rename or remove this file or the `security` folder.

##Example of use of an encrypted password

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
