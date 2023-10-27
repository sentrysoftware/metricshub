FROM debian:latest

EXPOSE 8888 4317 13133

COPY . /opt/metricshub

WORKDIR /opt/metricshub/bin

ENTRYPOINT ["./service"]