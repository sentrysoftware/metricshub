FROM adoptopenjdk/openjdk11:latest

EXPOSE 8080

ADD /target/hardware-sentry-exporter-*.jar /hardware-sentry/hardware-sentry-exporter.jar

VOLUME     [ "/hardware-sentry" ]

WORKDIR    /hardware-sentry

ENTRYPOINT ["java", "-jar", "/hardware-sentry/hardware-sentry-exporter.jar"]