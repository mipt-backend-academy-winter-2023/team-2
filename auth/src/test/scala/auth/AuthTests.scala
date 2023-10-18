package auth

import auth.api.HttpRoutes
import auth.model.{JsonProtocol, User}
import io.circe.Json
import io.circe.Encoder
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import zio.http.{!!, Body, Request, Response, URL}
import zio.ZLayer
import zio.http.model.Status
import zio.test.TestAspect.sequential
import zio.test.{ZIOSpecDefault, assertTrue, suite, test}

import scala.collection.mutable

trait SpecBase {
  val user1                   = new User("a", "a")
  val user1_with_dif_password = user1.copy(password = user1.password + "some changed")
  val user2                   = new User("b", "b")
  def userBody(user: User) = {
    val json = Json.obj(
      "username" -> Encoder[String].apply(user.username),
      "password" -> Encoder[String].apply(user.password)
    )
    Body.fromString(json.noSpaces)
  }

  def mockUserRepository() = {
    ZLayer.succeed(new MockUserRepository(mutable.HashMap.empty))
  }
  def shouldBeOk(response: Response) = response.status == Status.Ok

  def shouldBeBadRequest(response: Response) = response.status == Status.BadRequest

  def shouldBeForbidden(response: Response) = response.status == Status.Forbidden

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
}
object SignUpSpec extends ZIOSpecDefault with SpecBase {
  def spec = suite("Sign Up tests")(
    test("Sign Up should work correct") {
      (for {
        user1_signup                    <- signUp(user1)
        user1_signup_again              <- signUp(user1)
        user1_signup_different_password <- signUp(user1_with_dif_password)
        user2_signup                    <- signUp(user2)
      } yield {
        assertTrue(
          shouldBeOk(user1_signup)
            && shouldBeBadRequest(user1_signup_again)
            && shouldBeBadRequest(user1_signup_different_password)
            && shouldBeOk(user2_signup)
        )
      }).provideLayer(
        mockUserRepository()
      )
    }
  )
}

object SignInSpec extends ZIOSpecDefault with SpecBase {

  def spec = suite("Sign In tests)")(
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
        assertTrue(
          shouldBeForbidden(user1_early_signin)
            && shouldBeForbidden(user2_early_signin1)
            && shouldBeOk(user1_signup)
            && shouldBeOk(user1_signin1)
            && shouldBeForbidden(user1_wrong_password)
            && shouldBeForbidden(user2_early_signin2)
            && shouldBeOk(user2_signup)
            && shouldBeOk(user1_signin2)
            && shouldBeOk(user2_signin)
        )
      }).provideLayer(
        mockUserRepository()
      )
    }
  )
}
