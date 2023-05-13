package auth

import api.HttpRoutes
import auth.config.ServiceConfig
import repository.UserRepositoryImpl
import config.Config
import flyway.FlywayAdapter
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, http}
import zio.http.Server
import zio.sql.ConnectionPool

object AuthMain extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val server =
      for {
        flyway <- ZIO.service[FlywayAdapter.Service]
        _ <- flyway.migration
        server <- zio.http.Server.serve(HttpRoutes.app)
      } yield()
    server.provide(
      Server.live,
      ServiceConfig.live,
      Config.dbLive,
      FlywayAdapter.live,
      Config.connectionPoolLive,
      ConnectionPool.live,
      UserRepositoryImpl.live
    )
  }
}
