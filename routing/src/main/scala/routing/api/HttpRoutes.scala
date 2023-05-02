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
      val points = req.url.queryParams("ids").foldLeft(List[Point]())((acc, next) => Point(Integer.valueOf(next), "name" + next) :: acc)
      ZIO.succeed(Response.json(PointsResp(points).toJson))
    }
  }

}
