package routing.utils

import scala.collection.mutable
import routing.model.{Edge, Node}
import routing.repository.{EdgeRepository, NodeRepository}
import zio.ZIO

import scala.util.control.Breaks.break
object Graph {
  case class NodeInfo(pred: Edge, score: Float)

  private var nodes = mutable.SortedMap[Int, Node]()
  private var adj = mutable.SortedMap[Int, mutable.ArrayBuffer[Edge]]()
  private var info = mutable.SortedMap[Int, NodeInfo]()
  private var parsed_nodes = new mutable.ArrayBuffer[Node]()
  private var parsed_edges = new mutable.ArrayBuffer[Edge]()

  private val eps = 1e-7
  case class State(id: Int) extends Ordered[State] {
    override def compare(that: State): Int = {
      val my_score = info(id).score
      val that_score = info(that.id).score
      if (math.abs(my_score - that_score) < eps) {
        (my_score - that_score).sign.toInt
      } else {
        (id - that.id).sign
      }
    }
  }
  def init(): ZIO[Any, Throwable, Unit] = {
    nodes.clear
    adj.clear
    parsed_edges.foreach(edge => {
      adj(edge.end1) += edge
      adj(edge.end2) += edge
    })
    parsed_nodes.foreach(node => {
      nodes.addOne(node.id, node)
      adj.addOne(node.id, new mutable.ArrayBuffer[Edge]())
    })
    ZIO.succeed()
  }
  def load: ZIO[NodeRepository with EdgeRepository, Throwable, Unit] = {
    nodes.clear
    adj.clear
    for {
       _ <- NodeRepository.findAllNodes.runCollect.map(n =>
        parsed_nodes ++= n.toArray[Node]
      )
      _ <- EdgeRepository.findAllEdges.runCollect.map(e =>
        parsed_edges ++= e.toArray[Edge]
      )
      _ <- init()
    } yield ()
  }

  private def square(x: Float) : Float = x*x
  private def dist(end1: Int, end2: Int): Float = {
    val node1 = nodes(end1)
    val node2 = nodes(end2)
    math.sqrt(square(node1.lat - node2.lat) + square(node1.lon - node2.lon)).toFloat
  }

  def node_info(id: Int): String = {
    val node = nodes(id)
    if (node.is_intersection) {
      s"intersection[id: ${node.id}]"
    } else {
      s"house[id: ${node.id}, name: ${node.name}]"
    }
  }
  def edge_info(edge: Edge): String = {
    s"street[name: ${edge.name}]"
  }
  def shortest_path(from: Int, to: Int): ZIO[Any, Throwable, String] = {
    info.clear
    info.addOne(from, NodeInfo(Edge("dummy", 0, 0), dist(from, to)))
    var q = mutable.SortedSet[State]()
    q.addOne(State(from))
    while (!q.isEmpty) {
      val state = q.firstKey
      q.remove(state)
      val cur = state.id
      if (cur == to) {
        break
      }
      val my_score = info(cur).score
      adj(cur).foreach(edge => {
        val dest = edge.end1 + edge.end2 - cur
        val nscore = my_score + dist(cur, dest) - dist(cur, to) + dist(dest, to)
        val nstate = State(dest)
        if (!info.contains(dest) || info(dest).score > nscore) {
          if (q.contains(nstate)) q.remove(state)
          info.addOne(dest, NodeInfo(edge, nscore))
          q.addOne(nstate)
        }
      })
    }
    var cur = to
    var path: String = node_info(to)
    while (cur != from) {
      val cur_edge = info(cur).pred
      val par = cur_edge.end1 + cur_edge.end2 - cur
      path = node_info(par) + "-" + edge_info(cur_edge) + path
      cur = par
    }
    ZIO.succeed(path)
  }
}
