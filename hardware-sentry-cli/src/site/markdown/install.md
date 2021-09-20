keywords: install,java,java_home,jre,path
description: Installing the ${project.name} is done by simply unzipping the archive in a dedicated folder.

# Installing the ${project.name} (`hws`)

## Prerequisites

You must have a Java Runtime Environment (JRE) **version 11 or higher** installed to execute the `hws` command.

By default, the `hws` command will use `JAVA_HOME` environment variable to determine the path to the JRE. If not set, it will use the first Java executable found in the `PATH` environment variable.

To verify the version installed on your system, run the following commands:

* On Linux

    ```batch
    $ $JAVA_HOME/bin/java -version
    $ java -version
    ```

* On Windows

    ```batch
    C:> %JAVA_HOME%\bin\java -version
    C:> java -version
    ```

You can download the latest versions of the Java Runtime Environment from <a href="https://adoptium.net/" target="_blank">Adoptium (formerly AdoptOpenJDK)</a>.

## Download

First, download **hws-${project.version}.zip** or **hws-${project.version}.tar.gz** from [Sentry Software's Web site](https://www.sentrysoftware.com/support/hardware-sentry-cli.html).

## On Windows

Unzip the content of **hws-${project.version}.zip** into a program folder. There is no need to create a specific subfolder for `hws` as the zip archive already contains an **hws** folder.

> Note: You will need administrative privileges to unzip into **C:\Program Files**.

Verify the installation with the command prompt. Navigate to the **bin** subfolder and execute the `hws --version` command:

![Verify the installation of hws on Windows](./images/hws-version-win.png)

To make it more convenient to run the `hws` command, you may want to add its **bin** directory to the system `%PATH%` environment variable (see Windows' System Properties > Advanced > Environment Variables).

## On Linux (and UNIX)

Untar the content of **hws-${project.version}.tar.gz** into a program folder, like **/usr/local** or **/opt**.

> Note: There is no need to create a specific subfolder for `hws` as the zip archive already contains an **hws** folder. However, you may need to ensure everyone's right to access the **hws** directory with the `chmod 755 hws` command.

```bash
/:> cd /usr/local
/usr/local:> sudo tar xvf /tmp/hws-${project.version}.tar.gz
/usr/local:> sudo chmod 755 hws
```

Verify the installation with the `hws --version` command:

![Verify the installation of hws on Windows](./images/hws-version-lin.png)

Additionally, you may want to create a symbolic link to the **hws** executable so you don't need to type its full path:

```bash
/:> sudo ln -s /usr/local/hws/bin/hws /usr/local/bin/hws
```
