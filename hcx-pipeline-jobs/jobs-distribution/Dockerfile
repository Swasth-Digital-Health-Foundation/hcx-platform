#FROM flink:1.15.2
#
#USER flink
#ADD target/jobs-distribution-1.0.tar.gz $FLINK_HOME/lib/

FROM eclipse-temurin:17.0.9_9-jdk-focal AS builder
FROM flink:1.15.2
USER flink
COPY --from=builder /path/to/your/app.jar $FLINK_HOME/lib/app.jar
ENTRYPOINT ["java", "-jar", "/opt/flink/lib/app.jar"]

