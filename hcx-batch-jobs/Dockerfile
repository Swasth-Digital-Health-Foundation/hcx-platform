FROM sunbird/openjdk-java11-alpine:latest
ARG JAR_FILE=retry-batch-job/target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]