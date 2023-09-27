package routing

import routing.api.HttpRoutes
import routing.config.{Config, ServiceConfig}
import routing.flyway.FlywayAdapter
import routing.repository.{EdgeRepositoryImpl, NodeRepositoryImpl}
import routing.utils.Graph
import zio.http.Server
import zio.sql.ConnectionPool
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object RoutingMain extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val server =
      for {
        flyway <- ZIO.service[FlywayAdapter.Service]
        _ <- flyway.migration
        _ <- Graph.load
        server <- zio.http.Server.serve(HttpRoutes.app)
      } yield ()
    server.provide(
      Server.live,
      ServiceConfig.live,
      Config.dbLive,
      FlywayAdapter.live,
      ConnectionPool.live,
      Config.connectionPoolLive,
      NodeRepositoryImpl.live,
      EdgeRepositoryImpl.live
    )
  }
}