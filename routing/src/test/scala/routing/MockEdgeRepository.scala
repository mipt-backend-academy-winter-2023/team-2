package routing

import repository.EdgeRepository
import routing.model.Edge
import zio.stream.ZStream
final class MockEdgeRepository(edges: List[Edge]) extends EdgeRepository {
  override def findAllEdges: ZStream[Any, Throwable, Edge] =
    ZStream.fromIterable(edges)
}
