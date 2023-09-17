package routing.repository

import routing.model.Node
import zio.sql.ConnectionPool
import zio.stream.ZStream
import zio.{ZIO, ZLayer}

final class NodeRepositoryImpl(pool: ConnectionPool)
    extends NodeTableDescription
    with NodeRepository {

  val driverLayer: ZLayer[Any, Nothing, SqlDriver] =
    ZLayer.make[SqlDriver](SqlDriver.live, ZLayer.succeed(pool))

  override def findAll: ZStream[Any, Throwable, Node] = {
    val selectAll = select(fId, fNodeType, fName, fLatitude, fLongitude)
      .from(nodesTable)
      .where(fId > 0)
    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute findAll is ${renderRead(selectAll)}")
    ) *> execute(selectAll.to((Node.apply _).tupled))
      .provideSomeLayer(driverLayer)
  }
}

object NodeRepositoryImpl {
  val live: ZLayer[ConnectionPool, Throwable, NodeRepository] =
    ZLayer.fromFunction(new NodeRepositoryImpl(_))
}
