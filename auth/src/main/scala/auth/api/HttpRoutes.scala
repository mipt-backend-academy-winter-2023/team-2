package auth.api

import auth.repository.UserRepository
import auth.model.JsonProtocol.userEncoder
import io.circe.syntax.EncoderOps
import zio.ZIO
import zio.http._
import zio.http.model.Status.{InternalServerError, NotImplemented}
import zio.http.model.Method

object HttpRoutes {
  val app: HttpApp[UserRepository, Response] =
    Http.collectZIO[Request] {
      case req@Method.POST -> !! / "auth" / "signup" =>
        ZIO.succeed(Response.status(NotImplemented))

      case req@Method.POST -> !! / "auth" / "signin" =>
        UserRepository.findAll().runCollect.map(_.toArray).either.map {
          case Right(users) => Response.json(users.asJson.spaces2)
          case Left(e) => Response.status(InternalServerError)
        }

//        ZIO.succeed(Response.status(NotImplemented))
    }
}
