package routing

import routing.api.HttpRoutes
import routing.model.{Node, Edge}
import routing.utils.Graph
import io.circe.jawn.decode
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import scala.collection.mutable.ListBuffer
import zio._
import zio.stream._
import zio.http.{URL, Body, QueryParams, Request, !!}
import zio.http.model.{Status}
import zio.test.{ZIOSpecDefault, suite, test, assertTrue}

object RoutingTest extends ZIOSpecDefault {
  def spec =
    suite("Routing tests")(
      test("Find should return BadRequest if fromId is not int") {
        (for {
          response <- HttpRoutes.app.runZIO(
            Request.get(
              URL(
                !! / "route" / "find",
                queryParams =
                  QueryParams("fromId" -> Chunk("a"), "toId" -> Chunk("2"))
              )
            )
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
            Request.get(
              URL(
                !! / "route" / "find",
                queryParams =
                  QueryParams("fromId" -> Chunk("1"), "toId" -> Chunk("b"))
              )
            )
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
          _ <- Graph.loadGraph
          response <- HttpRoutes.app.runZIO(
            Request.get(
              URL(
                !! / "route" / "find",
                queryParams =
                  QueryParams("fromId" -> Chunk("1"), "toId" -> Chunk("3"))
              )
            )
          )
          body <- response.body.asString
        } yield {
          assertTrue(1 == 2)
          assertTrue(
            body == "Route - House house1 - Edge firstHalfOfStreet - Crossroad intersection1 - Edge secondHalfOfStreet - House house2 "
          )
        }).provideLayer(
          ZLayer.succeed(
            new MockNodeRepositoryImpl(
              ListBuffer(
                Node(1, "0", "house1", "SRID=4326;POINT(-110 30)"),
                Node(2, "1", "intersection1", "SRID=4326;POINT(-110 30)"),
                Node(3, "0", "house2", "SRID=4326;POINT(-110 30)")
              )
            )
          ) ++
            ZLayer.succeed(
              new MockEdgeRepositoryImpl(
                ListBuffer(
                  Edge("firstHalfOfStreet", 1, 2, 1.0.toFloat),
                  Edge("secondHalfOfStreet", 2, 3, 2.0.toFloat)
                )
              )
            )
        )
      }
    )
}
