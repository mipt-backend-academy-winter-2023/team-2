package routing.repository

import routing.model.Node
import zio.{ZIO, ZLayer}
import zio.sql.ConnectionPool
import zio.stream.ZStream

final class NodeRepositoryImpl(pool: ConnectionPool)
    extends PostgresTableDescription
    with NodeRepository {
  val driverLayer: ZLayer[Any, Nothing, SqlDriver] =
    ZLayer.make[SqlDriver](SqlDriver.live, ZLayer.succeed(pool))

  override def findAllNodes: ZStream[Any, Throwable, Node] = {
    println("NodeRepositoryImpl findAllNodes")
    val selectAll = select(category, name, location)
      .from(nodes)
      .where(true)

    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute findAllNodes is ${renderRead(selectAll)}")
    ) *> execute(selectAll.to((Node.apply _).tupled))
      .provideSomeLayer(driverLayer)
  }

  override def findNodeByNodename(node: Node): ZStream[Any, Throwable, Node] = {
    val selectAll = select(category, name, location)
      .from(nodes)
      .where(name === node.name)

    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute findNodeByNodename is ${renderRead(selectAll)}")
    ) *> execute(selectAll.to((Node.apply _).tupled))
      .provideSomeLayer(driverLayer)
  }
}

object NodeRepositoryImpl {
  val live: ZLayer[ConnectionPool, Throwable, NodeRepository] =
    ZLayer.fromFunction(new NodeRepositoryImpl(_))
}
