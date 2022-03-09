package org.swasth.dp.core.exception

case class PipelineException(code: String, message: String, trace: String) extends Exception(message)
