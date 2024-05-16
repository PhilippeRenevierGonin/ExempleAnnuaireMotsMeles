FROM openjdk:17-jdk-slim
COPY target/generateurgrille-1.jar /generateurgrille.jar
ENTRYPOINT java -Dserver.port=$PORT -jar /generateurgrille.jar $IP_ANNUAIRE