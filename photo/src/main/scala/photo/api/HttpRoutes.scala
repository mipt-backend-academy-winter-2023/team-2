package photo.api

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, Path}

import zio._
import zio.http._
import zio.http.model.{Method, Status, Header, Headers}
import zio.stream.{ZSink, ZStream, ZPipeline}

object HttpRoutes {
  val app: HttpApp[Any, Nothing] =
    Http.collectZIO[Request] {
      case request @ Method.PUT -> !! / "upload" =>
        (for {
          nodeIdStr <- ZIO
            .fromOption(
              request.url.queryParams
                .get("nodeId")
                .flatMap(_.headOption)
            )
            .tapError(_ => ZIO.logError("Provide nodeId argument"))
          _ <- ZIO
            .attempt(Files.createFile(Paths.get(s"/uploaded$nodeIdStr")))
            .either
            .map { case _ => null }
          path = Paths.get(s"/uploaded$nodeIdStr")
          _ <- request.body.asStream
            .via(ZPipeline.deflate())
            .run(ZSink.fromPath(path))
        } yield (nodeIdStr)).either.map {
          case Left(e)          => Response.status(Status.BadRequest)
          case Right(nodeIdStr) => Response.text(nodeIdStr)
        }

      case request @ Method.GET -> !! / "download" =>
        (for {
          nodeIdStr <- ZIO
            .fromOption(
              request.url.queryParams
                .get("nodeId")
                .flatMap(_.headOption)
            )
            .tapError(_ => ZIO.logError("Provide nodeId argument"))
        } yield (nodeIdStr)).either.map {
          case Left(e) => Response.status(Status.BadRequest)
          case Right(nodeIdStr) =>
            Response(
              body = Body.fromStream(
                ZStream
                  .fromPath(Paths.get(s"/uploaded$nodeIdStr"))
                  .via(ZPipeline.inflate())
              ),
              headers = Headers
                .apply("Access-Control-Allow-Origin", "http://localhost")
                .combine(
                  Headers.apply("Access-Control-Allow-Credentials", "true")
                  .combine(
                    Headers.apply("Access-Control-Allow-Methods", "GET")
                )
            )
        }
    }
}