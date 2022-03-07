package org.swasth.dp.claims.functions

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.slf4j.LoggerFactory
import org.swasth.dp.claims.task.ClaimsConfig
import org.swasth.dp.core.function.{BaseDispatcherFunction, ValidationResult}

import java.util

class ClaimsProcessFunction(config: ClaimsConfig)(implicit val stringTypeInfo: TypeInformation[String])
  extends BaseDispatcherFunction(config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[ClaimsProcessFunction])

  override def validate(event: util.Map[String, AnyRef]): ValidationResult = {
    // TODO: Add domain specific validations
    ValidationResult(true, None)
  }

}
