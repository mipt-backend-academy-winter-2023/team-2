package routing

import model.Node
import repository.NodeRepository
import zio.stream.ZStream
final class MockNodeRepository(nodes: List[Node]) extends NodeRepository {
  override def findAllNodes: ZStream[Any, Throwable, Node] =
    ZStream.fromIterable(nodes)
}
