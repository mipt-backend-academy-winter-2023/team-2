package helper

import helper.api.HelpRoutes
import helper.config.ServiceConfig
import zio.http.Server
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object HelperMain extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
      zio.http.Server.serve(HelpRoutes.app)
        .provide(
          Server.live,
          ServiceConfig.live,
        )
  }
}