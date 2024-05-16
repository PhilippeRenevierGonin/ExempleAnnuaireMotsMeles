FROM openjdk:17-jdk-slim
COPY target/words-service-1.jar /words-service.jar
COPY langues /langues
ENTRYPOINT java -Dserver.port=$PORT -jar /words-service.jar $IP_ANNUAIRE