FROM sunbird/openjdk-java11-alpine:latest
ARG JAR_FILE=common-scheduler-job/target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/ap.jar"]