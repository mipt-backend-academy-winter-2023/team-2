package routing

import routing.api.HttpRoutes
import routing.circuitbreaker.MyCircuitBreakerImpl
import routing.config.ServiceConfig
import routing.config.Config
import routing.flyway.FlywayAdapter
import routing.jams.JamServiceImpl
import routing.repository.{EdgeRepositoryImpl, NodeRepositoryImpl}
import zio.http.Server
import zio.sql.ConnectionPool
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}
import routing.utils.Graph
import sttp.client3.httpclient.zio.HttpClientZioBackend

object RoutingMain extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val server =
      for {
        flyway <- ZIO.service[FlywayAdapter.Service]
        _ <- flyway.migration
        _ <- Graph.loadGraph
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
      EdgeRepositoryImpl.live,
      JamServiceImpl.live,
      MyCircuitBreakerImpl.live,
      Scope.default,
      HttpClientZioBackend.layer()
    )
  }
}
