package auth

import auth.api.HttpRoutes
import auth.model.{User, JsonProtocol}
import io.circe.jawn.decode
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import scala.collection.mutable.ListBuffer
import zio.http.{URL, Body, Request, !!}
import zio.ZLayer
import zio.http.model.{Status}
import zio.test.{ZIOSpecDefault, suite, test, assertTrue}

object AuthSpec extends ZIOSpecDefault {
  def userToStringJson(user: User) =
    s"""{"username": "${user.username}","password": "${user.password}"}"""

  def spec =
    suite("Main suite")(
      test("Sign up should return Ok if not exists") {
        val user: User = new User("aaa", "bbb")
        val user2: User = new User("ccc", "ddd")
        (for {
          response <- HttpRoutes.app.runZIO(
            Request.post(
              Body.fromString(userToStringJson(user)),
              URL(!! / "auth" / "signup")
            )
          )
          body <- response.body.asString
        } yield {
          assertTrue(response.status == Status.Ok)
        }).provideLayer(
          ZLayer.succeed(new MockUserRepositoryImpl(ListBuffer(user2)))
        )
      },
      test("Sign up should return BadRequest if already exists") {
        val user: User = new User("aaa", "bbb")
        (for {
          response <- HttpRoutes.app.runZIO(
            Request.post(
              Body.fromString(userToStringJson(user)),
              URL(!! / "auth" / "signup")
            )
          )
          body <- response.body.asString
        } yield {
          assertTrue(response.status == Status.BadRequest)
        }).provideLayer(
          ZLayer.succeed(new MockUserRepositoryImpl(ListBuffer(user)))
        )
      },
      test("Sign in should return Forbidden if not exists") {
        val user: User = new User("aaa", "bbb")
        val user2: User = new User("ccc", "ddd")
        (for {
          response <- HttpRoutes.app.runZIO(
            Request.post(
              Body.fromString(userToStringJson(user)),
              URL(!! / "auth" / "signin")
            )
          )
          body <- response.body.asString
        } yield {
          assertTrue(response.status == Status.Forbidden)
        }).provideLayer(
          ZLayer.succeed(new MockUserRepositoryImpl(ListBuffer(user2)))
        )
      },
      test("Sign in should return Ok if already exists") {
        val user: User = new User("aaa", "bbb")
        (for {
          response <- HttpRoutes.app.runZIO(
            Request.post(
              Body.fromString(userToStringJson(user)),
              URL(!! / "auth" / "signin")
            )
          )
          body <- response.body.asString
        } yield {
          assertTrue(response.status == Status.Ok)
        }).provideLayer(
          ZLayer.succeed(new MockUserRepositoryImpl(ListBuffer(user)))
        )
      },
      test("Sign in should return Ok after signing up") {
        val user: User = new User("aaa", "bbb")
        (for {
          responseUp <- HttpRoutes.app.runZIO(
            Request.post(
              Body.fromString(userToStringJson(user)),
              URL(!! / "auth" / "signup")
            )
          )
          responseIn <- HttpRoutes.app.runZIO(
            Request.post(
              Body.fromString(userToStringJson(user)),
              URL(!! / "auth" / "signin")
            )
          )
        } yield {
          assertTrue(responseUp.status == Status.Ok)
          assertTrue(responseIn.status == Status.Ok)
        }).provideLayer(
          ZLayer.succeed(new MockUserRepositoryImpl(ListBuffer(user)))
        )
      }
    )
}
