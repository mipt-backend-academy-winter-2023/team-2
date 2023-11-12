package routing.api

import routing.circuitbreaker.MyCircuitBreaker
import routing.jams.Jams
import routing.model.JamValue
import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import routing.repository.{EdgeRepository, NodeRepository}
import routing.utils.Graph

import scala.collection.concurrent.TrieMap
import scala.util.Try

object HttpRoutes {
  private val fallback: TrieMap[Int, JamValue] = TrieMap.empty

  val app: HttpApp[
    MyCircuitBreaker with NodeRepository with EdgeRepository with Jams,
    Response
  ] =
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
          jamValue <- MyCircuitBreaker
            .run(Jams.getJamValue(fromId))
            .tap(jamValue => ZIO.succeed(fallback.put(fromId, jamValue)))
            .catchAll(_ =>
              ZIO
                .fromOption(fallback.get(fromId))
                .orElseFail(new Exception("No fallback available"))
            )
        } yield (path, jamValue)).either.map {
          case Right((route, jamValue)) =>
            Response.text(s"route: $route, jamValue: $jamValue")
          case Left(_) =>
            Response.status(Status.BadRequest)
        }
      case req @ Method.GET -> !! / "debug_graph" =>
        for {
          graph <- ZIO.succeed(Graph.toString)
        } yield Response.text(graph)
    }
}
