package routing.repository

import routing.model.Edge
import zio.{ZIO, ZLayer}
import zio.sql.ConnectionPool
import zio.stream.ZStream

final class EdgeRepositoryImpl(pool: ConnectionPool)
    extends PostgresTableDescription
    with EdgeRepository {
  val driverLayer: ZLayer[Any, Nothing, SqlDriver] =
    ZLayer.make[SqlDriver](SqlDriver.live, ZLayer.succeed(pool))

  override def findAllEdges: ZStream[Any, Throwable, Edge] = {
    println("DEBUG EdgeRepositoryImpl findAllEdges")
    val selectAll = select(label, fromid, toid, distance)
      .from(edges)
      .where(true)

    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute findAllEdges is ${renderRead(selectAll)}")
    ) *> execute(selectAll.to((Edge.apply _).tupled))
      .provideSomeLayer(driverLayer)
  }
}

object EdgeRepositoryImpl {
  val live: ZLayer[ConnectionPool, Throwable, EdgeRepository] =
    ZLayer.fromFunction(new EdgeRepositoryImpl(_))
}
