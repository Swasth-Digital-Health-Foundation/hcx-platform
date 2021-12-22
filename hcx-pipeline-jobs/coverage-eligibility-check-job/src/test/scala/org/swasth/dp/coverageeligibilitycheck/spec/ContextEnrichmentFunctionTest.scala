package org.swasth.dp.coverageeligibilitycheck.spec

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord
import org.apache.flink.streaming.util.ProcessFunctionTestHarnesses
import org.scalatest.{FlatSpec, Matchers}
import org.swasth.dp.core.function.ContextEnrichmentFunction
import org.swasth.dp.core.job.BaseJobConfig
import org.swasth.dp.core.util.JSONUtil
import org.swasth.dp.coverageeligiblitycheck.task.CoverageEligibilityCheckConfig
import org.swasth.fixture.EventFixture
import scala.collection.JavaConverters._
import java.util

class ContextEnrichmentFunctionTest extends FlatSpec with Matchers{

  val config: Config = ConfigFactory.load("coverage-eligibility-check.conf")
  val baseJobConfig: BaseJobConfig = new BaseJobConfig(config,"coverageEligibilityCheck")
  val eligibilityCheckConfig: CoverageEligibilityCheckConfig = new CoverageEligibilityCheckConfig(config)

  //instantiate created user defined function
  val processFunction = new ContextEnrichmentFunction(baseJobConfig)
  val objMapper = new ObjectMapper
  val gson = new Gson()

  "ContextEnrichmentFunction" should "write data into enriched topic" in {
    // wrap user defined function into a the corresponding operator
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)

    //StreamRecord object with the sample data
    val eventMap = gson.fromJson(EventFixture.SAMPLE_EVENT, new util.HashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]]
    val testData: StreamRecord[util.Map[String, AnyRef]] = new StreamRecord[util.Map[String, AnyRef]](eventMap)
    harness.processElement(testData)

    harness.getSideOutput(eligibilityCheckConfig.enrichedOutputTag) should have size 1

    harness.close()
  }

  "ContextEnrichmentFunction" should " have sender and recipient full details" in {
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)
    val testEvent = JSONUtil.deserialize[util.Map[String, AnyRef]](EventFixture.SAMPLE_EVENT)
    harness.processElement(testEvent, new java.util.Date().getTime)
    val outputs = harness.getSideOutput(eligibilityCheckConfig.enrichedOutputTag).asScala.map {
      event => event.getValue
    }
    outputs.size should be(1)

    outputs.map {
      event => {
        Console.println(event)
        event.get("cdata").asInstanceOf[util.Map[String, AnyRef]].get("sender").asInstanceOf[util.Map[String, AnyRef]]
          .get("participantCode").asInstanceOf[String] should be("10002")
        event.get("cdata").asInstanceOf[util.Map[String, AnyRef]].get("recipient").asInstanceOf[util.Map[String, AnyRef]]
          .get("participantCode").asInstanceOf[String] should be("10001")
      }
    }
  }

  "ContextEnrichmentFunction" should " have wrong sender and wrong recipient details" in {
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)
    val testEvent = JSONUtil.deserialize[util.Map[String, AnyRef]](EventFixture.SAMPLE_WRONG_EVENT)
    harness.processElement(testEvent, new java.util.Date().getTime)
    val outputs = harness.getSideOutput(eligibilityCheckConfig.enrichedOutputTag).asScala.map {
      event => event.getValue
    }
    outputs.size should be(1)

    outputs.map {
      event => {
        Console.println(event)
        event.get("mid") should be("761dfc11-1870-4981-b33d-16254a104a9d")
        event.get("action") should be("/v1/coverageeligibility/check")
        val senderCtx = event.getOrDefault("cdata", new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]].getOrDefault("sender", new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]]
        senderCtx.size() should be (0)
        val recipientCtx = event.getOrDefault("cdata", new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]].getOrDefault("recipient", new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]]
        recipientCtx.size() should be (0)
      }
    }
  }

  "ContextEnrichmentFunction" should " have wrong sender details" in {
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)
    val testEvent = JSONUtil.deserialize[util.Map[String, AnyRef]](EventFixture.SAMPLE_WRONG_SENDER_EVENT)
    harness.processElement(testEvent, new java.util.Date().getTime)
    val outputs = harness.getSideOutput(eligibilityCheckConfig.enrichedOutputTag).asScala.map {
      event => event.getValue
    }
    outputs.size should be(1)

    outputs.map {
      event => {
        Console.println(event)
        event.get("mid") should be("761dfc11-1870-4981-b33d-16254a104a9d")
        event.get("action") should be("/v1/coverageeligibility/check")
        val senderCtx = event.getOrDefault("cdata", new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]].getOrDefault("sender", new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]]
        senderCtx.size() should be (0)
        event.get("cdata").asInstanceOf[util.Map[String, AnyRef]].get("recipient").asInstanceOf[util.Map[String, AnyRef]]
          .get("participantCode").asInstanceOf[String] should be("10001")
      }
    }
  }

  "ContextEnrichmentFunction" should " have wrong recipient details" in {
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)
    val testEvent = JSONUtil.deserialize[util.Map[String, AnyRef]](EventFixture.SAMPLE_WRONG_RECIPIENT_EVENT)
    harness.processElement(testEvent, new java.util.Date().getTime)
    val outputs = harness.getSideOutput(eligibilityCheckConfig.enrichedOutputTag).asScala.map {
      event => event.getValue
    }
    outputs.size should be(1)

    outputs.map {
      event => {
        Console.println(event)
        event.get("mid") should be("761dfc11-1870-4981-b33d-16254a104a9d")
        event.get("action") should be("/v1/coverageeligibility/check")
        event.get("cdata").asInstanceOf[util.Map[String, AnyRef]].get("sender").asInstanceOf[util.Map[String, AnyRef]]
          .get("participantCode").asInstanceOf[String] should be("10002")
        val recipientCtx = event.getOrDefault("cdata", new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]].getOrDefault("recipient", new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]]
        recipientCtx.size() should be (0)
      }
    }
  }

}
