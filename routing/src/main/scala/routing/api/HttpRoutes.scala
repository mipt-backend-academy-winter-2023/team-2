package routing.api

import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import zio.http.model.Status.NotImplemented

import routing.model.Node
import routing.repository.NodeRepository
import routing.utils.Graph


object HttpRoutes {
  val app: HttpApp[Any, Response] =
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
          _ <- ZIO.logInfo(s"HTTPROUTES - toid $toId fromId $fromId")
          //path <- ZIO.succeed(Graph.astar(fromId, toId))
          path <- NodeRepository.findAllNodes.runCollect.map(_.toArray)
        } yield(path)).either.map {
          case Right(foundPath) =>
            1//Response.text(s"Route $foundPath")
          case Left(_) =>
            2//Response.text("bad request")
        }
      case req@Method.GET -> !! / "debug_graph" =>
        val response =
          for {
            graph <- ZIO.succeed(Graph.debug_graph)
          } yield Response.text(s"graph for debug: $graph")
        response
    }
}
