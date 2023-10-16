package routing

import routing.api.HttpRoutes
import routing.model.{Node, Edge}
import io.circe.jawn.decode
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import scala.collection.mutable.ListBuffer
//import zio.{Chunk, ZLayer}
import zio._
import zio.stream._
import zio.http.{URL, Body, QueryParams, Request, !!}
import zio.http.model.{Status}
import zio.test.{ZIOSpecDefault, suite, test, assertTrue}

object RoutingSpec extends ZIOSpecDefault {
  def spec =
    suite("Routing tests")(
      test("Find should return BadRequest if fromId is not int") {
        (for {
          response <- HttpRoutes.app.runZIO(
            Request.get(URL(!! / "route" / "find", queryParams = QueryParams("fromId" -> Chunk("a"), "toId" -> Chunk("2"))))
          )
        } yield {
          assertTrue(response.status == Status.BadRequest)
        }).provideLayer(
          ZLayer.succeed(new MockNodeRepositoryImpl(ListBuffer())) ++
          ZLayer.succeed(new MockEdgeRepositoryImpl(ListBuffer()))
        )
      },
      test("Find should return BadRequest if toId is not int") {
        (for {
          response <- HttpRoutes.app.runZIO(
            Request.get(URL(!! / "route" / "find", queryParams = QueryParams("fromId" -> Chunk("1"), "toId" -> Chunk("b"))))
          )
        } yield {
          assertTrue(response.status == Status.BadRequest)
        }).provideLayer(
          ZLayer.succeed(new MockNodeRepositoryImpl(ListBuffer())) ++
          ZLayer.succeed(new MockEdgeRepositoryImpl(ListBuffer()))
        )
      },
      test("Find should return proper route") {
        (for {
          response <- HttpRoutes.app.runZIO(
            Request.get(URL(!! / "route" / "find", queryParams = QueryParams("fromId" -> Chunk("1"), "toId" -> Chunk("4"))))
          )
          body <- response.body.asString
        } yield {
          println(body)
          assertTrue(response.status == Status.BadRequest)
        }).provideLayer(
          ZLayer.succeed(new MockNodeRepositoryImpl(ListBuffer())) ++
          ZLayer.succeed(new MockEdgeRepositoryImpl(ListBuffer()))
        )
      },
    )
}
