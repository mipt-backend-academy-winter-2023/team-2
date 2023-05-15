package auth

import config.Config
import api.HttpRoutes
import flyway.{FlywayAdapter}
import auth.config.ServiceConfig
import auth.repo.UserRepositoryImpl
import zio.http.Server
import zio.sql.ConnectionPool
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object AuthMain extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val server =
      for {
        flyway <- ZIO.service[FlywayAdapter.Service]
        _ <- flyway.migration
        server <- zio.http.Server.serve(HttpRoutes.app)
      } yield ()

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