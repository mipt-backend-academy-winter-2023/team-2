package auth.api

import zio.IO
import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import zio.json._
import zio.http.netty._

/*case class User(username: String, password: String)
    object User {
      implicit val encoder: JsonEncoder[User] = DeriveJsonEncoder.gen[User]
      implicit val decoder: JsonDecoder[User] = DeriveJsonDecoder.gen[User]
      implicit val codec: JsonCodec[User] = DeriveJsonCodec.gen[User]
    }*/
case class User(name: String, password: String)

object User {
  implicit val encoder: JsonEncoder[User] = DeriveJsonEncoder.gen[User]
  implicit val decoder: JsonDecoder[User] = DeriveJsonDecoder.gen[User]
  /*given JsonEncoder[User] =
    DeriveJsonEncoder.gen[User]
  given JsonDecoder[User] =
    DeriveJsonDecoder.gen[User]*/
}

object HttpRoutes {

  val app: HttpApp[Any, Response] = Http.collectZIO[Request] {
    /*case req@Method.GET -> !! / "route" / "find" / name => //ZIO.succeed(Response.text(s"${name} Hello2233"))
      val response = for {
        name <- ZIO.fromOption(
          req
            .url
            .queryParams
            .get("name")
            .flatMap(_.headOption)
        ).tapError(_ => ZIO.logError("not provide id"))
      } yield Response.text(s"Hello $name")
      response.orElseFail(Response.status(Status.BadRequest))*/


    

    case req@Method.POST -> !! / "auth" / "signin" => {
      /*val body = req.body.asString
      val u = body.map(_.fromJson[User])
      //u.flatMap(tmp => {println(tmp); tmp})
      //u.map(tmp => {println(tmp); tmp})
      //println(NettyBody.fromAsync(req.body).toString)
      req.body.asString
      println(req.url.getClass)
      println(req.url.queryParams.getClass)
      println(req.url.queryParams.get("name").getClass)
      println(req.body.asString.getClass)
      println(u.getClass)
      //ZIO.logInfo(s"POST /users -d $body")
      //val u = body.fromJson[User]
      //val u = req.body.asString.map(_.fromJson[User])
      for {
          //body <- req.body.asString
          _ <- ZIO.logInfo(s"POST /users -d $body")
          //_ <- ZIO.logInfo(s"POST /users -d ${u.username}")
          qwe <- u
          r <- ZIO.succeed(Response.json("{'username': 'qwe', 'password': '1'}"))
          
          //r <- u match {
          //  case Left(e) =>
          //    ZIO
          //      .debug(s"Failed to parse the input: $e")
          //      .as(
          //        Response.text(e).setStatus(Status.BadRequest)
          //      )
          //  case Right(u) =>
          //    ZIO.succeed(Response.json(u.toJson))
          //}
        } yield r*/
        //IO.effectAsync[Any, Nothing, Response] { cb => cb(ZIO.succeed(Response.text("qweqwe"))) }
        //val u = body.map(_.fromJson[User]).map(_ => ZIO.succeed(Response.text("qweqwe")))
        //req.body.asString.flatMap(_ => try { ZIO.succeed(Response.text("qweqwe")) } catch {case e : Exception => ZIO.fail(Response.text("qweqwe"))})
        //req.body.asString.flatMap(_ => Try (ZIO.succeed(Response.text("qweqwe"))))
        //req.body.asString.map(Response.text)
        req.body.asString.map(z => Response.text("qwe" + z)).orElseFail(Response.status(Status.BadRequest))
    }
      /*val userZIO = req.body.asString.map(_.fromJson[User])
      for {
        userOrError <- userZIO
        response <- userOrError match {
          case Left(e) => ZIO.debug(s"Failed to parse the input: $e").as(
            Response.text(e).setStatus(Status.BadRequest)
          )
          case Right(user) => ZIO.succeed(Response.json(user.toJson))
        }
      } yield response
    }*/
        //ZIO.succeed(Response.text(s"${req.body.asString}"))}
        /*for {
          name <- ZIO.fromOption(
            req
              .url
              .queryParams
              .get("name")
              .flatMap(_.headOption)
          ).tapError(_ => ZIO.logError("not provide id"))
        } yield Response.text(s"Hello $name")*/
      //response.orElseFail(Response.status(Status.BadRequest))

    /*case req@Method.GET -> !! / "route" / "find" if (req.url.queryParams.nonEmpty) =>
      ZIO.succeed(Response.text(s"Hello ${req.url.queryParams("ids").mkString(" and ")}!"))*/
  }

}
