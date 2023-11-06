package routing.api

import nl.vroste.rezilience.CircuitBreaker.{CircuitBreakerOpen, WrappedError}
import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import zio.http.model.Status.NotImplemented
import routing.repository.{EdgeRepository, NodeRepository}
import routing.utils.Graph
import integrations.jams.{JamValue, JamsIntegration}
import circuitbreaker.MyCircuitBreaker

import scala.collection.concurrent.TrieMap
import scala.util.Try

object HttpRoutes {
  val fallbackJam: TrieMap[Int, JamValue] = TrieMap.empty

  val app: HttpApp[NodeRepository with EdgeRepository with JamsIntegration with MyCircuitBreaker, Response] =
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
          jam <-
            MyCircuitBreaker
              .run(JamsIntegration.getJam(fromId))
              .tap(jam => ZIO.succeed(fallbackJam.put(fromId, jam)))
              .catchAll {
                case CircuitBreakerOpen =>
                  val data = fallbackJam.get(fromId)
                  ZIO.logInfo(s"Get data from fallback $data") *> ZIO.fromOption(data)
                case WrappedError(error) =>
                  ZIO.logError(s"Get error from jams ${error.toString}") *>
                    ZIO.fail(error)
              }
        } yield (path, jam)).either.map {
          case Right((route, jamValue)) => {
            Response.text(s"route: ${route}, jamValue: ${jamValue}")
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
