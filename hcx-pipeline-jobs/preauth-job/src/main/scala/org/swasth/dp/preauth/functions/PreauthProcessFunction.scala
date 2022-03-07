package org.swasth.dp.preauth.functions

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.slf4j.LoggerFactory
import org.swasth.dp.core.function.{BaseDispatcherFunction, ValidationResult}
import org.swasth.dp.preauth.task.PreauthConfig

import java.util

class PreauthProcessFunction(config: PreauthConfig)(implicit val stringTypeInfo: TypeInformation[String])
  extends BaseDispatcherFunction(config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[PreauthProcessFunction])

  override def validate(event: util.Map[String, AnyRef]): ValidationResult = {
    // TODO: Add domain specific validations
    ValidationResult(status = true, None)
  }

}
