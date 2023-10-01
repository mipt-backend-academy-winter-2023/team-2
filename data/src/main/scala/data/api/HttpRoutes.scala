package data.api

import io.circe.jawn.decode
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import zio.ZIO
import zio.http._
import zio.http.html._
import zio.http.model.Status.{BadRequest, Forbidden, Ok}
import zio.http.model.{Method, Status}

import java.io.PrintWriter
import java.nio.file.{Files, Paths}

object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {
      case request@Method.PUT -> !! / "upload" =>
        (for {
          id <- ZIO
            .fromOption(
              request.url.queryParams
                .get("id")
                .flatMap(_.headOption)
            )
            .tapError(_ => ZIO.logError("Provide id argument"))
          data <- request.body.asString
          path = Paths.get(s"tmp/photo_$id")
          _ <- ZIO.attempt(Files.createFile(path))
          _ <- ZIO.attempt(Files.writeString(path, data))
        } yield id).either.map {
          case Right(id) => Response.text(s"Uploaded photo for $id")
          case Left(_) => Response.status(Status.BadRequest)
        }
      case req@Method.GET -> !! / "download"  =>
        (for {
          id <- ZIO
            .fromOption(
              req.url.queryParams
                .get("id")
                .flatMap(_.headOption)
            )
            .tapError(_ => ZIO.logError("Provide id argument"))
        } yield id).either.map {
          case Right(id) => {
            val picture: Html =
                body(
                  p(s"Photo of $id"),
                  img(s"tmp/data_$id")
                )
            Response.html(picture)
          }
          case Left(_) => Response.status(Status.BadRequest)
        }
    }
}

