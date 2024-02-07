FROM eclipse-temurin:17.0.9_9-jdk-focal
ARG JAR_FILE=common-scheduler-job/target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]