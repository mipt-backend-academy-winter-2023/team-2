package routing.repository

import routing.model.Node
import zio.{Task, ZIO}
import zio.stream.ZStream

trait NodeRepository {
  def findNode(node: Node): ZStream[Any, Throwable, Node]

  def findNodeByNodename(node: Node): ZStream[NodeRepository, Throwable, Node]

  def add(node: Node): ZIO[NodeRepository, Throwable, Unit]
}

object NodeRepository {
  def findNode(node: Node): ZStream[NodeRepository, Throwable, Node] =
    ZStream.serviceWithStream[NodeRepository](_.findNode(node))

  def findNodeByNodename(node: Node): ZStream[NodeRepository, Throwable, Node] =
    ZStream.serviceWithStream[NodeRepository](_.findNodeByNodename(node))

  def add(node: Node): ZIO[NodeRepository, Throwable, Unit] =
    ZIO.serviceWithZIO[NodeRepository](_.add(node))
}
