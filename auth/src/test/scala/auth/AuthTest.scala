package auth

import zio.{ZIO, ZLayer}
import zio.stream.ZStream
import auth.repository.UserRepository
import auth.model.User
import scala.collection.mutable.ListBuffer

final class MockUserRepositoryImpl(users: ListBuffer[User]) extends UserRepository {
  def add(user: User): ZIO[UserRepository,Throwable,Unit] = {
    if (users.forall(_.username != user.username)) {
      users += user
      ZIO.succeed()
    } else {
      ZIO.fail(new Exception("User already exists"))
    }
  }
  def findUser(user: User): ZStream[Any,Throwable,User] =
    users.find(x => x.username == user.username && x.password == user.password) match {
      case None => ZStream.empty
      case Some(foundUser) => ZStream.fromZIO(ZIO.succeed(foundUser))
    }
  def findUserByUsername(user: User): ZStream[UserRepository,Throwable,User] =
    ZStream.fromZIO(ZIO.succeed(new User("", "")))
}



import auth.api.HttpRoutes
import zio.http.{URL, Body, Request, !!}
import zio.http.model.{Status}
import zio.test.{ZIOSpecDefault, suite, test, assertTrue}

object AuthSpec extends ZIOSpecDefault {
  def spec =
    suite("Main suite")(
      test("Sign up should return Ok if not exists") {
        val user: User = new User("aaa", "bbb")
        val user2: User = new User("ccc", "ddd")
        (for {
          response <- HttpRoutes.app.runZIO(Request.post(Body.fromString(s"{\"username\": \"${user.username}\",\"password\": \"${user.password}\"}"), URL(!! / "auth" / "signup")))
          body     <- response.body.asString
        } yield {
          assertTrue(response.status == Status.Ok)
        }).provideLayer(ZLayer.succeed(new MockUserRepositoryImpl(ListBuffer(user2))))
      },
      test("Sign up should return BadRequest if already exists") {
        val user: User = new User("aaa", "bbb")
        (for {
          response <- HttpRoutes.app.runZIO(Request.post(Body.fromString(s"{\"username\": \"${user.username}\",\"password\": \"${user.password}\"}"), URL(!! / "auth" / "signup")))
          body     <- response.body.asString
        } yield {
          assertTrue(response.status == Status.BadRequest)
        }).provideLayer(ZLayer.succeed(new MockUserRepositoryImpl(ListBuffer(user))))
      },
      test("Sign in should return Forbidden if not exists") {
        val user: User = new User("aaa", "bbb")
        val user2: User = new User("ccc", "ddd")
        (for {
          response <- HttpRoutes.app.runZIO(Request.post(Body.fromString(s"{\"username\": \"${user.username}\",\"password\": \"${user.password}\"}"), URL(!! / "auth" / "signin")))
          body     <- response.body.asString
        } yield {
          assertTrue(response.status == Status.Forbidden)
        }).provideLayer(ZLayer.succeed(new MockUserRepositoryImpl(ListBuffer(user2))))
      },
      test("Sign in should return Ok if already exists") {
        val user: User = new User("aaa", "bbb")
        (for {
          response <- HttpRoutes.app.runZIO(Request.post(Body.fromString(s"{\"username\": \"${user.username}\",\"password\": \"${user.password}\"}"), URL(!! / "auth" / "signin")))
          body     <- response.body.asString
        } yield {
          assertTrue(response.status == Status.Ok)
        }).provideLayer(ZLayer.succeed(new MockUserRepositoryImpl(ListBuffer(user))))
      }
    )
}

