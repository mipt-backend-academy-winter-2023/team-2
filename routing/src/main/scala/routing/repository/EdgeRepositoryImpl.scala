package routing.repository

import routing.model.Edge
import zio.sql.ConnectionPool
import zio.stream.ZStream
import zio.{ZIO, ZLayer}

final class EdgeRepositoryImpl(pool: ConnectionPool)
    extends EdgeTableDescription
    with EdgeRepository {
  val driverLayer: ZLayer[Any, Nothing, SqlDriver] =
    ZLayer.make[SqlDriver](SqlDriver.live, ZLayer.succeed(pool))

  override def findAll: ZStream[Any, Throwable, Edge] = {
    val selectAll = select(fId, fName, fFromId, ftoId)
      .from(edgesTable)
    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute findAll is ${renderRead(selectAll)}")
    ) *> execute(selectAll.to((Edge.apply _).tupled))
      .provideSomeLayer(driverLayer)
  }
}

object EdgeRepositoryImpl {
  val live: ZLayer[ConnectionPool, Throwable, EdgeRepository] =
    ZLayer.fromFunction(new EdgeRepositoryImpl(_))
}
