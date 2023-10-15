package auth

import zio.{ZIO, ZLayer}
import zio.stream.ZStream
import auth.repository.UserRepository
import auth.model.User

final class MockUserRepositoryImpl extends UserRepository {
  def add(user: auth.model.User): zio.ZIO[auth.repository.UserRepository,Throwable,Unit] = {
    //ZIO.succeed()
    ZIO.fail(new Exception(""))
  }
  def findUser(user: auth.model.User): zio.stream.ZStream[Any,Throwable,auth.model.User] =
    ZStream.fromZIO(ZIO.succeed(new User("", "")))
  def findUserByUsername(user: auth.model.User): zio.stream.ZStream[auth.repository.UserRepository,Throwable,auth.model.User] =
    ZStream.fromZIO(ZIO.succeed(new User("", "")))
}

object MockUserRepositoryImpl {
  val live: ZLayer[Any, Throwable, UserRepository] =
    ZLayer.succeed(new MockUserRepositoryImpl)
}



import auth.api.HttpRoutes
import zio.http.{URL, Body, Request, !!}
import zio.http.model.{Status}
import zio.test.{ZIOSpecDefault, suite, test, assertTrue}

object AuthSpec extends ZIOSpecDefault {
  def spec =
    suite("Main suite")(
      test("Sign up should return ok status") {
        (for {
          response <- HttpRoutes.app.runZIO(Request.post(Body.fromString("{\"username\": \"theUser\",\"password\": \"1112345\"}"), URL(!! / "auth" / "signup")))
          body     <- response.body.asString
        } yield {
          assertTrue(response.status == Status.Ok)
        }).provideLayer(MockUserRepositoryImpl.live)
      }
    )
}

