FROM flink:1.18.1-java17

USER flink
ADD target/jobs-distribution-1.0.tar.gz $FLINK_HOME/lib/

#FROM eclipse-temurin:17.0.9_9-jdk-focal AS builder
#FROM flink:1.15.2
#USER flink
#ADD target/jobs-distribution-1.0.tar.gz $FLINK_HOME/lib/
#ENTRYPOINT ["java","-jar","/app.jar"]

