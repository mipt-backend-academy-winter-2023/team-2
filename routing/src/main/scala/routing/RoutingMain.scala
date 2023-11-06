package routing

import routing.api.HttpRoutes
import routing.config.ServiceConfig
import routing.config.Config
import routing.flyway.FlywayAdapter
import routing.repository.{NodeRepositoryImpl, EdgeRepositoryImpl}
import routing.utils.Graph
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.http.Server
import zio.sql.ConnectionPool
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

import integrations.jams.JamsIntegrationImpl
import circuitbreaker.MyCircuitBreakerImpl

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
      JamsIntegrationImpl.live,
      MyCircuitBreakerImpl.live,
      Scope.default,
      HttpClientZioBackend.layer()
    )
  }
}
