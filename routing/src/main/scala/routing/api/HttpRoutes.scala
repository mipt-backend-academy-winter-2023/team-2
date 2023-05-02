package routing.api

import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}

object HttpRoutes {

  val app: HttpApp[Any, Response] = Http.collectZIO[Request] {
    case req@Method.GET -> !! / "route" / "find" if (req.url.queryParams.nonEmpty) =>
      ZIO.succeed(Response.text(s"Hello ${req.url.queryParams("ids").mkString(" and ")}!"))

    /*case Method.GET -> !! / "route" / "find" => ZIO.succeed(Response.text("Hello2233"))
    case req@Method.POST -> !! / "greeting" / "by" =>
      val response =
        for {
          name <- ZIO.fromOption(
            req
              .url
              .queryParams
              .get("name")
              .flatMap(_.headOption)
          ).tapError(_ => ZIO.logError("not provide id"))
        } yield Response.text(s"Hello $name")
      response.orElseFail(Response.status(Status.BadRequest))*/
  }

}