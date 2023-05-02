import helper.HelperMain
import auth.AuthMain
import routing.RoutingMain
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object StartApp extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    for {
      //_ <- HelperMain.run
      //_ <- RoutingMain.run
      _ <- AuthMain.run
      //_ <- HelperMain.run
    } yield ()
}
