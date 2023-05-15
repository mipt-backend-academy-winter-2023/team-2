package auth.repo

import auth.model.User
import zio.sql.ConnectionPool
import zio.{Task, ZIO, ZLayer}
import zio.stream.ZStream

final class UsersRepositoryImpl(pool: ConnectionPool) extends PostgresTableDescription with UsersRepository {
  val driverLayer: ZLayer[Any, Nothing, SqlDriver] =
    ZLayer
      .make[SqlDriver](
        SqlDriver.live,
        ZLayer.succeed(pool)
      )

  private def hash(s: String): String = s

  private def findUsername(userName : String): ZStream[Any, Throwable, User] = {
    val selectUsername = select(username, password)
      .from(tableUsers)
      .where(username === userName)

    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute find is ${renderRead(selectUsername)}")
    ) *> execute(selectUsername.to((User.apply _).tupled))
      .provideSomeLayer(driverLayer)
  }
  override def find(user: User): ZStream[Any, Throwable, User] = {
    val selectUser = select(username, password)
      .from(tableUsers)
      .where(username === user.username && password === hash(user.password))

    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute find is ${renderRead(selectUser)}")
    ) *> execute(selectUser.to((User.apply _).tupled))
      .provideSomeLayer(driverLayer)
  }

  override def add(user: User): ZIO[Any, Throwable, Unit] = {

    val insertUser = insertInto(tableUsers)(username, password)
      .values(
        (
          user.username,
          hash(user.password)
        )
      )

    findUsername(user.username).runCollect.map(_.toArray).either.map {
      //case Right(Array.empty) => ZIO.fail(new Exception("Username is taken"))
      case Left(_) => ZIO.fail(new Exception("Bad Request"))
      case Right(arr) =>
        if (arr.isEmpty) {
          ZIO.fail(new Exception("Username is taken"))
        } else {
          ZIO.logInfo(s"Query to insert user is ${renderInsert(insertUser)}") *>
            execute(insertUser).provideSomeLayer(driverLayer).unit
        }
    }
  }
}

object UserRepositoryImpl {
  val live: ZLayer[ConnectionPool, Throwable, UsersRepository] = ZLayer.fromFunction(new UsersRepositoryImpl(_))
}
