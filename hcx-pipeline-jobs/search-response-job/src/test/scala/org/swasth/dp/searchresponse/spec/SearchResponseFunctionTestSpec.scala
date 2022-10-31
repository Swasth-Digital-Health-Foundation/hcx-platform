package org.swasth.dp.searchresponse.spec

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord
import org.apache.flink.streaming.util.ProcessFunctionTestHarnesses
import org.scalatest.{FlatSpec, Matchers}
import org.swasth.dp.core.job.BaseJobConfig
import org.swasth.dp.searchresponse.functions.SearchResponseFunction
import org.swasth.dp.searchresponse.task.SearchResponseConfig
import org.swasth.fixture.EventFixture

import java.util

class SearchResponseFunctionTestSpec extends FlatSpec with Matchers{

  val config: Config = ConfigFactory.load("search-response-test.conf")
  val baseJobConfig: BaseJobConfig = new BaseJobConfig(config,"CompositeSearch")
  val searchConfig: SearchResponseConfig = new SearchResponseConfig(config)

  //instantiate created user defined function
  val processFunction = new SearchResponseFunction(searchConfig)
  val objMapper = new ObjectMapper
  val gson = new Gson()

  "SearchResponseFunction" should "write data into audit as the on_search event was successful" in {
    // wrap user defined function into a the corresponding operator
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)

    //StreamRecord object with the sample data
    val eventMap = gson.fromJson(EventFixture.ON_SEARCH_EVENT_VALID, new util.HashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]]
    val testData: StreamRecord[util.Map[String, AnyRef]] = new StreamRecord[util.Map[String, AnyRef]](eventMap)
    harness.processElement(testData)
    harness.close()
  }

}
