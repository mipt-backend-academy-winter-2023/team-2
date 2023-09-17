package routing.repository

import routing.model.Node
import zio.stream.ZStream

trait NodeRepository {
  def findAll: ZStream[Any, Throwable, Node]
}

object NodeRepository {
  def findAll: ZStream[NodeRepository, Throwable, Node] =
    ZStream.serviceWithStream[NodeRepository](_.findAll)
}
