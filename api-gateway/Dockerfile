FROM maven:3.8.4-openjdk-8-slim AS build  
WORKDIR /app

# copy the project files
COPY pom.xml ./pom.xml
COPY start.sh ./start.sh

# not useful for stateless builds
# RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -f /app/pom.xml package


# Create runtime image
FROM openjdk:8-jre-slim


WORKDIR /opt/java

COPY --from=build /app/target/*.jar /app/start.sh /opt/java/

RUN chmod +x /opt/java/start.sh

CMD ["/opt/java/start.sh"]