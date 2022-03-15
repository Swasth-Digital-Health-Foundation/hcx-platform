package org.swasth.dp.payments.spec

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
import org.swasth.dp.core.job.{BaseJobConfig, FlinkKafkaConnector}
import org.swasth.dp.core.util.{JSONUtil, PostgresConnect}
import org.swasth.dp.payments.task.{PaymentsConfig, PaymentsStreamTask}
import org.swasth.fixture.EventFixture

import java.util
import scala.collection.JavaConverters._

class PaymentsTaskTestSpec extends BaseTestSpec{

  implicit val mapTypeInfo: TypeInformation[util.Map[String, AnyRef]] = TypeExtractor.getForClass(classOf[util.Map[String, AnyRef]])

  val flinkCluster = new MiniClusterWithClientResource(new MiniClusterResourceConfiguration.Builder()
    .setNumberSlotsPerTaskManager(1)
    .setNumberTaskManagers(1)
    .build)

  var postgresConnect: PostgresConnect = _

  val config: Config = ConfigFactory.load("payments-test.conf")
  val baseJobConfig: BaseJobConfig = new BaseJobConfig(config,"payments")
  val eligibilityCheckConfig: PaymentsConfig = new PaymentsConfig(config)
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


  "PaymentsStreamTask" should "validate the success flow for payments search" in {
    when(mockKafkaUtil.kafkaMapSource(eligibilityCheckConfig.kafkaInputTopic)).thenReturn(new PaymentsSearchEventSource)
    val task = new PaymentsStreamTask(eligibilityCheckConfig, mockKafkaUtil)
    task.process()

    AuditEventsSink.values.size() should be(1)

    JSONUtil.deserialize[util.Map[String,AnyRef]](AuditEventsSink.values.get(0)).get("audit") should be("yes")
  }

  "PaymentsStreamTask" should "validate the success flow for payments request" in {
    when(mockKafkaUtil.kafkaMapSource(eligibilityCheckConfig.kafkaInputTopic)).thenReturn(new PaymentsRequestEventSource)
    val task = new PaymentsStreamTask(eligibilityCheckConfig, mockKafkaUtil)
    task.process()

    AuditEventsSink.values.size() should be(2)

    JSONUtil.deserialize[util.Map[String,AnyRef]](AuditEventsSink.values.get(0)).get("audit") should be("yes")
  }

}

class PaymentsRequestEventSource extends SourceFunction[util.Map[String, AnyRef]] {

  override def run(ctx: SourceContext[util.Map[String, AnyRef]]) {
    val gson = new Gson()
    val event = EventFixture.SAMPLE_PAYMENTS_REQUEST_EVENT
    val eventMap = gson.fromJson(event, new util.LinkedHashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]].asScala
    ctx.collect(eventMap.asJava)
  }

  override def cancel() = {}

}

class PaymentsSearchEventSource extends SourceFunction[util.Map[String, AnyRef]] {

  override def run(ctx: SourceContext[util.Map[String, AnyRef]]) {
    val gson = new Gson()
    val event = EventFixture.SAMPLE_PAYMENTS_SEARCH_EVENT
    val eventMap = gson.fromJson(event, new util.LinkedHashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]].asScala
    ctx.collect(eventMap.asJava)
  }

  override def cancel() = {}

}
