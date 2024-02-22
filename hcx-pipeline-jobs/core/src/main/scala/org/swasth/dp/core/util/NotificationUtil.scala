package org.swasth.dp.core.util


import org.yaml.snakeyaml.Yaml

import java.io.IOException
import java.util
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.convert.ImplicitConversions.`collection asJava`
class NotificationUtil {
  var notifications: List[util.Map[String, AnyRef]] = null

  var topicCodes: util.List[String] = new util.ArrayList[String]()

  loadNotifications()

  @throws[IOException]
  private def loadNotifications(): Unit = {
    val inputStream = getClass.getResourceAsStream("/properties.yaml")
    val yaml = new Yaml()
    val networkList = yaml.load(inputStream).asInstanceOf[util.List[util.Map[String, AnyRef]]].asScala.toList
    notifications = networkList
//    notifications.foreach(map => topicCodes.add(map.get(Constants.TOPIC_CODE).asInstanceOf[String]))
    notifications.foreach { map =>
      val topicCode = map.get(Constants.TOPIC_CODE) match {
        case str: String => str
        case other => // Handle other types appropriately, such as converting to string
          other.toString
      }
      topicCodes.add(topicCode)
    }
  }

  def isValidCode(code: String): Boolean = topicCodes.contains(code)

  def getNotification(code: String): util.Map[String, AnyRef] = {
    var notification = new util.HashMap[String, AnyRef]
    val result = notifications.stream.filter((obj: util.Map[String, AnyRef]) => obj.get(Constants.TOPIC_CODE) == code).findFirst
    if (result.isPresent) notification =  result.get.asInstanceOf[util.HashMap[String, AnyRef]]
    notification
  }

}

