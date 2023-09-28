import auth.AuthMain
import helper.HelperMain
import photo.PhotoMain
import routing.RoutingMain
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object StartApp extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    for {
      routing <- RoutingMain.run.fork
      auth <- AuthMain.run.fork
      photo <- PhotoMain.run.fork
//      helper <- HelperMain.run.fork
      _ <- routing.join
      _ <- auth.join
      _ <- photo.join
//      _ <- helper.join
    } yield ()
}
