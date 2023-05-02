package routing.api

import zio.json._
import zio.http._
import zio.http.model.{Method, Status}
import zio.ZIO

case class Point(id: Int, name: String)

object Point {
  implicit val encoder: JsonEncoder[Point] = DeriveJsonEncoder.gen[Point]
  implicit val decoder: JsonDecoder[Point] = DeriveJsonDecoder.gen[Point]
}

case class PointsResp(points: List[Point])

object PointsResp {
  implicit val encoder: JsonEncoder[PointsResp] = DeriveJsonEncoder.gen[PointsResp]
  implicit val decoder: JsonDecoder[PointsResp] = DeriveJsonDecoder.gen[PointsResp]
}

object HttpRoutes {

  val app: HttpApp[Any, Response] = Http.collectZIO[Request] {
    case req@Method.GET -> !! / "route" / "find" if (req.url.queryParams.nonEmpty) => {
      //val points = req.url.queryParams("ids").flatMap(id => Point(id, "name" + id))
      val points = req.url.queryParams("ids").foldLeft(List[Point]())((acc, next) => Point(Integer.valueOf(next), "name" + next) :: acc)
      ZIO.succeed(Response.json(PointsResp(points).toJson))
      //ZIO.succeed(Response.text(s"Hello ${req.url.queryParams("ids").mkString(" and ")}!"))
    }
  }

}




/*case class UserReq(name: String, password: String)

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

}*/
