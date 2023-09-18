package routing.utils

import scala.collection.mutable.{ListBuffer, ArrayBuffer, Map, PriorityQueue, Set}
import routing.model.{Node, Edge}
import routing.repository.{NodeRepository, EdgeRepository}

import zio.ZIO

object Graph {
  private var nodes: ArrayBuffer[Node] = new ArrayBuffer[Node]()
  private var edges: ArrayBuffer[Edge] = new ArrayBuffer[Edge]()
  private var nodesIdToIndex = Map.empty[Int, Int]
  private var nodesForGraph = ArrayBuffer.empty[NodeGraph]
  private var edgesForGraph = ArrayBuffer.empty[EdgeGraph]
  private var graph: ArrayBuffer[ListBuffer[EdgeGraph]] =
    new ArrayBuffer[ListBuffer[EdgeGraph]]()

  class BadNodeIndexException(s: String) extends Exception(s) {}  

  def addNodeToPath(path: String, node: NodeGraph): String =
    if (node.node.category == "0")
      s"- House ${node.node.name} $path"
    else
      s"- Crossroad ${node.node.name} $path"

  def addEdgeToPath(path: String, edge: EdgeGraph): String =
    s"- Edge ${edge.edge.label} $path"

  def loadGraph: ZIO[NodeRepository with EdgeRepository, Throwable, Unit] = {
    nodes.clear
    edges.clear
    for {
      _ <- NodeRepository.findAllNodes.runCollect
        .map(_.toArray)
        .either
        .flatMap {
          case Right(arr) => {
            ZIO.succeed(nodes ++= arr)
          }
          case Left(e) => {
            ZIO.fail(e)
          }
        }
      _ <- EdgeRepository.findAllEdges.runCollect
        .map(_.toArray)
        .either
        .flatMap {
          case Right(arr) => {
            ZIO.succeed(edges ++= arr)
          }
          case Left(e) => {
            ZIO.fail(e)
          }
        }
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
    nodes.zipWithIndex.foreach{ case (node, i) =>
        nodesIdToIndex(node.id) = i
    }
    nodes.foreach(node =>
        nodesForGraph += NodeGraph(node, nodesIdToIndex(node.id), None, 0, 0)
    )
    edges.foreach(edge =>
        edgesForGraph += EdgeGraph(edge, nodesIdToIndex(edge.fromid), nodesIdToIndex(edge.toid))
    )
    nodesForGraph.foreach(_ =>
        graph += new ListBuffer[EdgeGraph]()
    )
    edgesForGraph.foreach(edge => {
        graph(edge.fromIndex) += edge
        graph(edge.toIndex) += EdgeGraph(edge.edge, edge.toIndex, edge.fromIndex)
    })
    // return
    ZIO.succeed(())
  }

  def astar(fromid: Integer, toid: Integer): ZIO[Any, Throwable, String] = {
    // zero all additional data
    nodesForGraph = nodesForGraph.map{x =>
      NodeGraph(x.node, x.index, None, Float.PositiveInfinity, Float.PositiveInfinity)
    }
    // find node with specified id
    val fromIndex = nodesIdToIndex(fromid)
    val toIndex = nodesIdToIndex(toid)
    // do AStar
    var path = ""
    var open_set = PriorityQueue.empty[NodeGraph]
    var closed_set = Set.empty[Int]
    nodesForGraph(fromIndex) = NodeGraph(nodesForGraph(fromIndex).node, nodesForGraph(fromIndex).index, nodesForGraph(fromIndex).prev, 0, 1) //heuristic(start, goal)
    open_set enqueue nodesForGraph(fromIndex)
    while (open_set.length > 0) {
        // get current node and mark it closed
        var current = open_set.dequeue
        closed_set add current.index
        // if reached end
        if (current.index == toIndex) {
            // found finish, restore path
            path = addNodeToPath(path, current)
            while (current.prev != None) {
                current.prev match {
                    case Some(x) => {
                        path = addEdgeToPath(path, x)
                        current = nodesForGraph(x.fromIndex)
                    }
                    case None =>
                }
                path = addNodeToPath(path, current)
            }
            path = s"Route $path"
            open_set.clear
        } else {
            // continue, add neighbours
            graph(current.index).foreach(neighbour => {
                var temp_g_score = current.gscore + neighbour.edge.distance
                if (temp_g_score < nodesForGraph(neighbour.toIndex).gscore){
                    var nextIndex = nodesForGraph(neighbour.toIndex).index
                    nodesForGraph(neighbour.toIndex) = NodeGraph(nodesForGraph(neighbour.toIndex).node, nextIndex, Some(neighbour), temp_g_score, temp_g_score + 1) //heuristic(neighbour, goal)
                    if (!(closed_set contains nextIndex)) {
                        open_set enqueue nodesForGraph(neighbour.toIndex)
                    }
                }
            })
        }
    }
    // return
    ZIO.succeed(path)
  }

  case class NodeGraph(node: Node, index: Int, prev: Option[EdgeGraph], gscore: Float, fscore: Float) extends Ordered[NodeGraph] {
    override def toString: String = s"NodeGraph ($node) ($prev) index: $index gscore: $gscore fscore: $fscore"
    def compare(that: NodeGraph): Int = (fscore - that.fscore).signum
  }

  case class EdgeGraph(edge: Edge, fromIndex: Int, toIndex: Int) {
    override def toString: String = s"EdgeGraph ($edge) $fromIndex -> $toIndex"
  }
}
