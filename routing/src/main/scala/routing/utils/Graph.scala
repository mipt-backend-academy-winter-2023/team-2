package routing.utils

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import routing.model.Node
import routing.repository.NodeRepository

import zio.ZIO

object Graph {
  private var nodes: ArrayBuffer[Node] = new ArrayBuffer[Node]()
  private var graph: ArrayBuffer[ListBuffer[Int]] = new ArrayBuffer[ListBuffer[Int]]()

  def debug_graph: ArrayBuffer[String] = {
    println("debug_graph()")
    nodes.map(x => x.name)
  }

  def setNodes(data: Array[Node]): ZIO[Any, Throwable, String] = {
    println("!!!!!")
    data.foreach(x => {
      println(x)
      nodes += x
    })
    ZIO.succeed("")
  }

  def setEdges(data: Array[Node]): ZIO[Any, Throwable, String] = {
    println("@@@@@")
    data.foreach(x => {
      println(x)
      //nodes += x
    })
    ZIO.succeed("")
  }

  def astar(fromId: Integer, toId: Integer): ZIO[Any, Throwable, ListBuffer[String]] = {
    var res: ListBuffer[String] = new ListBuffer[String]()
    println(">>>>>>")
    (for {
      nodes <- NodeRepository.findAllNodes.runCollect.map(_.toArray)
    } yield (nodes)).either.map {
      case Right(x) => {
        println(x)
      }
      case Left(_) =>
        println("nah")
    }
    println("<<<<<<<<")
    res += (" aaa " + fromId.toString + toId.toString)
    ZIO.succeed(res)
  }
}
