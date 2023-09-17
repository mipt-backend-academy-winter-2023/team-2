package routing.repository

import routing.model.Edge
import zio.{Task, ZIO}
import zio.stream.ZStream

trait EdgeRepository {
  def findAllEdges: ZStream[Any, Throwable, Edge]
}

object EdgeRepository {
  def findAllEdges: ZStream[EdgeRepository, Throwable, Edge] = {
    println("NodeRepository findAllEdges")
    ZStream.serviceWithStream[EdgeRepository](_.findAllEdges)
 }
}
