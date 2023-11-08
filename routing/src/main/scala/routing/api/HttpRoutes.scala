package routing.api

import routing.circuitbreaker.MyCircuitBreaker
import routing.jams.JamService
import routing.model.JamValue
import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import routing.repository.{EdgeRepository, NodeRepository}
import routing.utils.{Graph, GraphPath}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ListBuffer
import scala.util.Try

object HttpRoutes {
  val jamFallback: TrieMap[String, JamValue] = TrieMap.empty

  val app: HttpApp[
    MyCircuitBreaker with NodeRepository with EdgeRepository with JamService,
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
          jams <- ZIO.foreach(path.nodes.toList)(node =>
            MyCircuitBreaker
              .run(JamService.get(node.id))
              .tap(jam => ZIO.succeed(jamFallback.put(node.id.toString, jam)))
              .catchAll(error =>
                jamFallback.get(node.id.toString) match {
                  case Some(data) =>
                    ZIO.logInfo(s"Get data from fallback $data") *> ZIO.succeed(
                      data
                    )
                  case None =>
                    ZIO.logError(s"Get error from jams ${error.toString}") *>
                      ZIO.fail(error)
                }
              )
          )
          pathWithJams <- ZIO.fromTry(
            Try(
              GraphPath.pathToString(path.nodes.toList, path.edges.toList, jams)
            )
          )
        } yield pathWithJams).either.map {
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
