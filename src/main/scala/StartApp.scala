import auth.AuthMain
import helper.HelperMain
import photo.PhotoMain
import routing.RoutingMain
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object StartApp extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    for {
      auth <- AuthMain.run.fork
      // helper <- HelperMain.run.fork
      photo <- PhotoMain.run.fork
      routing <- RoutingMain.run.fork
      _ <- auth.join
      // _ <- helper.join
      _ <- photo.join
      _ <- routing.join
    } yield ()
}
