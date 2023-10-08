package images.api

import images.utils.JpegFormat
import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import zio.stream.{ZPipeline, ZSink}

import java.io.File
import java.nio.file.{Files, Paths}
object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {
      case req @ Method.POST -> !! / "upload" / nodeId =>
        val imagePath = Paths.get(s"./src/images/$nodeId.jpeg")
        if (!Files.exists(imagePath.getParent))
          Files.createDirectories(imagePath.getParent)
        (for {
          path <- ZIO.attempt(Files.createFile(imagePath))
          fileSize <- req.body.asStream
            .via(JpegFormat.validate)
            .via(JpegFormat.cutMaxSize)
            .run(ZSink.count)
          _ <- JpegFormat.checkSize(fileSize)
          _ <- req.body.asStream
            .via(ZPipeline.deflate())
            .run(ZSink.fromPath(path))
        } yield ()).either
          .map {
            case Left(error) =>
              Files.deleteIfExists(imagePath)
              ZIO.logInfo(s"Error while uploading image of $nodeId: ${error.getMessage}")
              Response.status(Status.BadRequest)
            case _ => Response.ok
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
