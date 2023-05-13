package auth.config

import pureconfig.{ConfigSource, ConfigReader}
import pureconfig.generic.semiauto.deriveReader
import zio.{ULayer, ZIO, ZLayer}
import zio.sql.ConnectionPoolConfig

import java.util.Properties // ???

object Config {
  private val basePath = "app"
  private val source = ConfigSource.default.at(basePath)

  val dbLive: ULayer[DbConfig] = {
    ZLayer.fromZIO(
      ZIO.attempt(source.loadOrThrow[ConfigImpl].dbConfig).orDie
    )
  }

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
                       dbConfig: DbConfig,
                       //httpServiceConfig: HttpServerConfig
                     )
case class DbConfig(
                     url: String,
                     user: String,
                     password: String
                   )
//case class HttpServerConfigImpl(
//                       dbConfig: DbConfig,
//                       httpServiceConfig: HttpServerConfig
//                     )

object ConfigImpl {
  implicit val configReader: ConfigReader[ConfigImpl] = deriveReader[ConfigImpl]
  //implicit val configReaderHttpServerConfig: ConfigReader[HttpServerConfig] =
  //  deriveReader[HttpServerConfig]
  implicit val configReaderDbConfig: ConfigReader[DbConfig] =
    deriveReader[DbConfig]
}

