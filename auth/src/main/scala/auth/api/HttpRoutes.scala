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
        UserRepository.findAll().runCollect.map(_.toArray).either.map {
          case Right(users) => Response.json(users.asJson.spaces2)
          case Left(e) => Response.status(InternalServerError)
        }

      case req@Method.POST -> !! / "auth" / "signin" =>
        ZIO.succeed(Response.status(NotImplemented))
//        ZIO.succeed(Response.status(NotImplemented))
    }
}
