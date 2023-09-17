package routing.api

import routing.graph.GraphImpl
import routing.repository.{EdgeRepository, NodeRepository}
import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}

object HttpRoutes {
  val app: HttpApp[EdgeRepository with NodeRepository, Response] =
    Http.collectZIO[Request] { case req @ Method.GET -> !! / "route" / "find" =>
      (for {
        fromId <- ZIO
          .fromOption(req.url.queryParams.get("fromId").flatMap(_.headOption))
          .tapError(_ => ZIO.logError("fromId argument is empty"))

        toId <- ZIO.fromOption(req.url.queryParams.get("toId").flatMap(_.headOption))
          .tapError(_ => ZIO.logError("toId argument is empty"))
//        path <-
      } yield
    }
}
