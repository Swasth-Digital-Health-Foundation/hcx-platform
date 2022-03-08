package org.swasth.dp.coverageeligibilitycheck.spec

import com.google.gson.Gson
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.apache.flink.test.util.MiniClusterWithClientResource
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.swasth.dp.BaseTestSpec
import org.swasth.dp.core.job.{BaseJobConfig, FlinkKafkaConnector}
import org.swasth.dp.core.util.{JSONUtil, PostgresConnect}
import org.swasth.dp.coverageeligiblitycheck.task.{CoverageEligibilityCheckConfig, CoverageEligibilityCheckStreamTask}
import org.swasth.fixture.EventFixture

import scala.collection.JavaConverters._
import java.util

class CoverageEligibilityStreamTaskSpec extends BaseTestSpec{

  implicit val mapTypeInfo: TypeInformation[util.Map[String, AnyRef]] = TypeExtractor.getForClass(classOf[util.Map[String, AnyRef]])

  val flinkCluster = new MiniClusterWithClientResource(new MiniClusterResourceConfiguration.Builder()
    .setNumberSlotsPerTaskManager(1)
    .setNumberTaskManagers(1)
    .build)

  var postgresConnect: PostgresConnect = _

  val config: Config = ConfigFactory.load("coverage-eligibility-check.conf")
  val baseJobConfig: BaseJobConfig = new BaseJobConfig(config,"coverageEligibilityCheck")
  val eligibilityCheckConfig: CoverageEligibilityCheckConfig = new CoverageEligibilityCheckConfig(config)
  val mockKafkaUtil: FlinkKafkaConnector = mock[FlinkKafkaConnector](Mockito.withSettings().serializable())
  val gson = new Gson()

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    when(mockKafkaUtil.kafkaMapSource(eligibilityCheckConfig.kafkaInputTopic)).thenReturn(new EligibilityEventSource)
    when(mockKafkaUtil.kafkaStringSink(eligibilityCheckConfig.auditTopic)).thenReturn(new AuditCoverageEventsSink)
    flinkCluster.before()
  }

  override protected def afterAll():Unit = {
    flinkCluster.after()
  }


  "CoverageEligibilityCheckStreamTask" should "validate the success flow" in {

    val task = new CoverageEligibilityCheckStreamTask(eligibilityCheckConfig, mockKafkaUtil)
    task.process()

    AuditCoverageEventsSink.values.size() should be(1)
    RetryEventsSink.values.size() should be(0)
    JSONUtil.deserialize[util.Map[String,AnyRef]](AuditCoverageEventsSink.values.get(0)).get("eid") should be("AUDIT")
  }

}



class EligibilityEventSource extends SourceFunction[util.Map[String, AnyRef]] {

  override def run(ctx: SourceContext[util.Map[String, AnyRef]]) {
    val gson = new Gson()
    //val event = """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},"protocol":{"x-hcx-recipient_code":"0c918aa1-3a54-4e56-80dc-1d4643771a7d","x-hcx-api_call_id":"26b1060c-1e83-4600-9612-ea31e0ca5091","x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"f82d01f9-b334-4d0b-9933-5fae7a2c3cb5","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1","x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"ctx":{"sender":{"participant_code":"12345","signing_cert_path":"","roles":"admin","encryption_cert":"","endpoint_url":"","participant_name":"Test Provider","hfr_code":"0001","status":"Created"},"recipient":{"participant_code":"123456","signing_cert_path":"urn:isbn:0-476-27557-4","roles":"admin","encryption_cert":"urn:isbn:0-4234","endpoint_url":"https://httpbin.org/post","participant_name":"Test Provider","hfr_code":"0001","status":"Created"}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d","action":"/v1/coverageeligibility/check","logDetails":{"code":"bad.input","message":"Provider code not found","trace":""},"status":"Submitted"}"""
    val event = EventFixture.SAMPLE_EVENT
    val eventMap = gson.fromJson(event, new util.LinkedHashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]].asScala
    ctx.collect(eventMap.asJava)
  }

  override def cancel() = {}

}

class AuditCoverageEventsSink extends SinkFunction[String] {

  override def invoke(value: String): Unit = {
    synchronized {
      AuditCoverageEventsSink.values.add(value)
    }
  }
}

object AuditCoverageEventsSink {
  val values: util.List[String] = new util.ArrayList()
}


