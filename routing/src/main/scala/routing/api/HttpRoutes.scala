package routing.api

import routing.repository.{NodeRepository, EdgeRepository}
import routing.utils.Graph
import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import zio.http.model.Status.NotImplemented

object HttpRoutes {
  val app: HttpApp[NodeRepository with EdgeRepository, Response] =
    Http.collectZIO[Request] {
          /*
          for simplicity: parsing 2 nodes (from, to) instead of array
           */
      case req@Method.GET -> !! / "route" / "find" =>
        (for {
          from <- ZIO
            .fromOption(
              req.url.queryParams
                .get("from")
                .flatMap(_.headOption)
            )
            .tapError(_ => ZIO.logError("Provide from argument"))
          to <- ZIO
            .fromOption(
              req.url.queryParams
                .get("to")
                .flatMap(_.headOption)
            )
            .tapError(_ => ZIO.logError("Provide toId argument"))
          path <- Graph.shortest_path(from.toInt, to.toInt)
        } yield path).either.map {
          case Right(path) => {
            Response.text(path)
          }
          case Left(_) =>
            Response.status(Status.BadRequest)
        }
    }
}
