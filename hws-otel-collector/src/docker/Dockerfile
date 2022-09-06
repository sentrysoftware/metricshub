FROM openjdk:11

EXPOSE 8888 4317 13133

COPY . /opt/hws-otel-collector

WORKDIR /opt/hws-otel-collector/bin

RUN chmod +x hws-agent

ENTRYPOINT ["./hws-agent"]