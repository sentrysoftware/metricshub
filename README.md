# Matrix Reloaded

## Structure

This is a multi-module project:

* **/**: the root (parent of all submodules)
* **matrix-engine**: the brain, the heart of this project

## How to build the Project

### Requirements

* Have [Maven 3.x properly installed and configured](http://alpha.internal.sentrysoftware.net/lecloud/x/TwJn), with access to Sentry's repository.

### Build

To build the Matrix Reloaded package, from `./matrix-reloaded`:

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

All the encountered Checkstyle issues are reported under the `target/site` directory.

### Code Formatting

In this project, we maintain code formatting using `prettier-java`, a tool that helps ensure clean and consistent Java code. It automatically formats your code according to a predefined set of rules.

#### Prerequisites

- [Node version](https://nodejs.org/en/download/releases/) 10+
- [Prettier](https://github.com/prettier/prettier)

#### Installation

Before you start contributing to this project, it's essential to set up `prettier-java` on your local machine. Follow these steps to install it:

1. Visit the GitHub repository: [https://github.com/jhipster/prettier-java](https://github.com/jhipster/prettier-java)

2. Follow the installation guidelines provided in the repository's README.

```bash
# Local installation
npm install prettier-plugin-java --save-dev

# Or globally
npm install -g prettier prettier-plugin-java
```

#### Configuration

To ensure a consistent code formatting experience, we provide a `.prettierrc.yaml` configuration file in the root directory of the project. This configuration file defines the formatting rules and options used by 'prettier-java'. You don't need to modify this file unless you have specific formatting preferences.

#### Command Line Execution

Once `prettier-java` is installed, you can format your Java code effortlessly.
Simply navigate to your project directory and run the command on the files or directories you want to format. Examples:

```bash
# If you have installed the package locally
npx prettier --write "**/*.java"
```

To format specific java files you can run:

```bash
# Format a single file
npx prettier --write path/to/YourJavaFile.java

# Format all Java files in a directory
npx prettier --write path/to/YourJavaDirectory
````

#### Eclipse Setup

##### Configuration

1. Go to External Tools > External Tools Configurations...

2. Create a new External Tools Configuration.

3. Configure the following settings in your External Tools Configuration:
   * **Name**: Give your configuration a descriptive name, e.g., `Prettier-on-Java`.
   * **Location (Command)**: Set this to the full path of the `npx.cmd` executable, which is `C:\Program Files\nodejs\npx.cmd`.
   * **Arguments**: Enter the arguments you want to pass to `npx.cmd`, in this case, `prettier --write "**/*.java"`.
   * **Working Directory**: Set this to `${workspace_loc:/matrix-reloaded}` to ensure the command runs in the correct directory. Thus, `.prettierrc.yaml` is correctly loaded.

##### Execution

Go to External Tools > click `Prettier-on-Java` to format the code.
