package routing.repository

import routing.model.Edge
import zio.stream.ZStream

trait EdgeRepository {
  def findAll: ZStream[Any, Throwable, Edge]
}

object EdgeRepository {
  def findAll: ZStream[EdgeRepository, Throwable, Edge] =
    ZStream.serviceWithStream[EdgeRepository](_.findAll)
}
