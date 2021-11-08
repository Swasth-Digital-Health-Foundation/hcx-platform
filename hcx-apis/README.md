# HCX-APIs

## Protocol-Service local setup
This readme file contains the instruction to set up and run the protocol-service in local machine.

### Prerequisites:
* Maven
* Docker
* Kafka

### Kafka setup in docker:
1. Kafka stores information about the cluster and consumers into Zookeeper. ZooKeeper acts as a coordinator between them. we need to run two services(zookeeper & kafka), Prepare your docker-compose.yml file using the following reference.
```shell
version: '3'

services:
  zookeeper:
    image: 'wurstmeister/zookeeper:latest'
    container_name: zookeeper
    ports:
      - "2181:2181"    
    environment:
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://127.0.0.1:2181     
    
  kafka:
    image: 'wurstmeister/kafka:latest'
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      - KAFKA_BROKER_ID=1
      - KAFKA_ADVERTISED_HOST_NAME=127.0.0.1
      - KAFKA_LISTENERS=PLAINTEXT://:9092
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://127.0.0.1:9092
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181      
      - ALLOW_PLAINTEXT_LISTENER=yes
    depends_on:
      - zookeeper  
```
2. Go to the path where docker-compose.yml placed and run the below command to create and run the containers (zookeeper & kafka).
```shell
docker-compose -f docker-compose.yml up -d
```
3. To start kafka docker container shell, run the below command.
```shell
docker exec -it kafka sh
```
Go to path `/opt/kafka/bin`, where we will have executable files to perform operations(creating topics, running producers and consumers, etc) and create the topic using the following command.
```shell
kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic test 
```

### Running protocol-service:
1. Go to the path: `/hcx-platform/hcx-core` and run the below maven command to build the module.
```shell
mvn clean install -DskipTests
```
Similarly, build `/hcx-platform/hcx-apis` module. 
2. After build, go to the path: `/hcx-platform/hcx-apis` run the below maven command to start the tomcat server.
```shell
mvn spring-boot:run
```
3. Using the below command we can verify the application and its dependencies health. If all connections are good, health is shown as 'true' otherwise it will be 'false'.
```shell
curl http://localhost:8080/health
```
