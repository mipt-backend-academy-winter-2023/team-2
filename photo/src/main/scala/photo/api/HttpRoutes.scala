package photo.api

import io.circe.jawn.decode
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import zio.ZIO
import zio._
//import zio.console._
//import zio.nio.file._
//import zio.codec.Base64
import zio.http._
import zio.http.model.Status.{Ok, BadRequest, Forbidden}
import zio.http.model.{Method, Status}
import zio.stream.ZStream


object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {
      case req @ Method.POST -> !! / "add_photo" =>
        for {
          // Reading the photo data from the request body
          body <- req.getBodyAsString
          photoDataBase64 = body.split(",")(1) // extract base64 string
          photoData <- ZIO
            .effect(Base64.decode(photoDataBase64))
            .mapError(_ => new Exception("Failed to decode photo data"))
          // Create the folder if it doesn't exist
          _ <- Files.createDirectories(Path("my_photos"))
          // Create a unique filename for the photo
          filename = java.util.UUID.randomUUID().toString
          // Save the photo to the "my_photos" folder
          _ <- Files.write(Path(s"my_photos/$filename"), ZStream.fromIterable(photoData))
        } yield Response.ok

      case _ => ZIO.succeed(Response.notFound)
/*        (for {
          fromIdStr <- ZIO
            .fromOption(
              req.url.queryParams
                .get("fromId")
                .flatMap(_.headOption)
            )
            .tapError(_ => ZIO.logError("Provide fromId argument"))
          fromId = fromIdStr.toInt
          toIdStr <- ZIO
            .fromOption(
              req.url.queryParams
                .get("toId")
                .flatMap(_.headOption)
            )
            .tapError(_ => ZIO.logError("Provide toId argument"))
          toId <- ZIO.succeed(toIdStr.toInt)
          path <- Graph.astar(fromId, toId)
        } yield (path)).either.map {
          case Right(route) => {
            Response.text(route)
          }
          case Left(_) =>
            Response.status(Status.BadRequest)
        }*/

    }
}
