package auth.api

import java.time.Clock
import zio._
import zio.http._
import zio.http.model.{Method, Status}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}

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

  val signUp: HttpApp[Any, Response] = Http.collectZIO[Request] {
    case request@Method.POST -> !! / "auth" / "signup" =>
      val response =
        for {
          username <- ZIO.fromOption(
            request.url.queryParams.get("username").flatMap(_.headOption)
          )
          password <- ZIO.fromOption(
            request.url.queryParams.get("password").flatMap(_.headOption)
          )
        } yield Response.status(Status.Ok)
      response.orElseFail(Response.status(Status.BadRequest))
  }

  val signIn: HttpApp[Any, Response] = Http.collectZIO[Request] {
    case request@Method.POST -> !! / "auth" / "signin" =>
      val response =
        for {
          username <- ZIO.fromOption(
            request.url.queryParams.get("username").flatMap(_.headOption)
          )
          password <- ZIO.fromOption(
            request.url.queryParams.get("password").flatMap(_.headOption)
          )
          token = JwtUtils.createToken(username)
          jsonBody = s"""{"token": "$token"}"""
          _ = println(s"User $username signed in")
        } yield Response.json(jsonBody)
      response.orElseFail(Response.status(Status.BadRequest))
  }
  val app: HttpApp[Any, Response] = signUp ++ signIn
}
