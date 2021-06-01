FROM adoptopenjdk/openjdk11:latest

EXPOSE 8080

ADD /target/hardware-sentry-prometheus-*.jar /hardware-sentry/hardware-sentry-prometheus.jar

VOLUME     [ "/hardware-sentry" ]

WORKDIR    /hardware-sentry

ENTRYPOINT ["java", "-jar", "/hardware-sentry/hardware-sentry-prometheus.jar"]