package config

import pureconfig.{ConfigSource, ConfigReader}
import pureconfig.generic.semiauto.deriveReader
import zio.{ULayer, ZIO, ZLayer}

object Config {
  private val basePath = "app"
  private val source = ConfigSource.default.at(basePath)

  val dbLive: ULayer[DbConfig] = {
    ZLayer.fromZIO(
      ZIO.attempt(source.loadOrThrow[ConfigImpl].dbConfig).orDie
    )
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

