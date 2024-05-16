FROM openjdk:17-jdk-slim
COPY target/annuaire-1.jar /annuaire.jar
ENTRYPOINT java -Dserver.port=$PORT -jar /annuaire.jar