package org.swasth.dp.core.util

import java.io.IOException
import java.util

class NotificationUtil {

  var notifications: util.List[util.Map[String, AnyRef]] = null

  var topicCodes: util.List[String] = new util.ArrayList[String]()

  loadNotifications()

  @throws[IOException]
  private def loadNotifications(): Unit = {
    notifications = YamlUtil.convertYaml(getClass.getClassLoader.getResourceAsStream("notifications.yaml"), classOf[util.List[util.Map[String, AnyRef]]])
    notifications.forEach((obj: util.Map[String, AnyRef]) => topicCodes.add(obj.get(Constants.TOPIC_CODE).asInstanceOf[String]))

  }

  def isValidCode(code: String): Boolean = topicCodes.contains(code)

  def getNotification(code: String): util.Map[String, AnyRef] = {
    var notification = new util.HashMap[String, AnyRef]
    val result = notifications.stream.filter((obj: util.Map[String, AnyRef]) => obj.get(Constants.TOPIC_CODE) == code).findFirst
    if (result.isPresent) notification =  result.get.asInstanceOf[util.HashMap[String, AnyRef]]
    notification
  }

}

