FROM sunbird/openjdk-java11-alpine:latest
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
COPY src/main/resources src/main/resources
ENTRYPOINT ["java","-jar","/app.jar"]