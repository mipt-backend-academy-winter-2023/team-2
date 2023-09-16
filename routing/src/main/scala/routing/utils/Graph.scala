package routing.utils

import scala.collection.mutable.ListBuffer

object Graph {
  private var graph: ListBuffer[String] = new ListBuffer[String]()
  graph += "yeah"
  graph += "graph"

  def debug_graph: ListBuffer[String] = {
    graph
  }

  def astar(fromId: Int, toId: Int): ListBuffer[String] = {
    var res: ListBuffer[String] = new ListBuffer[String]()
    res += " aaa "
    res
  }
}
