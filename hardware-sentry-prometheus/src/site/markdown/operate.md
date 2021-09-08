# Launching ${project.name}

The simplest way to execute **${project.name}** consist in executing the following command:

```
$ java -jar hardware-sentry-prometheus-<version>.jar
```

## Using specific arguments

Use the following arguments to launch **${project.name}** with specific conditions.


```
$ java -jar hardware-sentry-prometheus-0.0.1-SNAPSHOT.jar <argument(s)>.yml
```

|Argument | Description |
|---------|------|
| ```--target.config.file```| To specify a custom yaml configuration file. Use the ```--target.config.file``` option followed by the relative or absolute path to the custom configuration YAML file (Example: ```--target.config.file=../custom-config.yml```).|
| ```--server.port```| To use a specific http port. Use the ```--server.port``` option to specify a port other than the default one (Example: ```--server.port=<port number>```).|
|```--server.ssl.enabled```| To enable the https protocol. Set the ```--server.ssl.enabled``` option to ```true``` (Example: ```--server.ssl.enabled=true```). |
|```--http.port```| To specify a prefered http port when the https protocol is enabled. Use the ```--http.port``` followed by the port number you wish to use (Example: ```--server.port=<port number>```).|
|```--server.ssl.redirect-http```| To redirect the http port the https port. Set the ```--server.ssl.redirect-http``` to ```true``` (Example: ```--server.ssl.redirect-http=true)```.|