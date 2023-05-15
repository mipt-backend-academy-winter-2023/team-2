package auth.config

import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.auto.exportReader
import pureconfig.generic.semiauto.deriveReader
import zio.http.ServerConfig
import zio.sql.ConnectionPoolConfig
import zio.{ULayer, ZIO, ZLayer, http}

import java.util.Properties

object Config {
  private val basePath = "app"
  private val source = ConfigSource.default.at(basePath)

  val dbLive: ULayer[DbConfig] = {
    ZLayer.fromZIO(
      ZIO.attempt(source.loadOrThrow[ConfigImpl].dbConfig).orDie
    )
  }

  /*
  val serverLive: ULayer[ServerConfig] =
    zio.http.ServerConfig.live(
      http.ServerConfig.default.port(
        source.loadOrThrow[ConfigImpl].httpServiceConfig.port
      )
    )
   */
  val connectionPoolLive: ZLayer[DbConfig, Throwable, ConnectionPoolConfig] =
    ZLayer(
      for {
        serverConfig <- ZIO.service[DbConfig]
      } yield (ConnectionPoolConfig(
        serverConfig.url,
        connProperties(serverConfig.user, serverConfig.password)
      ))
    )

  private def connProperties(user: String, password: String): Properties = {
    val props = new Properties
    props.setProperty("user", user)
    props.setProperty("password", password)
    props
  }
}

case class ConfigImpl(
                       dbConfig: DbConfig
                       /*,httpServiceConfig: ServiceConfig*/
)

case class DbConfig(
                       url: String,
                       user: String,
                       password: String
                     )

case class ServiceConfig(host: String, port: Int)

object ServiceConfig {
  private val source = ConfigSource.default.at("app").at("auth-service-config")
  private val serviceConfig: ServiceConfig = source.loadOrThrow[ServiceConfig]

  val live: ZLayer[Any, Nothing, ServerConfig] = zio.http.ServerConfig.live {
    http
      .ServerConfig
      .default
      .binding(serviceConfig.host, serviceConfig.port)
  }
}

object ConfigImpl {
  implicit val configReader: ConfigReader[ConfigImpl] = deriveReader[ConfigImpl]
  implicit val configReaderDbConfig: ConfigReader[DbConfig] = deriveReader[DbConfig]
}
