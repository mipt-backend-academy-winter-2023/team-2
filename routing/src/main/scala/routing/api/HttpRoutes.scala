package routing.api

import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import zio.http.model.Status.NotImplemented

object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {
      case req@Method.GET -> !! / "route" / "find" =>
        val response =
          for {
            ids <- ZIO.fromOption(
              req
                .url
                .queryParams
                .get("ids")
                .flatMap(_.headOption)
            ).tapError(_ => ZIO.logError("not provide ids"))
          } yield Response.text(s"finding route for $ids")
        response.orElseFail(Response.status(Status.BadRequest))
    }
}
