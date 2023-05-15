package auth.api

import auth.Jwt.Jwt
import auth.model.User
import io.circe.jawn.decode
import auth.model.JsonProtocol._
import auth.repo.UsersRepository
import zio.ZIO
import zio.http._
import zio.http.model.Status.{BadRequest, Created, Forbidden}
import zio.http.model.Method

object HttpRoutes {
  val app: HttpApp[UsersRepository, Response] =
    Http.collectZIO[Request] {
      case req@Method.POST -> !! / "auth" / "signup" =>
        (for {
          bodyStr <- req.body.asString
          user <- ZIO.fromEither(decode[User](bodyStr)).tapError(e => ZIO.logError(e.getMessage))
          _ <- UsersRepository.add(user)
          _ <- ZIO.logInfo(s"Created new user $user")
        } yield ()).either.map {
          case Right(_) => Response.status(Created)
          case Left(_) => Response.status(BadRequest)
        }

      case req@Method.POST -> !! / "auth" / "signin" =>
        (for {
          bodyStr <- req.body.asString
          user <- ZIO.fromEither(decode[User](bodyStr)).tapError(e => ZIO.logError(e.getMessage))
          findOutput <- UsersRepository.find(user).runCollect.map(_.toArray)
        } yield findOutput)
          .either.map  {
          case Left(_) => Response.status(BadRequest)
          case Right(found) =>
            if (found.isEmpty) {
              Response.status(Forbidden)
            } else {
              val user = found.head
              ZIO.logInfo(s"Signed in $user")
              val token = Jwt(Map(user.username -> user.password))
              Response.text(s"$token")
            }
        }
    }
}
