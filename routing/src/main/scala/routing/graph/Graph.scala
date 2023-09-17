package routing.graph

import routing.model.{Edge, Node}
import routing.repository.{EdgeRepository, NodeRepository}
import zio.ZIO
import zio.stream.ZStream

case class GeoPoint(latitude: Double, longitude: Double)

sealed abstract class GeoNode(val id: Int, val geoPoint: GeoPoint, val name: Option[String])

class Intersection(id: Int, geoPoint: GeoPoint, name: Option[String]) extends GeoNode(id, geoPoint, name)

class House(id: Int, geoPoint: GeoPoint, name: Option[String]) extends GeoNode(id, geoPoint, name)

case class Street(id: Int, name: String)


trait Graph {
  def findRoute(startId: Int, goalId: Int): List[GeoNode]

  def loadEdges(edgeStream: ZStream[EdgeRepository, Throwable, Edge]): ZIO[EdgeRepository, Throwable, Unit]

  def loadNodes(nodeStream: ZStream[NodeRepository, Throwable, Node]): ZIO[NodeRepository, Throwable, Unit]
}
