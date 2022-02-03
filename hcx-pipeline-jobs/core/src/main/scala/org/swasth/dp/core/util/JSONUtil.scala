package org.swasth.dp.core.util

import java.lang.reflect.{ParameterizedType, Type}

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.JsonGenerator.Feature
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import scala.collection.JavaConverters._
import com.fasterxml.jackson.core.`type`.TypeReference
import java.util
import java.util.Base64

object JSONUtil {

  @transient val mapper = new ObjectMapper()
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
  mapper.configure(Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
  mapper.setSerializationInclusion(Include.NON_NULL)

  @throws(classOf[Exception])
  def serialize(obj: AnyRef): String = {
    mapper.writeValueAsString(obj)
  }

  def deserialize[T: Manifest](json: String): T = {
    mapper.readValue(json, typeReference[T])
  }

  def deserialize[T: Manifest](json: Array[Byte]): T = {
    mapper.readValue(json, typeReference[T])
  }

  private[this] def typeReference[T: Manifest] = new TypeReference[T] {
    override def getType = typeFromManifest(manifest[T])
  }


  private[this] def typeFromManifest(m: Manifest[_]): Type = {
    if (m.typeArguments.isEmpty) { m.runtimeClass }
    else new ParameterizedType {
      def getRawType = m.runtimeClass
      def getActualTypeArguments = m.typeArguments.map(typeFromManifest).toArray
      def getOwnerType = null
    }
  }

  @throws[Exception]
  def encodeBase64Object(decodedObj: AnyRef): String = {
    val encodedString = Base64.getEncoder.encodeToString(serialize(decodedObj).getBytes)
    encodedString
  }

  @throws[Exception]
  def decodeBase64String[T](encodedString: String,clazz: Class[T]): T = {
    val decodedBytes = Base64.getDecoder.decode(encodedString)
    val decodedString = new String(decodedBytes)
    deserialize(decodedString,clazz)
  }

  def deserialize[T](json: String,clazz: Class[T]): T = {
    mapper.readValue(json, clazz);
  }

  @throws[Exception]
  def parsePayload(encodedPayload: String): util.HashMap[String, AnyRef] = {
    val strArray = encodedPayload.split("\\.")
    if (strArray.length > 0 && strArray.length == Constants.PAYLOAD_LENGTH) {
      val event = new util.HashMap[String, AnyRef]
      event.put(Constants.PROTECTED, strArray(0))
      event.put(Constants.ENCRYPTED_KEY, strArray(1))
      event.put(Constants.IV, strArray(2))
      event.put(Constants.CIPHERTEXT, strArray(3))
      event.put(Constants.TAG, strArray(4))
      event
    }
    else throw new Exception("payload is not complete")
  }

  def createPayloadByValues(payload: util.Map[String, AnyRef]): String = {
    var encodedString = ""
    for (objValue <- payload.asScala.values) {
      encodedString = encodedString + objValue + "."
    }
    //Remove the last . after adding all the values
    encodedString = encodedString.substring(0, encodedString.length - 1)
    encodedString
  }

}
