package images

import images.api.HttpRoutes
import images.config.ServiceConfig
import zio.http.Server
import zio.sql.ConnectionPool
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object ImagesMain extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val server =
      for {
        server <- zio.http.Server
          .serve(HttpRoutes.app)
      } yield ()

    server.provide(
      Server.live,
      ServiceConfig.live
    )
  }
}
