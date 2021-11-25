FROM anandp504/flink:1.12.0-scala_2.12-java11

USER root
RUN apt-get update
RUN apt-get install -y imagemagick

COPY target/jobs-distribution-1.0-SNAPSHOT.tar.gz /tmp
USER flink
RUN tar -xvf /tmp/jobs-distribution-1.0-SNAPSHOT.tar.gz -C $FLINK_HOME/lib/
USER root
RUN rm -f /tmp/jobs-distribution-1.0-SNAPSHOT.tar.gz
USER flink
