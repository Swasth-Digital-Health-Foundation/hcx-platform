package org.swasth.dp.core.util

import io.jsonwebtoken.{Jwts, SignatureAlgorithm}
import org.swasth.dp.core.job.BaseJobConfig

import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.util.{Base64, Collections, Date, UUID}
import java.{lang, util}

class JWTUtil(config: BaseJobConfig) extends Serializable {

  def generateToken(): String = {
    val date = new Date().getTime.asInstanceOf[lang.Long]
    val payload = new util.HashMap[String, AnyRef]()
    payload.put(Constants.JTI, UUID.randomUUID())
    payload.put(Constants.ISS, config.hcxRegistryCode)
    payload.put(Constants.SUB, config.hcxRegistryCode)
    payload.put(Constants.IAT, date)
    payload.put(Constants.EXP, new Date(date + config.expiryTime).getTime.asInstanceOf[lang.Long])
    val privateKeyDecoded = Base64.getDecoder.decode(config.privateKey)
    val spec = new PKCS8EncodedKeySpec(privateKeyDecoded)
    val privateKey = KeyFactory.getInstance("RSA").generatePrivate(spec)
    Jwts.builder.setHeaderParam("typ", "JWT").setClaims(payload).signWith(SignatureAlgorithm.RS256, privateKey).compact
  }

}