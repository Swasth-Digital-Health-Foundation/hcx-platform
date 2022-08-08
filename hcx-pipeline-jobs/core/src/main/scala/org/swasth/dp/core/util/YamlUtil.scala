package org.swasth.dp.core.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

import java.io.{IOException, InputStream}

object YamlUtil {

  private val mapper = new ObjectMapper(new YAMLFactory())

  @throws[IOException]
  def convertYaml[T](input: InputStream, clazz: Class[T]): T = mapper.readValue(input, clazz)

}
