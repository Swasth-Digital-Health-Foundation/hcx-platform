package org.swasth.dp.claims.spec

import com.google.gson.Gson
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.apache.flink.test.util.MiniClusterWithClientResource
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.swasth.dp.BaseTestSpec
import org.swasth.dp.claims.task.{ClaimsConfig, ClaimsStreamTask}
import org.swasth.dp.core.job.{BaseJobConfig, FlinkKafkaConnector}
import org.swasth.dp.core.util.{JSONUtil, PostgresConnect}
import org.swasth.fixture.EventFixture

import java.util
import scala.collection.JavaConverters._

class ClaimsTaskTestSpec extends BaseTestSpec{

  implicit val mapTypeInfo: TypeInformation[util.Map[String, AnyRef]] = TypeExtractor.getForClass(classOf[util.Map[String, AnyRef]])

  val flinkCluster = new MiniClusterWithClientResource(new MiniClusterResourceConfiguration.Builder()
    .setNumberSlotsPerTaskManager(1)
    .setNumberTaskManagers(1)
    .build)

  var postgresConnect: PostgresConnect = _

  val config: Config = ConfigFactory.load("claims-test.conf")
  val baseJobConfig: BaseJobConfig = new BaseJobConfig(config,"claimsJob")
  val eligibilityCheckConfig: ClaimsConfig = new ClaimsConfig(config)
  val mockKafkaUtil: FlinkKafkaConnector = mock[FlinkKafkaConnector](Mockito.withSettings().serializable())
  val gson = new Gson()

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    when(mockKafkaUtil.kafkaStringSink(eligibilityCheckConfig.auditTopic)).thenReturn(new AuditEventsSink)
    flinkCluster.before()
  }

  override protected def afterAll():Unit = {
    flinkCluster.after()
  }


  "ClaimsStreamTask" should "validate the success flow for claims submit" in {
    when(mockKafkaUtil.kafkaMapSource(eligibilityCheckConfig.kafkaInputTopic)).thenReturn(new ClaimsSubmitEventSource)
    val task = new ClaimsStreamTask(eligibilityCheckConfig, mockKafkaUtil)
    task.process()

    AuditEventsSink.values.size() should be(1)
    RetryEventsSink.values.size() should be(1)

    JSONUtil.deserialize[util.Map[String,AnyRef]](AuditEventsSink.values.get(0)).get("audit") should be("yes")
  }

  "ClaimsStreamTask" should "validate the success flow for claims search" in {
    when(mockKafkaUtil.kafkaMapSource(eligibilityCheckConfig.kafkaInputTopic)).thenReturn(new ClaimsSearchEventSource)
    val task = new ClaimsStreamTask(eligibilityCheckConfig, mockKafkaUtil)
    task.process()

    AuditEventsSink.values.size() should be(2)
    RetryEventsSink.values.size() should be(1)

    JSONUtil.deserialize[util.Map[String,AnyRef]](AuditEventsSink.values.get(0)).get("audit") should be("yes")
  }

}

class ClaimsSubmitEventSource extends SourceFunction[util.Map[String, AnyRef]] {

  override def run(ctx: SourceContext[util.Map[String, AnyRef]]) {
    val gson = new Gson()
    val event = EventFixture.SAMPLE_CLAIMS_SUBMIT_EVENT
    val eventMap = gson.fromJson(event, new util.LinkedHashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]].asScala
    ctx.collect(eventMap.asJava)
  }

  override def cancel() = {}

}

class ClaimsSearchEventSource extends SourceFunction[util.Map[String, AnyRef]] {

  override def run(ctx: SourceContext[util.Map[String, AnyRef]]) {
    val gson = new Gson()
    val event = EventFixture.SAMPLE_CLAIMS_SEARCH_EVENT
    val eventMap = gson.fromJson(event, new util.LinkedHashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]].asScala
    ctx.collect(eventMap.asJava)
  }

  override def cancel() = {}

}
