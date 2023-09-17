package routing.utils

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import routing.model.Node
import routing.repository.NodeRepository

object Graph {
  private var nodes: ArrayBuffer[Node] = new ArrayBuffer[Node]()
  println("Graph() s")
  NodeRepository.findAllNodes.runCollect.map(e => {println(e); e.collect(r => {println(r); nodes += r})}) //.foreach(nodes += _) //.run//Runtime.default.unsafeRun(NodeRepository.findAllNodes.runCollect)
  //NodeRepository.findAllNodes.tap(x => println(s"before mapping: $x"))
  println(nodes)
  println(NodeRepository.findAllNodes)
  println("Graph() e")

  (for {
    foundUser <- NodeRepository.findAllNodes.runCollect.map(_.toArray)
  } yield (foundUser)).either.map{
    case Right(users) =>
      println(users)
    case Left(_) =>
      println("NAH")
  }

  private var graph: ListBuffer[String] = new ListBuffer[String]()
  println("WWWWWWWWWWWWWWWWWWWWWWWW")

  def debug_graph: ArrayBuffer[String] = {
    println("debug_graph()")
    //NodeRepository.findAllNodes.tap(x => println(s"before mapping: $x"))
    nodes.map(x => x.name)
  }

  def astar(fromId: Int, toId: Int): ListBuffer[String] = {
    var res: ListBuffer[String] = new ListBuffer[String]()
    res += " aaa "
    res
  }
}
