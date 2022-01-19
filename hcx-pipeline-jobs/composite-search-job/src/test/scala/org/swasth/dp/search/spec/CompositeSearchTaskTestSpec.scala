package org.swasth.dp.search.spec

import com.google.gson.Gson
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration
import org.apache.flink.test.util.MiniClusterWithClientResource
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.swasth.dp.BaseTestSpec
import org.swasth.dp.core.job.{BaseJobConfig, FlinkKafkaConnector}
import org.swasth.dp.core.util.PostgresConnect
import org.swasth.dp.search.task.SearchConfig

import java.util
import scala.collection.JavaConverters._

class CompositeSearchTaskTestSpec extends BaseTestSpec{

  implicit val mapTypeInfo: TypeInformation[util.Map[String, AnyRef]] = TypeExtractor.getForClass(classOf[util.Map[String, AnyRef]])

  val flinkCluster = new MiniClusterWithClientResource(new MiniClusterResourceConfiguration.Builder()
    .setNumberSlotsPerTaskManager(1)
    .setNumberTaskManagers(1)
    .build)

  var postgresConnect: PostgresConnect = _

  val config: Config = ConfigFactory.load("search-test.conf")
  val baseJobConfig: BaseJobConfig = new BaseJobConfig(config,"CompositeSearchJob")
  val searchConfig: SearchConfig = new SearchConfig(config)
  val mockKafkaUtil: FlinkKafkaConnector = mock[FlinkKafkaConnector](Mockito.withSettings().serializable())
  val gson = new Gson()

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    flinkCluster.before()
  }

  override protected def afterAll():Unit = {
    flinkCluster.after()
  }

}


