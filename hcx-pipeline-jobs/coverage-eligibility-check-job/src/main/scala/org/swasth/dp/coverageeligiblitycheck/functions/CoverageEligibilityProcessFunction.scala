package org.swasth.dp.coverageeligiblitycheck.functions

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.swasth.dp.core.function.{BaseDispatcherFunction, ValidationResult}
import org.swasth.dp.core.job.Metrics
import org.swasth.dp.core.util.{JSONUtil, PostgresConnect, PostgresConnectionConfig}
import org.swasth.dp.coverageeligiblitycheck.task.CoverageEligibilityCheckConfig

import java.util

class CoverageEligibilityProcessFunction(config: CoverageEligibilityCheckConfig, @transient var postgresConnect: PostgresConnect = null)(implicit val stringTypeInfo: TypeInformation[String])
  extends BaseDispatcherFunction(config) {

  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    if (postgresConnect == null) {
      postgresConnect = new PostgresConnect(PostgresConnectionConfig(
        user = config.postgresUser,
        password = config.postgresPassword,
        database = config.postgresDb,
        host = config.postgresHost,
        port = config.postgresPort,
        maxConnections = config.postgresMaxConnections
      ))
    }
  }

  override def close(): Unit = {
    super.close()
    postgresConnect.closeConnection()
  }

  override def validate(event: util.Map[String, AnyRef]): ValidationResult = {
    // TODO: Add domain specific validations
    ValidationResult(true, None)
  }

  override def getPayload(event: util.Map[String, AnyRef]): util.Map[String, AnyRef] = {
    val payloadRefId = getPayloadRefId(event);
    val postgresQuery = String.format("SELECT data FROM %s WHERE mid = '%s'", config.payloadTable, payloadRefId);
    val preparedStatement = postgresConnect.getConnection.prepareStatement(postgresQuery)
    try {
      val resultSet = preparedStatement.executeQuery()
      if(resultSet.next()) {
        val payload = resultSet.getString(1)
        JSONUtil.deserialize[util.Map[String, AnyRef]](payload)
      } else {
        throw new Exception("Payload not found for the given reference id")
      }
    } catch {
      case ex: Exception => throw ex
    } finally {
      preparedStatement.close()
    }

  }

  override def audit(event: util.Map[String, AnyRef], status: Boolean, context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    // TODO: Implement audit here
    val audit = new util.HashMap[String, AnyRef]();
    context.output(config.auditOutputTag, audit)
    metrics.incCounter(config.auditEventsCount)
  }
}
