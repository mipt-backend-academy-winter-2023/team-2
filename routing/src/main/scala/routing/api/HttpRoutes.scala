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
            ).tapError(_ => ZIO.logError("Provide fromId argument"))
            fromId <- ZIO.succeed(fromIdStr.toInt)
            toIdStr <- ZIO.fromOption(
              req
                .url
                .queryParams
                .get("toId")
                .flatMap(_.headOption)
            ).tapError(_ => ZIO.logError("Provide toId argument"))
            toId <- ZIO.succeed(toIdStr.toInt)
            path <- ZIO.succeed(Graph.astar(fromId, toId))
          } yield Response.text(s"Route from $fromId to $toId: $path")
        response.orElseFail(Response.status(Status.BadRequest))
      case req@Method.GET -> !! / "debug_graph" =>
        val response =
          for {
            graph <- ZIO.succeed(Graph.debug_graph)
          } yield Response.text(s"graph for debug: $graph")
        response
    }
}
