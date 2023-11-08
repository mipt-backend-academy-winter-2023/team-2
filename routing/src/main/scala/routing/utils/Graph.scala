package routing.utils

import scala.collection.mutable.{
  ArrayBuffer,
  ListBuffer,
  Map,
  PriorityQueue,
  Set
}
import routing.model.{Edge, JamValue, Node}
import routing.repository.{EdgeRepository, NodeRepository}
import zio.ZIO

import scala.collection.mutable

case class GraphPath(var nodes: ListBuffer[Node], var edges: ListBuffer[Edge]) {
  def addNode(node: Node): Unit = nodes += node
  def addEdge(edge: Edge): Unit = edges += edge
  def endPath(): Unit = {
    nodes = nodes.reverse
    edges = edges.reverse
  }
}

object GraphPath {
  def edgeToString(edge: Edge): String = s"Edge {name: ${edge.label}}"

  def nodeToString(node: Node, jam: JamValue): String =
    if (node.category == "0")
      s"House {name: ${node.name}, jam_value: ${jam.jam_value}}"
    else
      s"Crossroad {name: ${node.name}, jam_value: ${jam.jam_value}"

  def pathToString(
      nodes: List[Node],
      edges: List[Edge],
      jams: List[JamValue]
  ): String = {
    val start = nodeToString(nodes.head, jams.head)
    val end = nodes
      .zip(jams)
      .drop(1)
      .zip(edges)
      .map(state => {
        val ((node, jam), edge) = state
        s" - ${edgeToString(edge)} - ${nodeToString(node, jam)}"
      })
    start + end.mkString
  }
}

object Graph {
  private var nodes: ArrayBuffer[Node] = new ArrayBuffer[Node]()
  private var edges: ArrayBuffer[Edge] = new ArrayBuffer[Edge]()
  private var nodesIdToIndex = Map.empty[Int, Int]
  private var nodesForGraph = ArrayBuffer.empty[NodeGraph]
  private var edgesForGraph = ArrayBuffer.empty[EdgeGraph]
  private var graph: ArrayBuffer[ListBuffer[EdgeGraph]] =
    new ArrayBuffer[ListBuffer[EdgeGraph]]()

  class BadNodeIndexException(s: String) extends Exception(s) {}

  def loadGraph: ZIO[NodeRepository with EdgeRepository, Throwable, Unit] = {
    nodes.clear
    edges.clear
    for {
      _ <- NodeRepository.findAllNodes.runCollect.map(n =>
        nodes ++= n.toArray[Node]
      )
      _ <- EdgeRepository.findAllEdges.runCollect.map(e =>
        edges ++= e.toArray[Edge]
      )
      _ <- initGraph
    } yield ()
  }

  override def toString: String = {
    // return all data from nodes, edges, graph
    "Nodes: " +
      nodes.foldLeft("") { (old, value) => old + value.toString + " " } +
      "\nEdges: " +
      edges.foldLeft("") { (old, value) => old + value.toString + " " } +
      "\nGraph connections:\n" +
      graph.foldLeft("") { (oldA, valueA) =>
        oldA + valueA.foldLeft("") { (old, value) =>
          old + s"(${value.toIndex.toString},${value.fromIndex.toString}) "
        } + "\n"
      }
  }

  def initGraph: ZIO[Any, Throwable, Unit] = {
    // clear data
    nodesIdToIndex.clear
    nodesForGraph.clear
    edgesForGraph.clear
    graph.clear
    // construct graph
    nodes.zipWithIndex.foreach { case (node, i) =>
      nodesIdToIndex(node.id) = i
    }
    nodes.foreach(node =>
      nodesForGraph += NodeGraph(node, nodesIdToIndex(node.id), None, 0, 0)
    )
    edges.foreach(edge =>
      edgesForGraph += EdgeGraph(
        edge,
        nodesIdToIndex(edge.fromid),
        nodesIdToIndex(edge.toid)
      )
    )
    nodesForGraph.foreach(_ => graph += new ListBuffer[EdgeGraph]())
    edgesForGraph.foreach(edge => {
      graph(edge.fromIndex) += edge
      graph(edge.toIndex) += EdgeGraph(edge.edge, edge.toIndex, edge.fromIndex)
    })
    // return
    ZIO.succeed(())
  }

  def astar(fromid: Integer, toid: Integer): ZIO[Any, Throwable, GraphPath] = {
    // zero all additional data
    nodesForGraph = nodesForGraph.map { x =>
      NodeGraph(
        x.node,
        x.index,
        None,
        Float.PositiveInfinity,
        Float.PositiveInfinity
      )
    }
    if (!nodesIdToIndex.contains(fromid) || !nodesIdToIndex.contains(toid)) {
      return ZIO.fail(new Exception("Endpoint doesnt exists"))
    }
    // find node with specified id
    val fromIndex = nodesIdToIndex(fromid)
    val toIndex = nodesIdToIndex(toid)
    // do AStar
    var path = GraphPath(ListBuffer.empty, ListBuffer.empty)
    var openSet = PriorityQueue.empty[NodeGraph]
    var closedSet = Set.empty[Int]
    nodesForGraph(fromIndex) = NodeGraph(
      nodesForGraph(fromIndex).node,
      nodesForGraph(fromIndex).index,
      nodesForGraph(fromIndex).prev,
      0,
      1
    )
    openSet enqueue nodesForGraph(fromIndex)
    var wasFound = false
    while (openSet.length > 0) {
      // get current node and mark it closed
      var current = openSet.dequeue
      closedSet add current.index
      // if reached end
      if (current.index == toIndex) {
        wasFound = true
        // found finish, restore path
        path.addNode(current.node)
        while (current.prev != None) {
          current.prev match {
            case Some(x) => {
              path.addEdge(x.edge)
              current = nodesForGraph(x.fromIndex)
            }
            case None =>
          }
          path.addNode(current.node)
        }
        openSet.clear
      } else {
        // continue, add neighbours
        graph(current.index).foreach(neighbour => {
          var tempGScore = current.gscore + neighbour.edge.distance
          if (tempGScore < nodesForGraph(neighbour.toIndex).gscore) {
            var nextIndex = nodesForGraph(neighbour.toIndex).index
            nodesForGraph(neighbour.toIndex) = NodeGraph(
              nodesForGraph(neighbour.toIndex).node,
              nextIndex,
              Some(neighbour),
              tempGScore,
              tempGScore + 1
            ) //heuristic(neighbour, goal)
            if (!(closedSet contains nextIndex)) {
              openSet enqueue nodesForGraph(neighbour.toIndex)
            }
          }
        })
      }
    }
    path.endPath()
    if (wasFound)
      ZIO.succeed(path)
    else
      ZIO.fail(new Exception("There is no path"))
  }

  case class NodeGraph(
      node: Node,
      index: Int,
      prev: Option[EdgeGraph],
      gscore: Float,
      fscore: Float
  ) extends Ordered[NodeGraph] {
    override def toString: String =
      s"NodeGraph ($node) ($prev) index: $index gscore: $gscore fscore: $fscore"
    def compare(that: NodeGraph): Int = (fscore - that.fscore).signum
  }

  case class EdgeGraph(edge: Edge, fromIndex: Int, toIndex: Int) {
    override def toString: String = s"EdgeGraph ($edge) $fromIndex -> $toIndex"
  }
}
