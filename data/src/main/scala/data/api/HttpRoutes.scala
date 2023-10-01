package data.api

import io.circe.jawn.decode
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import zio.ZIO
import zio.http._
import zio.http.html._
import zio.http.model.Status.{BadRequest, Forbidden, Ok}
import zio.http.model.{Method, Status}

object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {
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
              html(
                body(
                  p(s"Photo of $id"),
                  img(s"tmp/data_$id")
                )
              )
            Response.html(picture)
          }
          case Left(_) => Response.status(Status.BadRequest)
        }
    }
}

