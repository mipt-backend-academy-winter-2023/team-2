package auth

import zio.{ZIO, ZLayer, ULayer}
import zio.http.{Body, Request, Response, URL, !!}
import zio.http.model.Status
import zio.test.{ZIOSpecDefault, Spec, TestResult, assertTrue}

import auth.api.HttpRoutes
import auth.model.User
import auth.repository.UserRepository

class Auth extends ZIOSpecDefault {
  def mockUserRepository(): ULayer[MockUserRepository] = {
    ZLayer.succeed(new MockUserRepository(collection.mutable.ListBuffer.empty))
  }

  def userToJsonBody(user: User): Body = {
    val json =
      s"""
         |{
         |  "username": "${user.username}",
         |  "password": "${user.password}"
         |}
    """.stripMargin
    Body.fromString(json)
  }

  def signUp(user: User): ZIO[UserRepository, Option[Response], Response] =
    HttpRoutes.app.runZIO(
      Request.post(
        userToJsonBody(user),
        URL(!! / "auth" / "signup")
      )
    )

  def signIn(user: User): ZIO[UserRepository, Option[Response], Response] =
    HttpRoutes.app.runZIO(
      Request.post(
        userToJsonBody(user),
        URL(!! / "auth" / "signin")
      )
    )

  def assertOk(response: zio.http.Response): TestResult =
    assertTrue(response.status == Status.Ok)

  def assertBadRequest(response: zio.http.Response): TestResult =
    assertTrue(response.status == Status.BadRequest)

  private val mock_user1 = new User("mock-username-1", "mock-password-1")
  private val mock_user1_copy_password =
    new User("mock-username-1-copy", "mock-password-1")
  private val mock_user1_copy_username =
    new User("mock-username-1", "mock-password-1-copy")
  def spec: Spec[Any, Option[Response]] = suite("Auth tests")(
    test("Successful sign up and sign in") {
      (for {
        user1_sign_up <- signUp(mock_user1)
        user1_sign_in <- signIn(mock_user1)
        user1_sign_in_again <- signIn(mock_user1)
        user1_copy_password_sign_up <- signUp(mock_user1_copy_password)
      } yield {
        assertOk(user1_sign_up)
        assertOk(user1_sign_in)
        assertOk(user1_sign_in_again)
        assertOk(user1_copy_password_sign_up)
      }).provideLayer(mockUserRepository())
    }
  )
  test("Unsuccessful sign up again and sign in") {
    (for {
      user1_sign_up <- signUp(mock_user1)
      user1_sign_up_again <- signUp(mock_user1)
      user1_mock_user1_copy_password_sign_up <- signUp(mock_user1_copy_password)
      user1_mock_user1_copy_username_sign_up <- signUp(mock_user1_copy_username)

      user_1_sign_in <- signIn(mock_user1)
      user_1_sign_in_again <- signIn(mock_user1)
      user1_mock_user1_copy_password_sign_in <- signIn(mock_user1_copy_password)
      user1_mock_user1_copy_username_sign_in <- signIn(mock_user1_copy_username)
    } yield {
      assertOk(user1_sign_up)
      assertBadRequest(user1_sign_up_again)
      assertOk(user1_mock_user1_copy_password_sign_up)
      assertBadRequest(user1_mock_user1_copy_username_sign_up)

      assertOk(user_1_sign_in)
      assertOk(user_1_sign_in_again)
      assertOk(user1_mock_user1_copy_password_sign_in)
      assertBadRequest(user1_mock_user1_copy_username_sign_in)
    }).provideLayer(mockUserRepository())
  }

}
