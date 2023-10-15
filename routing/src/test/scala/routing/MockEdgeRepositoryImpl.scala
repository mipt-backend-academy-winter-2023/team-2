package routing

import zio.ZIO
import zio.stream.ZStream
import routing.repository.EdgeRepository
import routing.model.Edge
import scala.collection.mutable.ListBuffer

final class MockEdgeRepositoryImpl(edges: ListBuffer[Edge])
    extends EdgeRepository {
  def findAllEdges: ZStream[Any, Throwable, Edge] =
    ZStream.fromIterable(edges)
}
