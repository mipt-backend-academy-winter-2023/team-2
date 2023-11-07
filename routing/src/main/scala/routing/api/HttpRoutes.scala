package routing.api

import routing.jams.JamService
import routing.model.JamValue
import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import zio.http.model.Status.NotImplemented
import routing.repository.{EdgeRepository, NodeRepository}
import routing.utils.Graph
import zio.http.SSLConfig.Data

import scala.collection.concurrent.TrieMap
import scala.util.Try

object HttpRoutes {
  val jamFallback: TrieMap[String, JamValue] = TrieMap.empty

  val app: HttpApp[NodeRepository with EdgeRepository with JamService, Response] =
    Http.collectZIO[Request] {
      case req @ Method.GET -> !! / "route" / "find" =>
        (for {
          fromIdStr <- ZIO
            .fromOption(
              req.url.queryParams
                .get("fromId")
                .flatMap(_.headOption)
            )
            .tapError(_ => ZIO.logError("Provide fromId argument"))
          fromId <- ZIO.fromTry(Try(fromIdStr.toInt))
          toIdStr <- ZIO
            .fromOption(
              req.url.queryParams
                .get("toId")
                .flatMap(_.headOption)
            )
            .tapError(_ => ZIO.logError("Provide toId argument"))
          toId <- ZIO.fromTry(Try(toIdStr.toInt))
          path <- Graph.astar(fromId, toId)
        } yield (path)).either.map {
          case Right(route) => {
            Response.text(route)
          }
          case Left(_) =>
            Response.status(Status.BadRequest)
        }
      case req @ Method.GET -> !! / "debug_graph" =>
        for {
          graph <- ZIO.succeed(Graph.toString)
        } yield Response.text(graph)
    }
}
