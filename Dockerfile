FROM eclipse-temurin:21.0.7_6-jre

WORKDIR /root/

RUN apt-get update && apt-get install -y netcat

COPY .docker/entrypoint.sh ./
COPY build/libs/*.jar ./

RUN chmod +x entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]
