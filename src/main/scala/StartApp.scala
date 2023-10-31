import auth.AuthMain
import helper.HelperMain
import routing.RoutingMain
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}
import images.ImagesMain
object StartApp extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    for {
//      routing <- RoutingMain.run.fork
//      auth <- AuthMain.run.fork
      images <- ImagesMain.run.fork
//      helper <- HelperMain.run.fork
//      _ <- routing.join
//      _ <- auth.join
      _ <- images.join
//      _ <- helper.join
    } yield ()
}
