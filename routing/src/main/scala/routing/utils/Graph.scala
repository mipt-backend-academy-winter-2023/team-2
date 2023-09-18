package routing.utils

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import routing.model.{Node, Edge}
import routing.repository.NodeRepository

import zio.ZIO

object Graph {
  private var nodes: ArrayBuffer[Node] = new ArrayBuffer[Node]()
  private var edges: ArrayBuffer[Edge] = new ArrayBuffer[Edge]()
  private var graph: ArrayBuffer[ListBuffer[Edge]] = new ArrayBuffer[ListBuffer[Edge]]()

  def loadGraph: ZIO[NodeRepository, Throwable, Unit] = {
    println("@@@@@@@@@@@ loadGraph")
    NodeRepository.findAllNodes.runCollect.map(_.toArray).either.flatMap{
      case Right(arr) => {
        ZIO.log(arr.length.toString)//foldLeft(""){(x, y) => x + y.name})
      }
      case Left(e) => {
        ZIO.fail(e)
      }
    }
  }

  def debug_graph: ArrayBuffer[String] = {
    // return all data from nodes, edges, graph
    var res = edges.map(x => x.label) ++ nodes.map(x => x.name)
    res += "_"
    graph.foreach(row => {
      row.foreach(edge => {
        res += edge.label
      })
      res += "_"
    })
    res
  }

  def initGraph(newNodes: Array[Node], newEdges: Array[Edge]): ZIO[Any, Throwable, String] = {
    println("!!!!! Graph initGraph !!!!!")
    // clear data
    nodes.clear
    edges.clear
    graph.clear
    // load from arguments
    newNodes.foreach(node => {
      println(node)
      nodes += node
    })
    newEdges.foreach(edge => {
      println(edge)
      edges += edge
    })
    // construct graph
    nodes.foreach(_ => {
      graph += new ListBuffer[Edge]()
    })
    edges.foreach(edge => {
      graph(edge.fromid - 1) += edge
      graph(edge.toid - 1) += edge // unoriented graph
    })
    // return
    ZIO.succeed("")
  }

  def astar(fromId: Integer, toId: Integer): ZIO[Any, Throwable, ListBuffer[String]] = {
    // debug
    /*println(">>>>>>")
    (for {
      nodes <- NodeRepository.findAllNodes.runCollect.map(_.toArray)
    } yield (nodes)).either.map {
      case Right(x) => {
        println(x)
      }
      case Left(_) =>
        println("nah")
    }
    println("<<<<<<<<")*/
    // find path
    var res: ListBuffer[String] = new ListBuffer[String]()
    res ++= debug_graph
    res += fromId.toString += toId.toString
    // return
    ZIO.succeed(res)
  }
}
