package org.swasth.dp.payments.functions

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.slf4j.LoggerFactory
import org.swasth.dp.core.function.{BaseDispatcherFunction, ValidationResult}
import org.swasth.dp.payments.task.PaymentsConfig

import java.util

class PaymentsProcessFunction(config: PaymentsConfig)(implicit val stringTypeInfo: TypeInformation[String])
  extends BaseDispatcherFunction(config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[PaymentsProcessFunction])

  override def validate(event: util.Map[String, AnyRef]): ValidationResult = {
    // TODO: Add domain specific validations
    ValidationResult(status = true, None)
  }

}
