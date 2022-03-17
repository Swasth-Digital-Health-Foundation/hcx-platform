package org.swasth.dp.payments.spec

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord
import org.apache.flink.streaming.util.ProcessFunctionTestHarnesses
import org.scalatest.{FlatSpec, Matchers}
import org.swasth.dp.core.job.BaseJobConfig
import org.swasth.dp.core.util.JSONUtil
import org.swasth.dp.payments.functions.PaymentsProcessFunction
import org.swasth.dp.payments.task.PaymentsConfig
import org.swasth.fixture.EventFixture

import java.util

class PaymentsProcessFunctionTestSpec extends FlatSpec with Matchers{

  val config: Config = ConfigFactory.load("payments-test.conf")
  val baseJobConfig: BaseJobConfig = new BaseJobConfig(config,"coverageEligibilityCheck")
  val eligibilityCheckConfig: PaymentsConfig = new PaymentsConfig(config)

  //instantiate created user defined function
  val processFunction = new PaymentsProcessFunction(eligibilityCheckConfig)
  val objMapper = new ObjectMapper
  val gson = new Gson()

  "PaymentsProcessFunction" should "write data into audit as the search event does not have context " in {
    // wrap user defined function into a the corresponding operator
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)

    //StreamRecord object with the sample data
    val eventMap = gson.fromJson(EventFixture.SAMPLE_PAYMENTS_SEARCH_EVENT, new util.HashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]]
    val testData: StreamRecord[util.Map[String, AnyRef]] = new StreamRecord[util.Map[String, AnyRef]](eventMap)
    harness.processElement(testData)
    harness.close()
  }

  "PaymentsProcessFunction" should " have wrong search recipient details" in {
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)
    val testEvent = JSONUtil.deserialize[util.Map[String, AnyRef]](EventFixture.SAMPLE_INVALID_PAYMENTS_SEARCH_ACTION_EVENT)
    harness.processElement(testEvent, new java.util.Date().getTime)
    harness.close()
  }

  "PaymentsProcessFunction" should "write data into audit as the request event does not have context " in {
    // wrap user defined function into a the corresponding operator
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)

    //StreamRecord object with the sample data
    val eventMap = gson.fromJson(EventFixture.SAMPLE_PAYMENTS_REQUEST_EVENT, new util.HashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]]
    val testData: StreamRecord[util.Map[String, AnyRef]] = new StreamRecord[util.Map[String, AnyRef]](eventMap)
    harness.processElement(testData)
    harness.close()
  }

  "PaymentsProcessFunction" should " have wrong request recipient details" in {
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)
    val testEvent = JSONUtil.deserialize[util.Map[String, AnyRef]](EventFixture.SAMPLE_INVALID_PAYMENTS_REQUEST_ACTION_EVENT)
    harness.processElement(testEvent, new java.util.Date().getTime)
    harness.close()
  }

  "PaymentsProcessFunction" should "write data into audit as the search event was successful" in {
    // wrap user defined function into a the corresponding operator
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)

    //StreamRecord object with the sample data
    val eventMap = gson.fromJson(EventFixture.SAMPLE_VALID_PAYMENTS_SEARCH_ACTION_EVENT, new util.HashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]]
    val testData: StreamRecord[util.Map[String, AnyRef]] = new StreamRecord[util.Map[String, AnyRef]](eventMap)
    harness.processElement(testData)
    harness.close()
  }

  "PaymentsProcessFunction" should "write data into audit after successful payments request" in {
    // wrap user defined function into a the corresponding operator
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)

    //StreamRecord object with the sample data
    val eventMap = gson.fromJson(EventFixture.SAMPLE_VALID_PAYMENTS_REQUEST_ACTION_EVENT, new util.HashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]]
    val testData: StreamRecord[util.Map[String, AnyRef]] = new StreamRecord[util.Map[String, AnyRef]](eventMap)
    harness.processElement(testData)
    harness.close()
  }

}
