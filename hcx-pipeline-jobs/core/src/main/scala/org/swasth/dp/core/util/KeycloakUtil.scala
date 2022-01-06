package org.swasth.dp.core.util

import com.typesafe.config.{Config, ConfigFactory}
import org.keycloak.admin.client.Keycloak
import org.swasth.dp.core.job.BaseJobConfig

object KeycloakUtil {

  val config: Config = ConfigFactory.load("base-config.conf")
  val baseConfig: BaseJobConfig = new BaseJobConfig(config, "base-job")

  def getToken() : String = {
    var accessToken : String = null
    try {
      val instance = Keycloak.getInstance(baseConfig.keycloakUrl, baseConfig.keycloakRealm, baseConfig.keycloakUsername, baseConfig.keycloakPassword, baseConfig.keycloakClientId)
      val manager = instance.tokenManager
      accessToken = manager.getAccessTokenString
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
    }
    accessToken
  }

}
