package auth.api

import zio.ZIO
import zio.http._
import zio.http.model.Status.NotImplemented
import zio.http.model.Method

object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {
      case req@Method.POST -> !! / "auth" / "signup" =>
        ZIO.succeed(Response.status(NotImplemented))

      case req@Method.POST -> !! / "auth" / "signin" =>
        ZIO.succeed(Response.status(NotImplemented))
    }
}
