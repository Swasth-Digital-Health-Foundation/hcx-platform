package org.swasth.dp.coverageeligiblitycheck.functions

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.slf4j.LoggerFactory
import org.swasth.dp.core.function.{BaseDispatcherFunction, ValidationResult}
import org.swasth.dp.coverageeligiblitycheck.task.CoverageEligibilityCheckConfig

import java.util

class CoverageEligibilityProcessFunction(config: CoverageEligibilityCheckConfig)(implicit val stringTypeInfo: TypeInformation[String])
  extends BaseDispatcherFunction(config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[CoverageEligibilityProcessFunction])

  override def validate(event: util.Map[String, AnyRef]): ValidationResult = {
    // TODO: Add domain specific validations
    ValidationResult(true, None)
  }

}
