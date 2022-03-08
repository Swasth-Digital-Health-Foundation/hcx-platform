package org.swasth.dp.coverageeligibilitycheck.spec

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord
import org.apache.flink.streaming.util.ProcessFunctionTestHarnesses
import org.scalatest.{FlatSpec, Matchers}
import org.swasth.dp.core.job.BaseJobConfig
import org.swasth.dp.core.util.JSONUtil
import org.swasth.dp.coverageeligiblitycheck.functions.CoverageEligibilityProcessFunction
import org.swasth.dp.coverageeligiblitycheck.task.CoverageEligibilityCheckConfig
import org.swasth.fixture.EventFixture

import java.util

class CoverageEligibilityProcessFunctionTest extends FlatSpec with Matchers{

  val config: Config = ConfigFactory.load("coverage-eligibility-check.conf")
  val baseJobConfig: BaseJobConfig = new BaseJobConfig(config,"coverageEligibilityCheck")
  val eligibilityCheckConfig: CoverageEligibilityCheckConfig = new CoverageEligibilityCheckConfig(config)

  //instantiate created user defined function
  val processFunction = new CoverageEligibilityProcessFunction(eligibilityCheckConfig)
  val objMapper = new ObjectMapper
  val gson = new Gson()

  "CoverageEligibilityProcessFunction" should "write data into audit topic as the event does not have context " in {
    // wrap user defined function into a the corresponding operator
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)

    //StreamRecord object with the sample data
    val eventMap = gson.fromJson(EventFixture.SAMPLE_EVENT, new util.HashMap[String, AnyRef]().getClass).asInstanceOf[util.Map[String, AnyRef]]
    val testData: StreamRecord[util.Map[String, AnyRef]] = new StreamRecord[util.Map[String, AnyRef]](eventMap)
    harness.processElement(testData)

    harness.getSideOutput(eligibilityCheckConfig.auditOutputTag) should have size 1

    harness.close()
  }

  "CoverageEligibilityProcessFunction" should " have wrong recipient details" in {
    val harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction)
    val testEvent = JSONUtil.deserialize[util.Map[String, AnyRef]](EventFixture.SAMPLE_INVALID_ACTION_EVENT)
    harness.processElement(testEvent, new java.util.Date().getTime)
  }

}
