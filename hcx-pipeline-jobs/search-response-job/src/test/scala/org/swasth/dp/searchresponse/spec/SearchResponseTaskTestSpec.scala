package org.swasth.dp.searchresponse.spec

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
import org.swasth.dp.searchresponse.task.{SearchResponseConfig, SearchResponseStreamTask}
import org.swasth.fixture.EventFixture

import java.util
import scala.collection.JavaConverters._

class SearchResponseTaskTestSpec extends BaseTestSpec{

  implicit val mapTypeInfo: TypeInformation[util.Map[String, AnyRef]] = TypeExtractor.getForClass(classOf[util.Map[String, AnyRef]])

  val flinkCluster = new MiniClusterWithClientResource(new MiniClusterResourceConfiguration.Builder()
    .setNumberSlotsPerTaskManager(1)
    .setNumberTaskManagers(1)
    .build)

  var postgresConnect: PostgresConnect = _

  val config: Config = ConfigFactory.load("search-response-test.conf")
  val baseJobConfig: BaseJobConfig = new BaseJobConfig(config,"CompositeSearchJob")
  val eligibilityCheckConfig: SearchResponseConfig = new SearchResponseConfig(config)
  val mockKafkaUtil: FlinkKafkaConnector = mock[FlinkKafkaConnector](Mockito.withSettings().serializable())
  val gson = new Gson()

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    flinkCluster.before()
  }

  override protected def afterAll():Unit = {
    flinkCluster.after()
  }


  "PreauthStreamTask" should "validate the retry flow because of wrong action for Preauth Submit" in {
    when(mockKafkaUtil.kafkaMapSource(eligibilityCheckConfig.kafkaInputTopic)).thenReturn(new PreauthSubmissionInvalidEventSource)
    val task = new SearchResponseStreamTask(eligibilityCheckConfig, mockKafkaUtil)
    task.process()

    AuditEventsSink.values.size() should be(1)
    RetryEventsSink.values.size() should be(1)

    JSONUtil.deserialize[util.Map[String,AnyRef]](AuditEventsSink.values.get(0)).get("audit") should be("yes")
  }

  "PreauthStreamTask" should "validate the success flow for Preauth Submit" in {
    when(mockKafkaUtil.kafkaMapSource(eligibilityCheckConfig.kafkaInputTopic)).thenReturn(new PreauthSubmissionCorrectEventSource)
    val task = new SearchResponseStreamTask(eligibilityCheckConfig, mockKafkaUtil)
    task.process()

    AuditEventsSink.values.size() should be(2)
    RetryEventsSink.values.size() should be(2)

    JSONUtil.deserialize[util.Map[String,AnyRef]](AuditEventsSink.values.get(0)).get("audit") should be("yes")
  }

  "PreauthStreamTask" should "validate the retry flow because of wrong action for Preauth Search" in {
    when(mockKafkaUtil.kafkaMapSource(eligibilityCheckConfig.kafkaInputTopic)).thenReturn(new PreauthSearchInvalidEventSource)
    val task = new SearchResponseStreamTask(eligibilityCheckConfig, mockKafkaUtil)
    task.process()

    AuditEventsSink.values.size() should be(3)
    RetryEventsSink.values.size() should be(3)

    JSONUtil.deserialize[util.Map[String,AnyRef]](AuditEventsSink.values.get(0)).get("audit") should be("yes")
  }

  "PreauthStreamTask" should "validate the success flow of preauth search" in {
    when(mockKafkaUtil.kafkaMapSource(eligibilityCheckConfig.kafkaInputTopic)).thenReturn(new PreauthSearchCorrectEventSource)
    val task = new SearchResponseStreamTask(eligibilityCheckConfig, mockKafkaUtil)
    task.process()

    AuditEventsSink.values.size() should be(4)
    RetryEventsSink.values.size() should be(4)

    JSONUtil.deserialize[util.Map[String,AnyRef]](AuditEventsSink.values.get(0)).get("audit") should be("yes")
  }

}

class PreauthSubmissionCorrectEventSource extends SourceFunction[util.Map[String, AnyRef]] {

  override def run(ctx: SourceContext[util.Map[String, AnyRef]]) {
    val gson = new Gson()
    val event = EventFixture.SAMPLE_PREAUTH_SUBMIT_EVENT
    val eventMap = gson.fromJson(event, new util.LinkedHashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]].asScala
    ctx.collect(eventMap.asJava)
  }

  override def cancel() = {}

}

class PreauthSubmissionInvalidEventSource extends SourceFunction[util.Map[String, AnyRef]] {

  override def run(ctx: SourceContext[util.Map[String, AnyRef]]) {
    val gson = new Gson()
    val event = EventFixture.SAMPLE_INVALID_PREAUTH_SUBMIT_ACTION_EVENT
    val eventMap = gson.fromJson(event, new util.LinkedHashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]].asScala
    ctx.collect(eventMap.asJava)
  }

  override def cancel() = {}

}

class PreauthSearchCorrectEventSource extends SourceFunction[util.Map[String, AnyRef]] {

  override def run(ctx: SourceContext[util.Map[String, AnyRef]]) {
    val gson = new Gson()
    val event = EventFixture.SAMPLE_PREAUTH_SEARCH_EVENT
    val eventMap = gson.fromJson(event, new util.LinkedHashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]].asScala
    ctx.collect(eventMap.asJava)
  }

  override def cancel() = {}

}

class PreauthSearchInvalidEventSource extends SourceFunction[util.Map[String, AnyRef]] {

  override def run(ctx: SourceContext[util.Map[String, AnyRef]]) {
    val gson = new Gson()
    val event = EventFixture.SAMPLE_INVALID_PREAUTH_SEARCH_ACTION_EVENT
    val eventMap = gson.fromJson(event, new util.LinkedHashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]].asScala
    ctx.collect(eventMap.asJava)
  }

  override def cancel() = {}

}


