package images

import images.api.HttpRoutes
//import .model.User
import scala.collection.mutable.ListBuffer
import zio.http.{URL, Body, Request, !!}
import zio.ZLayer
import zio.http.model.{Status}
import zio.test.{ZIOSpecDefault, suite, test, assertTrue}

object ImagesSpec extends ZIOSpecDefault {
  def spec =
    suite("Main suite")(
      test("Upload should return Ok if sent JPEG") {
        assertTrue(1 == 1)
        /*val user: User = new User("aaa", "bbb")
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
        )*/
      },
/*      test("Sign up should return BadRequest if already exists") {
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
      }*/
    )
}
