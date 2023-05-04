package auth.api

import zio.json._
import zio.http._
import zio.http.model.{Method, Status}

case class UserReq(username: String, password: String)

object UserReq {
  implicit val encoder: JsonEncoder[UserReq] = DeriveJsonEncoder.gen[UserReq]
  implicit val decoder: JsonDecoder[UserReq] = DeriveJsonDecoder.gen[UserReq]
}

case class TokenResp(token: String)

object TokenResp {
  implicit val encoder: JsonEncoder[TokenResp] = DeriveJsonEncoder.gen[TokenResp]
  implicit val decoder: JsonDecoder[TokenResp] = DeriveJsonDecoder.gen[TokenResp]
}

object HttpRoutes {

  val app: HttpApp[Any, Response] = Http.collectZIO[Request] {
    case req@Method.POST -> !! / "auth" / "signin" => {
      req.body.asString.map(body =>
        body.fromJson[UserReq] match {
          case Left(e) => Response.status(Status.Forbidden)
          case Right(user) => Response.json(TokenResp(user.username + "|" + user.password).toJson)
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
