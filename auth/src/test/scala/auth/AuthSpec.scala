package auth

import auth.api.HttpRoutes
import auth.model.{User, JsonProtocol}
import io.circe.Json
import io.circe.Encoder
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import zio.http.{URL, Body, Request, !!}
import zio.ZLayer
import zio.http.model.{Status}
import zio.test.{ZIOSpecDefault, suite, test, assertTrue}
import scala.collection.mutable

object AuthSpec extends ZIOSpecDefault {

  def mockUserRepository() = {
    ZLayer.succeed(new MockUserRepository(mutable.HashMap.empty))
  }
  def userBody(user: User) = {
    val json = Json.obj(
      "username" -> Encoder[String].apply(user.username),
      "password" -> Encoder[String].apply(user.password)
    )
    Body.fromString(json.noSpaces)
  }

  private val user1                   = new User("a", "a")
  private val user1_with_dif_password = user1.copy(password = user1.password + "some changed")
  private val user2                   = new User("b", "b")

  def signUp(user: User) =
    HttpRoutes.app.runZIO(
      Request.post(
        userBody(user),
        URL(!! / "auth" / "signup")
      )
    )

  def signIn(user: User) =
    HttpRoutes.app.runZIO(
      Request.post(
        userBody(user),
        URL(!! / "auth" / "signin")
      )
    )

  def shouldBeOk(response: zio.http.Response) =
    assertTrue(response.status == Status.Ok)

  def shouldBeBad(response: zio.http.Response) =
    assertTrue(response.status == Status.BadRequest)
  def spec = suite("Auth tests")(
    test("Sign Up should work correct") {
      (for {
        user1_signup                    <- signUp(user1)
        user1_signup_again              <- signUp(user1)
        user1_signup_different_password <- signUp(user1_with_dif_password)
        user2_signup                    <- signUp(user2)
      } yield {
        shouldBeOk(user1_signup)
        shouldBeBad(user1_signup_again)
        shouldBeBad(user1_signup_different_password)
        shouldBeOk(user2_signup)
      }).provideLayer(
        mockUserRepository()
      )
    },
    test("Sign in should work correct") {
      (for {
        user1_early_signin  <- signIn(user1)
        user2_early_signin1 <- signIn(user2)

        user1_signup <- signUp(user1)

        user1_signin1        <- signIn(user1)
        user1_wrong_password <- signIn(user1_with_dif_password)
        user2_early_signin2  <- signIn(user2)

        user2_signup <- signUp(user2)

        user1_signin2 <- signIn(user1)
        user2_signin  <- signIn(user2)
      } yield {
        shouldBeBad(user1_early_signin)
        shouldBeBad(user2_early_signin1)
        shouldBeOk(user1_signup)
        shouldBeOk(user1_signin1)
        shouldBeBad(user1_wrong_password)
        shouldBeBad(user2_early_signin2)
        shouldBeOk(user2_signup)
        shouldBeOk(user1_signin2)
        shouldBeOk(user2_signin)
      }).provideLayer(
        mockUserRepository()
      )
    }
  )
}
