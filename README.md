# MetricsHub

## Structure

This is a multi-module project:

* **/**: the root (parent of all submodules)
* **metricshub-engine**: the brain, the heart of this project
* **metricshub-agent**: the MetrricsHub Agent which includes a CLI
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
