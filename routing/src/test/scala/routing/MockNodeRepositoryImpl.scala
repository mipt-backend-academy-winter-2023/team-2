package routing

import zio.ZIO
import zio.stream.ZStream
import routing.repository.NodeRepository
import routing.model.Node
import scala.collection.mutable.ListBuffer

final class MockNodeRepositoryImpl(nodes: ListBuffer[Node])
    extends NodeRepository {
  override def findAllNodes: ZStream[Any, Throwable, Node] =
    ZStream.fromIterable(nodes)
}
