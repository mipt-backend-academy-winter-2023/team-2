package auth

/*import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ParserTest extends AnyFlatSpec with Matchers {
  it should "fail this test" in {
    1 shouldBe 2
  }
}*/

/*import auth.api.HttpRoutes
import zio.http.{URL, Request, !!, Header}
import zio.http.model.{Status}
import zio.test.{ZIOSpecDefault, suite, test, assertTrue}

object HomeSpec extends ZIOSpecDefault {
  def spec =
    suite("Main backend application")(
      test("root route should redirect to /greet") {
        for {
          //response <- AuthMain.run.runZIO(Request.get(URL(Root)))
          response <- HttpRoutes.app.runZIO(Request.get(URL(!!)))
          body     <- response.body.asString
        } yield {
          assertTrue(
            response.status == Status.TemporaryRedirect,
            //response.headers(Header.Location).contains(Header.Location(URL(!! / "greet"))),
            body.isEmpty,
          )
        }
      }
    )
}*/



import zio._
import zio.mock._
import auth.repository.UserRepositoryImpl
import auth.model.User

object MockUserRepositoryImpl extends Mock[UserRepositoryImpl] {
  object Save extends Effect[User, String, Unit]

  val compose: URLayer[Proxy, UserRepositoryImpl] =
    ZLayer {
      for {
        proxy <- ZIO.service[Proxy]
      } yield new UserRepositoryImpl {
        override def save(user: User): IO[String, Unit] =
          proxy(Save, user)
      }
    }
}



import auth.api.HttpRoutes
import zio.http.{URL, Request, !!} //, Header}
import zio.http.model.{Status}
import zio.test.{ZIOSpecDefault, suite, test, assertTrue}

object HomeSpec extends ZIOSpecDefault {
  def spec =
    suite("Main backend application")(
      test("root route should redirect to /greet") {
        (for {
          response <- HttpRoutes.app.runZIO(Request.get(URL(!!)))
          body     <- response.body.asString
        } yield {
          assertTrue(
            response.status == Status.TemporaryRedirect,
            //response.headers(Header.Location).contains(Header.Location(URL(!! / "greet"))),
            body.isEmpty,
          )
        }).provideLayer(MockUserRepositoryImpl.live)
      }
    )
}





/*import auth.api.HttpRoutes
import zio.RIO
import zio.http.{URL, Request, !!} //, Header}
import zio.http.model.Status
import zio.test.environment.TestEnvironment
import zio.test.mock.Expectation._
import zio.test.mock._
import zio.test.{ZIOBaseSpec, assertM, suite, test}

object HomeSpec extends ZIOBaseSpec with MockUserRepository {
  def spec = suite("Main backend application")(
    testM("root route should redirect to /greet") {
      val expectedResponse = ??? // specify the expected response here
      (for {
        request <- RIO.succeed(Request.get(URL(!!)))
        result  <- HttpRoutes.app.run(request)
        body    <- result.body.asString
      } yield {
        // assertM(result.status, equalTo(Status.TemporaryRedirect)) &&
        // assertM(body, isEmptyString) &&
        // assertM(mockUserRepository.verify(addToDatabase(equalTo(expectedResponse))), once)
      }).provideCustomLayer(mockUserRepository)
    }
  )
}*/

/*import auth.repository.UserRepository
import zio.{Has, IO, Ref, UIO, URLayer, ZIO, ZLayer}

object MockUserRepository {
  val mock: URLayer[Has[Ref[List[String]]], UserRepository] = 
    ZLayer.fromServiceM { ref: Ref[List[String]] =>
      new UserRepository.Service {
        def addUser(user: String): UIO[Unit] =
          ref.update(users => user :: users).unit

        def getUsers: IO[Unit, List[String]] =
          ref.get
      }
    }

  def addToDatabase(user: String): ZIO[Has[UserRepository], Nothing, Unit] =
    ZIO.accessM(_.get.addUser(user))

  def getUsersFromDatabase: ZIO[Has[UserRepository], Throwable, List[String]] =
    ZIO.accessM(_.get.getUsers)

  def makeRef(initial: List[String]): UIO[Ref[List[String]]] =
    Ref.make(initial)
}

import auth.api.HttpRoutes
import auth.repository.UserRepository
import zio.RIO
import zio.http._
import zio.http.model.Status
import zio.test.assertM
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, assertM, suite, test}

object HomeSpec extends DefaultRunnableSpec {
  val testLayer: URLayer[TestEnvironment, Has[UserRepository]] =
    MockUserRepository.mock

  def spec = suite("Main backend application")(
    testM("root route should redirect to /greet") {
      val expectedResponse = ??? // specify the expected response here

      (for {
        request <- RIO.succeed(Request.get(URL(!!)))
        result  <- HttpRoutes.app.run(request)
        body    <- result.body.asString
      } yield {
        assertM(result.status, equalTo(Status.TemporaryRedirect)) &&
          assertM(body, isEmptyString) &&
          assertM(MockUserRepository.verify(MockUserRepository.addToDatabase(equalTo(expectedResponse))), once)
      }).provideCustomLayer(testLayer)
    }
  )
}
*/
