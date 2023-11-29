package images.api

import images.utils.JpegValidation
import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import zio.http.model.Status.NotImplemented
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.stream.{ZPipeline, ZSink}

import java.io.File
import java.nio.file.{Files, Paths}

object HttpRoutes {
  val app: HttpApp[Producer, Response] =
    Http.collectZIO[Request] {
      case req @ Method.POST -> !! / "upload" / nodeId =>
        val imagePath = Paths.get(s"./src/images/$nodeId.jpeg")
        if (!Files.exists(imagePath.getParent))
          Files.createDirectories(imagePath.getParent)
        (for {
          path <- ZIO.attempt(Files.createFile(imagePath))
          _ <- req.body.asStream
            .via(JpegValidation.pipeline)
            .run(ZSink.drain)
          _ <- req.body.asStream
            .run(ZSink.fromPath(path))
          _ <- Producer.produce[Any, Long, String](
            topic = "images",
            key = nodeId.toLong,
            value = imagePath.toString,
            keySerializer = Serde.long,
            valueSerializer = Serde.string
          )
        } yield ()).either.map {
          case Right(_) => Response.ok
          case _ =>
            Files.deleteIfExists(imagePath)
            ZIO.logInfo(s"Uploading image $nodeId went wrong")
            Response.status(Status.BadRequest)
        }

      case req @ Method.GET -> !! / "download" / nodeId =>
        val imagePath = Paths.get(s"./src/images/$nodeId.jpeg")
        if (Files.exists(imagePath)) {
          ZIO.succeed(Response.status(Status.NotFound))
          ZIO.succeed(
            Response(
              status = Status.Ok,
              body = Body.fromFile(new File(imagePath.toAbsolutePath.toString))
            )
          )
        } else {
          ZIO.succeed(Response.status(Status.NotFound))
        }
    }
}
