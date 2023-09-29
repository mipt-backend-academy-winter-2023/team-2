package photo.api

import zio._
import zio.http._
import zio.stream._
//import zhttp.http._
import java.nio.file.Paths
import zio.http.model.{Method, Status}

object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collect[Request] {
      case req @ Method.POST -> !! / "add_photo" =>
        req.body
          .runCollect
          .flatMap(bytes => savePhoto(bytes))
          .foldM(
            error => Response.text(s"Error saving photo: $error"),
            _ => Response.text("Photo saved successfully!")
          )

      case _ => Response.text("Invalid route!")
  }

  private def savePhoto(photoBytes: Chunk[Byte]): ZIO[Console, Throwable, Unit] = {
    val photoPath = Paths.get("my_photos/fireball.jpg")
    val fileStream = ZStream.fromChunk(photoBytes).transduce(ZTransducer.fromOutputStream(photoPath))
    fileStream.run(Sink.drain)
  }
}

