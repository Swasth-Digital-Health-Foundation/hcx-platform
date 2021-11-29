# hcx-pipeline-jobs
Background and pipeline jobs of HCX Platform

## Denormaliser Job local setup
This readme file contains the instruction to set up and run the denormaliser job in local machine.

### Prerequisites:
* Flink-1.12.0
* Maven
* Docker
* Kafka
* Redis

### Running Denormaliser Job:
1. Go to the path: `/hcx-pipeline-jobs` and run the below maven command to build the modules.
```shell
mvn clean install -DskipTests
```
2. Start the flink cluster, access the flink dashboard using `http://localhost:8081/`
3. In flink dashboard select `Submit New Job` and get the jar file from the path `/hcx-pipeline-jobs/denormaliser/target` and upload.
4. After uploading, submit the job and it will start running.
5. Kafka topics are defined in application.properties, please create the same topics in kafka.
   * Input Topic - `local.hcx.request.ingest`
   * Output Topic -`local.hcx.request.denorm`
6. To test the job, write an event to the input topic, denormaliser job will read from it, processes and write to the output topic.
