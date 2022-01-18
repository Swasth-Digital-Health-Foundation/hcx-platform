package org.swasth.dp.searchresponse.spec

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord
import org.apache.flink.streaming.util.ProcessFunctionTestHarnesses
import org.scalatest.{FlatSpec, Matchers}
import org.swasth.dp.core.job.BaseJobConfig
import org.swasth.dp.core.util.JSONUtil
import org.swasth.dp.searchresponse.functions.SearchResponseFunction
import org.swasth.dp.searchresponse.task.SearchResponseConfig
import org.swasth.fixture.EventFixture

import java.util
import scala.collection.JavaConverters._

class SearchResponseFunctionTestSpec extends FlatSpec with Matchers{

  val config: Config = ConfigFactory.load("search-response-test.conf")
  val baseJobConfig: BaseJobConfig = new BaseJobConfig(config,"CompositeSearch")
  val searchConfig: SearchResponseConfig = new SearchResponseConfig(config)

  //instantiate created user defined function
  val processFunction = new SearchResponseFunction(searchConfig)
  val objMapper = new ObjectMapper
  val gson = new Gson()

  "SearchResponseFunctionTestSpec" should "write data into audit topic as the event does not have context " in {
    // wrap user defined function into a the corresponding operator
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)

    //StreamRecord object with the sample data
    val eventMap = gson.fromJson(EventFixture.SAMPLE_PREAUTH_SUBMIT_EVENT, new util.HashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]]
    val testData: StreamRecord[util.Map[String, AnyRef]] = new StreamRecord[util.Map[String, AnyRef]](eventMap)
    harness.processElement(testData)

    harness.getSideOutput(searchConfig.auditOutputTag) should have size 1

    harness.close()
  }

  "SearchResponseFunctionTestSpec" should " have wrong recipient details" in {
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)
    val testEvent = JSONUtil.deserialize[util.Map[String, AnyRef]](EventFixture.SAMPLE_INVALID_PREAUTH_SUBMIT_ACTION_EVENT)
    harness.processElement(testEvent, new java.util.Date().getTime)
    val outputs = harness.getSideOutput(searchConfig.retryOutputTag).asScala.map {
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

  "SearchResponseFunctionTestSpec" should "write data into audit topic as the event does not have context " in {
    // wrap user defined function into a the corresponding operator
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)

    //StreamRecord object with the sample data
    val eventMap = gson.fromJson(EventFixture.SAMPLE_PREAUTH_SEARCH_EVENT, new util.HashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]]
    val testData: StreamRecord[util.Map[String, AnyRef]] = new StreamRecord[util.Map[String, AnyRef]](eventMap)
    harness.processElement(testData)

    harness.getSideOutput(searchConfig.auditOutputTag) should have size 1

    harness.close()
  }

  "SearchResponseFunctionTestSpec" should " have wrong recipient details" in {
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)
    val testEvent = JSONUtil.deserialize[util.Map[String, AnyRef]](EventFixture.SAMPLE_INVALID_PREAUTH_SEARCH_ACTION_EVENT)
    harness.processElement(testEvent, new java.util.Date().getTime)
    val outputs = harness.getSideOutput(searchConfig.retryOutputTag).asScala.map {
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

}
