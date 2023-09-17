package routing.config

import pureconfig.generic.semiauto.deriveReader
import pureconfig.{ConfigReader, ConfigSource}
import zio.sql.ConnectionPoolConfig
import zio.{ULayer, ZIO, ZLayer}

import java.util.Properties

object Config {
  val basePath = "app-routing"
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
      } yield ConnectionPoolConfig(
        serverConfig.url,
        connProperties(serverConfig.user, serverConfig.password)
      )
    )

  private def connProperties(user: String, password: String): Properties = {
    val props = new Properties
    props.setProperty("user", user)
    props.setProperty("password", password)
    props
  }
}

case class DbConfig(
    url: String,
    user: String,
    password: String
)

case class ConfigImpl(dbConfig: DbConfig)

object ConfigImpl {
  implicit val configReader: ConfigReader[ConfigImpl] = deriveReader[ConfigImpl]
  implicit val configReaderDbConfig: ConfigReader[DbConfig] =
    deriveReader[DbConfig]
}
