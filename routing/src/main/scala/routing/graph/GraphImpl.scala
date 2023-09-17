package routing.graph

import routing.model.{Edge, Node}
import routing.repository.{EdgeRepository, NodeRepository}
import zio.ZIO
import zio.stream.ZStream

import scala.collection.mutable

class GraphImpl extends Graph {
  private var nodes: Map[Int, GeoNode] = Map()
  private var edges: Set[(GeoNode, GeoNode, Street)] = Set()

  private def heuristic(start: GeoNode, goal: GeoNode): Double = {
    val (lat1, lon1) = (start.geoPoint.latitude, start.geoPoint.longitude)
    val (lat2, lon2) = (goal.geoPoint.latitude, goal.geoPoint.longitude)

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = Math.pow(Math.sin(dLat / 2), 2) +
      Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math
        .pow(Math.sin(dLon / 2), 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    val distance = 6371 * c
    distance
  }

  def findRoute(fromId: Int, toId: Int): List[GeoNode] = {
    val from = nodes.getOrElse(fromId, return Nil)
    val to = nodes.getOrElse(toId, return Nil)

    val openSet = mutable.PriorityQueue((heuristic(from, to), from))(
      Ordering.by[(Double, GeoNode), Double](-_._1)
    )
    val (parentMap, gScore, fScore) = initializeMaps(from, to)

    while (openSet.nonEmpty) {
      val current = openSet.dequeue()._2
      if (current.id == to.id)
        return reconstructPath(parentMap, current)
      updateScoresAndCameFrom(current, to, parentMap, gScore, fScore, openSet)
    }
    Nil
  }

  private def initializeMaps(start: GeoNode, goal: GeoNode): (
      mutable.Map[GeoNode, GeoNode],
      mutable.Map[GeoNode, Double],
      mutable.Map[GeoNode, Double]
  ) = {
    val parentMap = mutable.Map.empty[GeoNode, GeoNode]
    val gScore = mutable.Map(start -> 0.0)
    val fScore = mutable.Map(start -> heuristic(start, goal))
    (parentMap, gScore, fScore)
  }

  private def reconstructPath(
      cameFrom: mutable.Map[GeoNode, GeoNode],
      current: GeoNode
  ): List[GeoNode] = {
    val path = Iterator
      .iterate(current)(cameFrom.getOrElse(_, return Nil))
      .takeWhile(cameFrom.contains)
      .toList :+ current
    path.reverse
  }

  private def updateScoresAndCameFrom(
      current: GeoNode,
      to: GeoNode,
      parentMap: mutable.Map[GeoNode, GeoNode],
      gScore: mutable.Map[GeoNode, Double],
      fScore: mutable.Map[GeoNode, Double],
      openSet: mutable.PriorityQueue[(Double, GeoNode)]
  ): Unit = {
    for (edge <- edges.filter(e => e._1 == current || e._2 == current)) {
      val neighbor = if (edge._1 == current) edge._2 else edge._1
      val tentativeGScore = gScore(current) + heuristic(current, neighbor)
      if (!gScore.contains(neighbor) || tentativeGScore < gScore(neighbor)) {
        parentMap(neighbor) = current
        gScore(neighbor) = tentativeGScore
        fScore(neighbor) = tentativeGScore + heuristic(neighbor, to)
        if (!openSet.exists(_._2.id == neighbor.id))
          openSet.enqueue((fScore(neighbor), neighbor))
      }
    }
  }

  def loadEdges(
      edgeStream: ZStream[EdgeRepository, Throwable, Edge]
  ): ZIO[EdgeRepository, Throwable, Unit] = {
    edgeStream.runCollect.either.flatMap {
      case Left(e) => ZIO.fail(e)
      case Right(arr) =>
        if (arr.isEmpty) ZIO.fail(new Exception("Empty edges"))
        else {
          edges = arr.map { edge =>
            val fromNode = nodes(edge.fromId)
            val toNode = nodes(edge.toId)
            (fromNode, toNode, Street(edge.id, edge.name))
          }.toSet
          ZIO.succeed(())
        }
    }
  }

  def loadNodes(
      nodeStream: ZStream[NodeRepository, Throwable, Node]
  ): ZIO[NodeRepository, Throwable, Unit] = {
    nodeStream.runCollect.map(_.toArray).either.flatMap {
      case Right(arr) =>
        arr match {
          case Array() =>
            ZIO.fail(new Exception("Empty nodes"))
          case _ =>
            nodes = arr
              .map(node =>
                node.nodeType match {
                  case 0 =>
                    (
                      node.id,
                      new House(
                        node.id,
                        GeoPoint(node.latitude, node.longitude),
                        node.name
                      )
                    )
                  case _ =>
                    (
                      node.id,
                      new Intersection(
                        node.id,
                        GeoPoint(node.latitude, node.longitude),
                        node.name
                      )
                    )
                }
              )
              .toMap
            ZIO.succeed()
        }
      case Left(e) =>
        ZIO.fail(e)
    }
  }
}

object GraphImpl {
  val graph = new GraphImpl()

  def loadGraph: ZIO[NodeRepository with EdgeRepository, Throwable, Unit] = {
    val loadNodesZIO = graph.loadNodes(NodeRepository.findAll)
    val loadEdgesZIO = graph.loadEdges(EdgeRepository.findAll)
    loadNodesZIO *> loadEdgesZIO
  }

  def findRoute(fromId: Int, toId: Int): List[GeoNode] = {
    graph.findRoute(fromId, toId)
  }
}
