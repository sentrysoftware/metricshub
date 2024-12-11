keywords: mysql, jdbc, database monitoring
description: How to configure MetricsHub MetricsHub to monitor MySQL databases using JDBC and export metrics to observability platforms like Prometheus

# Monitoring MySQL Databases

**MetricsHub** allows you to monitor MySQL databases running either on Windows or Linux and collect the following metrics:

* `db.server.io`: The number of bytes sent or received to/from all clients.
* `db.server.connections`: The number of connection attempts (successful or not) to the database.
* `db.server.connection.count`: The number of client connections to the database
* `db.server.uptime`: The total number of seconds the server has been up.
* `db.server.queries`: The total number of statements executed by the server, including statements within stored programs but excluding `COM_PING` and `COM_STATISTICS` commands
* `db.server.tables`: The total number of tables in the database.


![MetricsHub collects MySQL database metrics](../images/db-metrics.png)

In the example below, we configured **MetricsHub** to collect metrics about the `MySQL-Linux` resource hosted on `nb-docker` and push the collected metrics to Prometheus.


# Procedure

To monitor a MySQL database with **MetricsHub**: 

1. In the `config/metricshub.yaml` file, we configure the `MySQL-Linux` resource who is hosted on `nb-docker`, a Linux-based system:

    ```yaml
        MySQL-Linux:
            attributes:
            host.name: nb-docker
            host.type: linux 
    ```

2.  We configure the `jdbc` protocol by providing the credentials to connect to the database, the database `type` and the name of the instance to connect to: 

    ```yaml
            protocols:
            jdbc:
                username: root  
                password: mypassword
                type: mysql # Type of database
                database: mysql # Name of instance to connect to. 
    ```

    Here is the complete YAML configuration to be added to `config/metricshub.yaml` to monitor a MySQL database with **MetricsHub**

    ```yaml
        MySQL-Linux:
            attributes:
            host.name: nb-docker
            host.type: linux 
            protocols:
            jdbc:
                username: root  
                password: mypassword
                type: mysql
                database: mysql 
    ```
3. Finally we configure the [Prometheus integration](../prometheus/prometheus.md). 

After completing the configuration, we restart **MetricsHub** and check the logs and Prometheus to confirm that the MySQL metrics are being collected and exported successfully.

![MetricsHub pushes MySQL metrics to Prometheus](../images/mysql-linux-metrics.png)