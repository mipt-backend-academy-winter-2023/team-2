package auth.repository

import auth.model.User
import zio.{ZIO, ZLayer}
import zio.sql.ConnectionPool
import zio.stream.ZStream

final class UserRepositoryImpl(pool: ConnectionPool) extends PostgresTableDescription with UserRepository {
  val driverLayer: ZLayer[Any, Nothing, SqlDriver] =
    ZLayer.make[SqlDriver](SqlDriver.live, ZLayer.succeed(pool))

  override def findByCredentials(user: User): ZStream[Any, Throwable, User] = {
    val selectAll = select(username, password).from(userTable).where(username === user.username).where(password === user.password)

    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute findAll is ${renderRead(selectAll)}")
    ) *> execute(selectAll.to((User.apply _).tupled)).provideSomeLayer(driverLayer)
  }

  override def add(user: User): ZIO[Any, Throwable, Unit] = {
    val query =
      insertInto(userTable)(username, password)
        .values(
          (
            user.username,
            user.password
          )
        )

    ZIO.logInfo(s"Query to insert customer is ${renderInsert(query)}") *>
      execute(query).provideSomeLayer(driverLayer).unit
  }
}

object UserRepositoryImpl {
  val live: ZLayer[ConnectionPool, Throwable, UserRepository] = ZLayer.fromFunction(new UserRepositoryImpl(_))
}

