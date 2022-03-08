package org.swasth.dp.claims.spec

import com.google.gson.Gson
import com.typesafe.config.{Config, ConfigFactory}
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres

import java.util
import org.junit.Assert.assertEquals
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.apache.flink.test.util.MiniClusterWithClientResource
import org.mockito.Mockito
import org.mockito.Mockito._
import org.swasth.dp.claims.task.{ClaimsConfig, ClaimsStreamTask}
import org.swasth.dp.{BaseMetricsReporter, BaseTestSpec}
import org.swasth.dp.core.job.FlinkKafkaConnector
import org.swasth.dp.core.util.{PostgresConnect, PostgresConnectionConfig}

import scala.collection.JavaConverters._

class ClaimsStreamTaskTestSpec extends BaseTestSpec {

  implicit val mapTypeInfo: TypeInformation[util.Map[String, AnyRef]] = TypeExtractor.getForClass(classOf[util.Map[String, AnyRef]])

  val flinkCluster = new MiniClusterWithClientResource(new MiniClusterResourceConfiguration.Builder()
    .setConfiguration(testConfiguration())
    .setNumberSlotsPerTaskManager(1)
    .setNumberTaskManagers(1)
    .build)

  var postgresConnect: PostgresConnect = _
  val config: Config = ConfigFactory.load("test.conf")
  val eligibilityCheckConfig: ClaimsConfig = new ClaimsConfig(config)
  val mockKafkaUtil: FlinkKafkaConnector = mock[FlinkKafkaConnector](Mockito.withSettings().serializable())
  val gson = new Gson()

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    // Start the embeddedPostgress Server
    EmbeddedPostgres.builder.setPort(eligibilityCheckConfig.postgresPort).start() // Use the same port 5430 which is defined in the base-claims-test.conf
    // Clear the metrics
    BaseMetricsReporter.gaugeMetrics.clear()

    val postgresConfig = PostgresConnectionConfig(
      user = eligibilityCheckConfig.postgresUser,
      password = eligibilityCheckConfig.postgresPassword,
      database = eligibilityCheckConfig.postgresDb,
      host = eligibilityCheckConfig.postgresHost,
      port = eligibilityCheckConfig.postgresPort,
      maxConnections = eligibilityCheckConfig.postgresMaxConnections
    )
    postgresConnect = new PostgresConnect(postgresConfig)

    // Create the postgres Table
    postgresConnect.execute("CREATE TABLE IF NOT EXISTS payload(mid text PRIMARY KEY,data text)")

    /*
    * Update the few fields like (last_acccess , user_declared_on)
    * Since these fields should not get update again if the these fields are already present in the db's
    */
    val data = """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},"protocol":{"x-hcx-recipient_code":"67890","x-hcx-api_call_id":"26b1060c-1e83-4600-9612-ea31e0ca5091","x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"12345","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1","x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"cdata":{"sender":{"participant_code":"12345","signing_cert_path":"","roles":"admin","encryption_cert":"","endpoint_url":"","participant_name":"Test Provider","hfr_code":"0001","status":"Created"},"recipient":{"participant_code":"123456","signing_cert_path":"urn:isbn:0-476-27557-4","roles":"admin","encryption_cert":"urn:isbn:0-4234","endpoint_url":"/testurl","participant_name":"Test Provider","hfr_code":"0001","status":"Created"}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d","action":"coverageeligibility.check","logDetails":{"code":"bad.input","message":"Provider code not found","trace":""},"status":"Submitted"}"""
    val updateQuery = String.format("INSERT INTO %s (mid, data)  VALUES('%s','%s')", eligibilityCheckConfig.postgresTable, "761dfc11-1870-4981-b33d-16254a104a9d", data)

    // If need to truncate the table uncomment the below and execute the query
    val truncateQuery = String.format("TRUNCATE TABLE  %s RESTART IDENTITY", eligibilityCheckConfig.postgresTable)
    postgresConnect.execute(truncateQuery)
    postgresConnect.execute(updateQuery)



    val result1 = postgresConnect.executeQuery("SELECT data from payload where mid='761dfc11-1870-4981-b33d-16254a104a9d'")
    while ( {
      result1.next
    }) {
      assertEquals(data, result1.getString(1))
    }

    when(mockKafkaUtil.kafkaMapSource(eligibilityCheckConfig.kafkaInputTopic)).thenReturn(new ClaimsEventSource)
    when(mockKafkaUtil.kafkaStringSink(eligibilityCheckConfig.auditTopic)).thenReturn(new AuditEventsSink)
    flinkCluster.before()
  }

  "EligibilityCheckJob" should "validate the success flow" in {

    val task = new ClaimsStreamTask(eligibilityCheckConfig, mockKafkaUtil)
    task.process()
    BaseMetricsReporter.gaugeMetrics(s"${eligibilityCheckConfig.jobName}.${eligibilityCheckConfig.dispatcherSuccessCount}").getValue() should be(1)
    BaseMetricsReporter.gaugeMetrics(s"${eligibilityCheckConfig.jobName}.${eligibilityCheckConfig.auditEventsCount}").getValue() should be(1)
  }
}

class ClaimsEventSource extends SourceFunction[util.Map[String, AnyRef]] {

  override def run(ctx: SourceContext[util.Map[String, AnyRef]]) {
    val gson = new Gson()
    val event = """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},"protocol":{"x-hcx-recipient_code":"67890","x-hcx-api_call_id":"26b1060c-1e83-4600-9612-ea31e0ca5091","x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"12345","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1","x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"ctx":{"sender":{"participant_code":"12345","signing_cert_path":"","roles":"admin","encryption_cert":"","endpoint_url":"","participant_name":"Test Provider","hfr_code":"0001","status":"Created"},"recipient":{"participant_code":"123456","signing_cert_path":"urn:isbn:0-476-27557-4","roles":"admin","encryption_cert":"urn:isbn:0-4234","endpoint_url":"https://httpbin.org/post","participant_name":"Test Provider","hfr_code":"0001","status":"Created"}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d","action":"/v1/claim/submit","logDetails":{"code":"bad.input","message":"Provider code not found","trace":""},"status":"Submitted"}"""
    val eventMap = gson.fromJson(event, new util.LinkedHashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]].asScala
    ctx.collect(eventMap.asJava)
  }

  override def cancel() = {}

}

class RetryEventsSink extends SinkFunction[util.Map[String, AnyRef]] {

  override def invoke(value: util.Map[String, AnyRef]): Unit = {
    synchronized {
      RetryEventsSink.values.add(value)
    }
  }
}

object RetryEventsSink {
  val values: util.List[util.Map[String, AnyRef]] = new util.ArrayList()
}

class AuditEventsSink extends SinkFunction[String] {

  override def invoke(value: String): Unit = {
    synchronized {
      AuditEventsSink.values.add(value)
    }
  }
}

object AuditEventsSink {
  val values: util.List[String] = new util.ArrayList()
}