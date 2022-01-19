package org.swasth.dp.search.spec

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.api.scala.createTypeInformation
import org.scalatest.{FlatSpec, Matchers}
import org.swasth.dp.core.job.BaseJobConfig
import org.swasth.dp.search.functions.CompositeSearchFunction
import org.swasth.dp.search.task.SearchConfig

class CompositeSearchFunctionTestSpec extends FlatSpec with Matchers{

  val config: Config = ConfigFactory.load("search-test.conf")
  val baseJobConfig: BaseJobConfig = new BaseJobConfig(config,"CompositeSearch")
  val searchConfig: SearchConfig = new SearchConfig(config)

  //instantiate created user defined function
  val processFunction = new CompositeSearchFunction(searchConfig)
  val objMapper = new ObjectMapper
  val gson = new Gson()


}
