package routing.api

import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import zio.http.model.Status.NotImplemented

import routing.repository.{NodeRepository, EdgeRepository}
import routing.utils.Graph


object HttpRoutes {
  val app: HttpApp[NodeRepository with EdgeRepository, Response] =
    Http.collectZIO[Request] {
      case req@Method.GET -> !! / "route" / "find" =>
        (for {
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
          nodes <- NodeRepository.findAllNodes.runCollect.map(_.toArray)
          edges <- EdgeRepository.findAllEdges.runCollect.map(_.toArray)
          _ <- Graph.initGraph(nodes, edges)
          path <- Graph.astar(toId, fromId)
        } yield (path)).either.map {
          case Right(foundPath) => {
            Response.text(s"Route $foundPath")
          }
          case Left(_) =>
            Response.text("bad request")
        }
      case req@Method.GET -> !! / "debug_graph" =>
        val response =
          for {
            graph <- ZIO.succeed(Graph.debug_graph)
          } yield Response.text(s"graph for debug: $graph")
        response
    }
}
