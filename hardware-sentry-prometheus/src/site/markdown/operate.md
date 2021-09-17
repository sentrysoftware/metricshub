keywords: prometheus exporter, hardware, launch, arguments, 
description: How to customize the Hardware Sentry Exporter for Prometheus configuration file.

# Launching ${project.name}

Run the command below to execute **${project.name}**:

```
$ java -jar ${project.artifactId}-${project.version}.jar
```

## Using specific arguments

Use the following arguments to launch **${project.name}** with specific conditions:

```
$ java -jar ${project.artifactId}-${project.version}.jar <argument(s)>
```

|Argument | Description |
|---------|------|
| ```--target.config.file```| To specify a custom yaml configuration file. Use the ```--target.config.file``` option followed by the relative or absolute path to the custom configuration YAML file. <br/>Example: ```--target.config.file=../custom-config.yml```.|
| ```--server.port```| To use a specific HTTP port. Use the ```--server.port``` option to specify a port other than the default one. <br/>Example: ```--server.port=8081```.|
|```--server.ssl.enabled```| To enable the HTTPS protocol. Set the ```--server.ssl.enabled``` option to ```true```. <br/>Example: ```--server.ssl.enabled=true```. |
|```--server.ssl.redirect-http```| To redirect the HTTP port to the HTTPS port. Set the ```--server.ssl.redirect-http``` to ```true```. <br/>Example: ```--server.ssl.redirect-http=true)```|