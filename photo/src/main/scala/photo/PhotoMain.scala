package photo

import photo.api.HttpRoutes
import photo.config.ServiceConfig
import zio.http.Server
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object PhotoMain extends ZIOAppDefault {
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
