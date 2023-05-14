package auth.api

import auth.repository.UserRepository
import auth.model.JsonProtocol._
import auth.model.User
import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import java.time.Clock
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import zio.CanFail.canFailAmbiguous1
import zio._
import zio.http._
import zio.http.model._
import zio.http.model.Status.{BadRequest, Created, Forbidden, Ok}

object JwtUtils {
  private val secretKey = "secret-key"
  def createToken(username: String): String = {
    val claim = JwtClaim(
      expiration = Some(3600),
      issuedAt = Some(Clock.systemUTC().instant().getEpochSecond),
      subject = Some(username)
    )
    Jwt.encode(claim, secretKey, JwtAlgorithm.HS256)
  }
}

object HttpRoutes {
  val app: HttpApp[UserRepository, Response] =
    Http.collectZIO[Request] {
      case req@Method.POST -> !! / "auth" / "signup" =>
        (for {
          bodyStr <- req.body.asString
          user <- ZIO.fromEither(decode[User](bodyStr)).tapError(e => ZIO.logError(e.getMessage))
          _ <- UserRepository.add(user)
          _ <- ZIO.logInfo(s"Created new user $user")
        } yield ()).either.map {
          case Right(_) => Response.status(Created)
          case Left(_) => Response.status(BadRequest)
        }
//        ZIO.succeed(Response.status(NotImplemented))

      case req@Method.POST -> !! / "auth" / "signin" =>
        (for {
          bodyStr <- req.body.asString
          user <- ZIO.fromEither(decode[User](bodyStr)).tapError(e => ZIO.logError(e.getMessage))
          allFound <- UserRepository.findByCredentials(user).runCollect.map(_.toArray)
        } yield (allFound)).either.map {
          case Right(users) =>
            users match {
              case Array() => Response.status(Forbidden)
              case htail => {
                //Response.json(htail.head.asJson.spaces2)
                Response.text(s"{\"token\": \"${JwtUtils.createToken(htail.head.username)}\"}")
                _ <- ZIO.logInfo(s"Signed in $user")
              }
            }
          case Left(_) => Response.status(BadRequest)
        }
//        ZIO.succeed(Response.status(NotImplemented))
    }
}

