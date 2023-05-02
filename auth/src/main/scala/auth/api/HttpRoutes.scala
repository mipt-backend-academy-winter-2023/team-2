package auth.api

import zio.json._
import zio.http._
import zio.http.model.{Method, Status}

case class UserReq(name: String, password: String)

object UserReq {
  implicit val encoder: JsonEncoder[UserReq] = DeriveJsonEncoder.gen[UserReq]
  implicit val decoder: JsonDecoder[UserReq] = DeriveJsonDecoder.gen[UserReq]
}

case class TokenReq(token: String)

object TokenReq {
  implicit val encoder: JsonEncoder[TokenReq] = DeriveJsonEncoder.gen[TokenReq]
  implicit val decoder: JsonDecoder[TokenReq] = DeriveJsonDecoder.gen[TokenReq]
}

object HttpRoutes {

  val app: HttpApp[Any, Response] = Http.collectZIO[Request] {
    case req@Method.POST -> !! / "auth" / "signin" => {
      req.body.asString.map(body =>
        body.fromJson[UserReq] match {
          case Left(e) => Response.status(Status.Forbidden)
          case Right(user) => Response.json(TokenReq(user.name + "|" + user.password).toJson)
        }
      ).orElseFail(Response.status(Status.BadRequest))
    }

    case req@Method.POST -> !! / "auth" / "signup" => {
      req.body.asString.map(body =>
        body.fromJson[UserReq] match {
          case Left(e) => Response.status(Status.Forbidden)
          case Right(user) => Response.status(Status.Ok)
        }
      ).orElseFail(Response.status(Status.BadRequest))
    }
  }

}
