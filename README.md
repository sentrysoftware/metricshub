# Matrix (Hardware Sentry)

## Structure

This is a multi-module project:

* **/**: the root (parent of all submodules)
* **connector-serializer**: a small module used to serialize connectors in the **matrix-connectors** module
* **hardware-sentry-cli**: the CLI
* **hardware-sentry-agent**: the Agent
* **hws-otel-collector**: the OpenTelemetry Collector (**requires Go!**)
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

### Special options

If you do not have **Go** installed in your environment and do not wish to build the **OpenTelemetry Collector**, you can use the `-Dnogo` option:

```sh
$ mvn package -Dnogo
```
