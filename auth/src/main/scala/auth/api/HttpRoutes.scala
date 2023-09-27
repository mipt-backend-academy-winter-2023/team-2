package auth.api

import auth.utils.JwtUtils
import io.circe.jawn.decode
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import auth.model.User
import auth.repository.UserRepository
import zio.ZIO
import zio.http._
import zio.http.model.Status.{Ok, BadRequest, Forbidden}
import zio.http.model.{Method, Status}

object HttpRoutes {
  val app: HttpApp[UserRepository, Response] =
    Http.collectZIO[Request] {
      case req @ Method.POST -> !! / "auth" / "signup" =>
        (for {
          bodyStr <- req.body.asString
          user <- ZIO
            .fromEither(decode[User](bodyStr))
            .tapError(e => ZIO.logError(e.getMessage))
          _ <- UserRepository.add(user)
          _ <- ZIO.logInfo(s"Created new user $user")
        } yield ()).either.map {
          case Left(_)  => Response.status(Status.BadRequest)
          case Right(_) => Response.status(Status.Ok)
        }

      case req @ Method.POST -> !! / "auth" / "signin" =>
        (for {
          bodyStr <- req.body.asString
          user <- ZIO
            .fromEither(decode[User](bodyStr))
            .tapError(e => ZIO.logError(e.getMessage))
          foundUser <- UserRepository.findUser(user).runCollect.map(_.toArray)
        } yield (foundUser)).either.map {
          case Right(users) =>
            users match {
              case Array() => Response.status(Status.Forbidden)
              case users =>
                val user = users.head
                ZIO.logInfo(s"User $user signed in")
                Response.text(
                  s"{'token': '${JwtUtils.createToken(user.username)}'}"
                )
            }
          case Left(_) => Response.status(BadRequest)
        }
    }
}
