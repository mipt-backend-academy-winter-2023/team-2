package routing.repository

import routing.model.Node
import zio.{Task, ZIO}
import zio.stream.ZStream

trait NodeRepository {
  def findAllNodes: ZStream[Any, Throwable, Node]

  def findNodeByNodename(node: Node): ZStream[NodeRepository, Throwable, Node]
}

object NodeRepository {
  def findAllNodes: ZStream[NodeRepository, Throwable, Node] = {
    ZStream.serviceWithStream[NodeRepository](_.findAllNodes)
 }

  def findNodeByNodename(node: Node): ZStream[NodeRepository, Throwable, Node] =
    ZStream.serviceWithStream[NodeRepository](_.findNodeByNodename(node))
}
