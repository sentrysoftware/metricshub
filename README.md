# MetricsHub

## Structure

This is a multi-module project:

* **/**: the root (parent of all submodules)
* **metricshub-engine**: the brain, the heart of this project
* **metricshub-agent**: the MetricsHub Agent which includes a CLI
* **metricshub-windows**: builds the MSI package
* **metricshub-rhel**: builds the RPM package
* **metricshub-debian**: builds the Debian package
* **connector-serializer**: Serializes a set of connectors present in a directory

## How to build the Project

### Requirements

* Have [Maven 3.x properly installed and configured](http://alpha.internal.sentrysoftware.net/lecloud/x/TwJn), with access to Sentry's repository.

### Build

To build the MetricsHub package, from `./metricshub`:

```sh
$ mvn clean package
```

#### Building Windows Packages (.MSI)

* **Host:** Windows
* **JPackage Setup:** Install JDK 19 to enable the `jpackage` utility.
* **Environment Variable:** Set the system environment variable `JPACKAGE_HOME` to the home directory of JDK 19.
  * Verify that your `JPACKAGE_HOME` system environment variable is configured accurately, example:
    ```
	echo %JPACKAGE_HOME%
	C:\Program Files\Eclipse Adoptium\jdk-19.0.2.7-hotspot
	```
* **WiXToolSet Installation:** Download and install [WiX Toolset](https://github.com/wixtoolset/wix3/releases/tag/wix3112rtm) under `C:\Program Files (x86)\WiX Toolset v3.11`.
* Execute the `mvn package` command within the Metricshub root directory (`metricshub`). You can find the `.msi` package in the `metricshub/metricshub-windows/target` directory upon completion (`metricshub-windows-<version>.msi`).

#### Building Debian Packages (.DEB)

* **Host:** Debian Linux
* **JPackage Setup:** Install JDK 19 to enable the `jpackage` utility.
* **Environment Variable:** Set the system environment variable `JPACKAGE_HOME` to the home directory of JDK 19.
  * Verify that your `JPACKAGE_HOME` system environment variable is configured accurately, example:
    ```
	echo $JPACKAGE_HOME
	/opt/java/jdk-19.0.2+7
	```
* **Additional Packages:** Install the following packages:
  * `fakeroot` (`/usr/bin/fakeroot`)
  * `gcc-multilib`
* Execute the `mvn package` command within the Metricshub root directory (`metricshub`). You can find the `.deb` package in the `metricshub/metricshub-debian/target` directory upon completion (`metricshub-debian-<version>-amd64.deb`).
  * The `Docker` package that is compatible with the `debian:latest` image will also be generated under the `metricshub/metricshub-debian/target` directory (`metricshub-debian-<version>-docker.tar.gz`).

#### Building RHEL Packages (.RPM)

* **Host:** Red Hat Enterprise Linux (Centos, Ubuntu, etc.)
* **JPackage Setup:** Install JDK 19 to enable the `jpackage` utility
* **Environment Variable:** Set the system environment variable `JPACKAGE_HOME` to the home directory of JDK 19.
  * Verify that your `JPACKAGE_HOME` system environment variable is configured accurately, example:
    ```
	echo $JPACKAGE_HOME
	/opt/java/jdk-19.0.2+7
	```
* **Additional Packages:** Install the `rpm-build` package (`/usr/bin/rpmbuild`).
* Execute the `mvn package` command within the Metricshub root directory (`metricshub`). You can find the `.rpm` package in the `metricshub/metricshub-rhel/target` directory upon completion (`metricshub-rhel-<version>-1.x86_64.rpm`).


## Checkstyle

In this project, we use Checkstyle to ensure consistent and clean Java code across our codebase. 

Maven Checkstyle Plugin is configured globally in the main `pom.xml` file, and it verifies the Java code during the build process:

```xml
	<plugin>
		<artifactId>maven-checkstyle-plugin</artifactId>
		<version>3.3.0</version>
		<configuration>
			<sourceEncoding>${project.build.sourceEncoding}</sourceEncoding>
			<configLocation>checkstyle.xml</configLocation>
		</configuration>
		<executions>
			<execution>
				<id>validate</id>
				<phase>validate</phase>
				<goals>
					<goal>checkstyle</goal>
					<goal>check</goal>
				</goals>
			</execution>
		</executions>
	</plugin>
```

The Checkstyle rules that govern our code quality and style are defined in the `./checkstyle.xml` file. It's important to adhere to these rules to maintain code consistency and quality throughout the project.

The build will fail if one or more Checkstyle rules are violated.

To perform Checkstyle analysis and generate a report on violations, navigate to the directory of the Maven project you wish check and run the following `mvn` command:

```bash
mvn checkstyle:checkstyle
```

All the encountered Checkstyle issues are reported under the `target/site` directory.

To perform Checkstyle analysis and output violations to the console, navigate to the directory of the Maven project you wish check and run the following `mvn` command:

```bash
mvn checkstyle:check
```

## Code Formatting

In this project, we maintain code formatting using `prettier-java`, a tool that helps ensure clean and consistent Java code. It automatically formats your code according to a predefined set of rules.

### Prettier Maven Plugin

To automatically format the Java code in a specific Maven module, navigate to the directory of the Maven project you wish to format and run the following `mvn` command:

```bash
mvn prettier:write
```

To validate the formatted code, navigate to the directory of the Maven project you wish to check and run the following `mvn` command:

```bash
mvn prettier:check
```

The build will fail if you forgot to run Prettier.

## Submitting a PR

Before you submit a PR, make sure to use the available tools for code formatting, and ensure that the style checks and unit tests pass.
