FROM openjdk:8-jre-slim
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/
COPY start.sh /app/
CMD ["/app/start.sh"]