FROM adoptopenjdk/openjdk11:latest

EXPOSE 8080

ADD /target/hws-agent-*.jar /hardware-sentry/hws-agent.jar

VOLUME     [ "/hardware-sentry" ]

WORKDIR    /hardware-sentry

ENTRYPOINT ["java", "-jar", "/hardware-sentry/hws-agent.jar"]