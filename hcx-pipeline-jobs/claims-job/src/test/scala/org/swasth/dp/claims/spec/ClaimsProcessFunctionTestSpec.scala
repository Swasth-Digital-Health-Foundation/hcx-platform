package org.swasth.dp.claims.spec

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord
import org.apache.flink.streaming.util.ProcessFunctionTestHarnesses
import org.scalatest.{FlatSpec, Matchers}
import org.swasth.dp.claims.functions.ClaimsProcessFunction
import org.swasth.dp.claims.task.ClaimsConfig
import org.swasth.dp.core.job.BaseJobConfig
import org.swasth.dp.core.util.JSONUtil
import org.swasth.fixture.EventFixture

import java.util
import scala.collection.JavaConverters._

class ClaimsProcessFunctionTestSpec extends FlatSpec with Matchers{

  val config: Config = ConfigFactory.load("claims-test.conf")
  val baseJobConfig: BaseJobConfig = new BaseJobConfig(config,"coverageEligibilityCheck")
  val eligibilityCheckConfig: ClaimsConfig = new ClaimsConfig(config)

  //instantiate created user defined function
  val processFunction = new ClaimsProcessFunction(eligibilityCheckConfig)
  val objMapper = new ObjectMapper
  val gson = new Gson()

  "ClaimsProcessFunction" should "write data into audit topic as the search event does not have context " in {
    // wrap user defined function into a the corresponding operator
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)

    //StreamRecord object with the sample data
    val eventMap = gson.fromJson(EventFixture.SAMPLE_CLAIMS_SEARCH_EVENT, new util.HashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]]
    val testData: StreamRecord[util.Map[String, AnyRef]] = new StreamRecord[util.Map[String, AnyRef]](eventMap)
    harness.processElement(testData)

    harness.getSideOutput(eligibilityCheckConfig.auditOutputTag) should have size 1

    harness.close()
  }

  "ClaimsProcessFunction" should " have wrong search recipient details" in {
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)
    val testEvent = JSONUtil.deserialize[util.Map[String, AnyRef]](EventFixture.SAMPLE_INVALID_CLAIMS_SEARCH_ACTION_EVENT)
    harness.processElement(testEvent, new java.util.Date().getTime)
    val outputs = harness.getSideOutput(eligibilityCheckConfig.retryOutputTag).asScala.map {
      event => event.getValue
    }
    outputs.size should be(1)

    outputs.map {
      event => {
        Console.println("Test console event:"+JSONUtil.serialize(event))
      }
    }
    harness.close()
  }

  "ClaimsProcessFunction" should "write data into audit topic as the request event does not have context " in {
    // wrap user defined function into a the corresponding operator
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)

    //StreamRecord object with the sample data
    val eventMap = gson.fromJson(EventFixture.SAMPLE_CLAIMS_SUBMIT_EVENT, new util.HashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]]
    val testData: StreamRecord[util.Map[String, AnyRef]] = new StreamRecord[util.Map[String, AnyRef]](eventMap)
    harness.processElement(testData)

    harness.getSideOutput(eligibilityCheckConfig.auditOutputTag) should have size 1

    harness.close()
  }

  "ClaimsProcessFunction" should " have wrong request recipient details" in {
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)
    val testEvent = JSONUtil.deserialize[util.Map[String, AnyRef]](EventFixture.SAMPLE_INVALID_CLAIMS_SUBMIT_ACTION_EVENT)
    harness.processElement(testEvent, new java.util.Date().getTime)
    val outputs = harness.getSideOutput(eligibilityCheckConfig.retryOutputTag).asScala.map {
      event => event.getValue
    }
    outputs.size should be(1)

    outputs.map {
      event => {
        Console.println("Test console event:"+JSONUtil.serialize(event))
      }
    }
    harness.close()
  }

  "ClaimsProcessFunction" should "write data into audit topic as the claim search event was successful" in {
    // wrap user defined function into a the corresponding operator
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)

    //StreamRecord object with the sample data
    val eventMap = gson.fromJson(EventFixture.SAMPLE_VALID_CLAIMS_SEARCH_ACTION_EVENT, new util.HashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]]
    val testData: StreamRecord[util.Map[String, AnyRef]] = new StreamRecord[util.Map[String, AnyRef]](eventMap)
    harness.processElement(testData)

    harness.getSideOutput(eligibilityCheckConfig.auditOutputTag) should have size 1

    harness.close()
  }

  "ClaimsProcessFunction" should "write data into audit topic after successful claims request" in {
    // wrap user defined function into a the corresponding operator
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)

    //StreamRecord object with the sample data
    val eventMap = gson.fromJson(EventFixture.SAMPLE_VALID_CLAIMS_SUBMIT_ACTION_EVENT, new util.HashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]]
    val testData: StreamRecord[util.Map[String, AnyRef]] = new StreamRecord[util.Map[String, AnyRef]](eventMap)
    harness.processElement(testData)

    harness.getSideOutput(eligibilityCheckConfig.auditOutputTag) should have size 1

    harness.close()
  }

}
