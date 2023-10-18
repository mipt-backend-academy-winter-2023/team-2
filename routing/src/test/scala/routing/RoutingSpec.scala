package routing

import java.util.concurrent.TimeUnit
import model.{Edge, Node}
import io.circe.Json
import io.circe.Encoder
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import routing.api.HttpRoutes
import routing.utils.Graph
import zio.http.{!!, Body, QueryParams, Request, Response, URL}
import zio.{Chunk, Duration, ZIO, ZLayer}
import zio.http.model.Status
import zio._
import zio.test.TestAspect.sequential
import zio.test.{TestClock, ZIOSpecDefault, assertTrue, suite, test}
object RoutingSpec extends ZIOSpecDefault {
  private def mockRepository(nodes: List[Node], edges: List[Edge]) =
    ZLayer.succeed(new MockNodeRepository(nodes)) ++
      ZLayer.succeed(new MockEdgeRepository(edges))

  private def findQuery(queryParams: Map[String, Chunk[String]]) =
    HttpRoutes.app.runZIO(
      Request.get(
        URL(
          !! / "route" / "find",
          queryParams = QueryParams(queryParams)
        )
      )
    )

  private def findStr(from: String, to: String) =
    findQuery(Map("fromId" -> Chunk(from), "toId" -> Chunk(to)))

  private def find(from: Int, to: Int) =
    findStr(from.toString, to.toString)

  private val house1 = Node(1, "0", "house1", "Point(-1, 0)")
  private val street1 = Edge("street1", 1, 2, 1)
  private val intersection = Node(2, "1", "intersection", "Point(0, 0)")
  private val street2 = Edge("street2", 2, 3, 1)
  private val house2 = Node(3, "0", "house2", "Point(1, 0)")

  private val allNodes = List(house1, intersection, house2)
  private val allEdges = List(street1, street2)
  private def shouldBeOk(response: Response) =
    response.status == Status.Ok

  private def shouldBeBad(response: Response) =
    response.status == Status.BadRequest

  private def checkPath(response: Response, path: String) = {
    val prefix = "Body.fromAsciiString("
    val suffix = " )"
    val body = response.body.toString
    body.length >= prefix.length + suffix.length &&
      body.substring(prefix.length, body.length - suffix.length) == path
  }

  def spec = suite("Routing tests")(
    test("Should return BadRequest if queryParams are ill-formatted") {
      (for {
        fromId_notInt <- findStr("a", "2")
        toId_notInt <- findStr("1", "a")
        no_fromId <- findQuery(Map("toId" -> Chunk("2")))
        no_toId <- findQuery(Map("fromId" -> Chunk("1")))
      } yield { assertTrue(
          shouldBeBad(fromId_notInt)
          && shouldBeBad(toId_notInt)
          && shouldBeBad(no_fromId)
          && shouldBeBad(no_toId)
        )
      }).provideLayer(
        mockRepository(List.empty, List.empty)
      )
    }, test("should fail if there is no path") {
      (for {
        _ <- Graph.loadGraph
        fromId_doesntExists <- find(42, 3)
        toId_doesntExists <- find(1, 42)
        endpointsAreNotConnected <- find(1, 3)
      } yield { assertTrue(
          shouldBeBad(fromId_doesntExists)
          && shouldBeBad(toId_doesntExists)
          && shouldBeBad(endpointsAreNotConnected)
        )
      }).provideLayer(
        mockRepository(allNodes, List.empty)
      )
    }, test("Should correctly find path") {
      (for {
        _ <- Graph.loadGraph
        path_1_1 <- find(1, 1)
        path_1_3 <- find(1, 3)
      } yield {
        val expected_1_1 = "Route - House house1"
        val expected_1_3 = "Route - House house1 - Edge street1 - Crossroad intersection - Edge street2 - House house2"

        assertTrue(
          shouldBeOk(path_1_1)
          && shouldBeOk(path_1_3)
          && checkPath(path_1_1, expected_1_1)
          && checkPath(path_1_3, expected_1_3)
        )
      }).provideLayer(
        mockRepository(allNodes, allEdges)
      )
    }
  ) @@ sequential
}
