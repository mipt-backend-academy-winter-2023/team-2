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

  override def findNode(node: Node): ZStream[Any, Throwable, Node] = {
    val selectAll = select(category, name, location)
      .from(nodes)
      .where(
        category == node.category &&
        name == node.name &&
        location == node.location
      )

    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute findNode is ${renderRead(selectAll)}")
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

  override def add(node: Node): ZIO[NodeRepository, Throwable, Unit] = {
    findNodeByNodename(node).runCollect.map(_.toArray).either.flatMap {
      case Right(value) =>
        value match {
          case Array() =>
            val query = insertInto(nodes)(category, name, location)
              .values((node.category, node.name, node.location))

            ZIO.logInfo(s"Query to execute add is ${renderInsert(query)}") *>
              execute(query)
                .provideSomeLayer(driverLayer)
                .unit
          case _ => ZIO.fail(new Exception("Node already exists"))
        }
      case Left(_) => ZIO.fail(new Exception("Adding error"))
    }
  }
}

object NodeRepositoryImpl {
  val live: ZLayer[ConnectionPool, Throwable, NodeRepository] =
    ZLayer.fromFunction(new NodeRepositoryImpl(_))
}