## Structure

This is a multi-module project:

* `./`: Matrix (root) project, used to build the entire solution.
* `./connector-compiler-plugin`: Plugin project that you can build and call when building the Hardware Connector Library.
* `./matrix-engine`: Java project with the business logic code to interpret connectors and perform hardware device detections, discoveries, collects and alerting.

> Note: The `connector-compiler-plugin` is not referenced in `matrix-engine`.

## How to build the Project

### Requirements

- Have [Maven 3.x properly installed and configured](http://alpha.internal.sentrysoftware.net/lecloud/x/TwJn), with access to Sentry's repository.

### Package

To build the Matrix Engine package, from `./matrix`:

```sh
$ mvn clean package
```
This *goals* will produce the requested packages in the `./matrix-engine/target` directory:

* `matrix-engine-${project.version}.jar`

### Simple Compile

If you're just modifying the Java code, you may want to just make sure the code compiles properly. This is easily achieved with the below command, from the **./matrix/** directory or any other sub-directory, e.g. **matrix/matrix-engine/** or **./matrix/connector-compiler-plugin/** :

```sh
$ mvn compile
```

### Full Package

To build and install the Matrix Engine artifact in your local repository, you only need to run these commands from the `./matrix` folder:

```sh
$ mvn clean install
```

## Deploy

To build and deploy the entire project, all you need is to run from the `./matrix` root project:

```sh
$ mvn clean deploy
```
This will package, install and deploy the *matrix*, *matrix-engine* and *connector-compiler-plugin* artifacts.
