FROM eclipse-temurin:11.0.20.1_1-jdk-focal
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]