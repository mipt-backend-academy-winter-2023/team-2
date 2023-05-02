package auth.config

import pureconfig.ConfigSource
import pureconfig.generic.auto.exportReader
import zio.http.ServerConfig
import zio.{ZLayer, http}

case class ServiceConfig(host: String, port: Int)

object ServiceConfig {
  private val source = ConfigSource.default.at("app").at("auth-service-config")
  private val serviceConfig: ServiceConfig = source.loadOrThrow[ServiceConfig]

  println("auth", serviceConfig.port)
  val live: ZLayer[Any, Nothing, ServerConfig] = zio.http.ServerConfig.live {
    http
      .ServerConfig
      .default
      .binding(serviceConfig.host, serviceConfig.port)
  }
}
