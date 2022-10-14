# Matrix (Hardware Sentry)

## Structure

This is a multi-module project:

* **/**: the root (parent of all submodules)
* **connector-serializer**: a small module used to serialize connectors in the **matrix-connectors** module
* **hws-agent**: the Hardware Sentry Agent which includes a CLI
* **hws-agent-mapping**: defines OpenTelemetry metrics mapping
* **hws-windows**: builds the MSI package
* **hws-rhel**: builds the RPM package
* **hws-debian**: builds the Debian package
* **hws-doc**: Hardware Sentry documentation module (site)
* **matrix-connectors**: a pom project only to retrieve the sources of the Hardware Connector Library and serialize them as an artifact for this project
* **matrix-engine**: the brain, the heart of this project

## How to build the Project

### Requirements

* Have [Maven 3.x properly installed and configured](http://alpha.internal.sentrysoftware.net/lecloud/x/TwJn), with access to Sentry's repository.
* [Go properly installed](https://golang.org/doc/install), with the `go` executable in your `$PATH`

### Build

To build the Matrix Engine package, from `./matrix`:

```sh
$ mvn clean package
```

### Windows Build Rules (.MSI)

- Windows host.
- Define the `JPACKAGE_HOME` system env variable in order to access the jpackage tool. Example: `JPACKAGE_HOME=C:\Program Files\Java\jdk-19`
- WixToolSet installed under `C:\Program Files (x86)\WiX Toolset v3`.
- Define the `JAVA_RUNTIME_HOME` system env variable in order to embed the runtime image in the final package. Example: `JAVA_RUNTIME_HOME=C:\Program Files\Java\jdk-11.0.16.1+1-jre`

### Debian Build Rules (.DEB)

- Debian Linux host.
- Define the `JPACKAGE_HOME` system env variable in order to access the jpackage tool. Example: `JPACKAGE_HOME=/opt/java/jdk-19+36`
- Install the `fakeroot` package. (`/usr/bin/fakeroot`).
- Install `gcc-multilib`.
- Define the `JAVA_RUNTIME_HOME` system env variable in order to embed the runtime image in the final package. Example: `JAVA_RUNTIME_HOME=/opt/java/jdk-11.0.16.1+1-jre`

### RHEL Build Rules (.RPM)

- Red Hat Enterprise Linux host.
- Define the `JPACKAGE_HOME` system env variable in order to access the jpackage tool. (jpackage from JDK19)
- Install the `rpm-build` package. (`usr/bin/rpmbuild`).
- Define the `JAVA_RUNTIME_HOME` system env variable in order to embed the runtime image in the final package.