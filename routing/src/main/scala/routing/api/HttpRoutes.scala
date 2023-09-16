package routing.api

import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import zio.http.model.Status.NotImplemented

import routing.utils.Graph

object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {
      case req@Method.GET -> !! / "route" / "find" =>
        val response =
          for {
            fromIdStr <- ZIO.fromOption(
              req
                .url
                .queryParams
                .get("fromId")
                .flatMap(_.headOption)
            ).tapError(_ => ZIO.logError("not provide ids"))
            fromId <- ZIO.succeed(fromIdStr.toInt + 2)
            data <- ZIO.succeed(Graph.get)
          } yield Response.text(s"finding route for $fromId; and $data")
        response.orElseFail(Response.status(Status.BadRequest))
    }
}
