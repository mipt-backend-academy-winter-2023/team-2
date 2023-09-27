package auth.repository

import auth.utils.PasswordUtils
import auth.model.User
import zio.{ZIO, ZLayer}
import zio.sql.ConnectionPool
import zio.stream.ZStream

final class UserRepositoryImpl(pool: ConnectionPool)
    extends PostgresTableDescription
    with UserRepository {
  val driverLayer: ZLayer[Any, Nothing, SqlDriver] =
    ZLayer.make[SqlDriver](SqlDriver.live, ZLayer.succeed(pool))

  override def findUser(user: User): ZStream[Any, Throwable, User] = {
    val selectAll = select(username, password)
      .from(users)
      .where(
        username === user.username && password
          === PasswordUtils.encode(user.password)
      )

    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute findUser is ${renderRead(selectAll)}")
    ) *> execute(selectAll.to((User.apply _).tupled))
      .provideSomeLayer(driverLayer)
  }

  override def findUserByUsername(user: User): ZStream[Any, Throwable, User] = {
    val selectAll = select(username, password)
      .from(users)
      .where(username === user.username)

    ZStream.fromZIO(
      ZIO.logInfo(
        s"Query to execute findUserByUsername is ${renderRead(selectAll)}"
      )
    ) *> execute(selectAll.to((User.apply _).tupled))
      .provideSomeLayer(driverLayer)
  }

  override def add(user: User): ZIO[UserRepository, Throwable, Unit] = {
    findUserByUsername(user).runCollect.map(_.toArray).either.flatMap {
      case Right(value) =>
        value match {
          case Array() =>
            val query = insertInto(users)(username, password)
              .values((user.username, PasswordUtils.encode(user.password)))

            ZIO.logInfo(s"Query to execute add is ${renderInsert(query)}") *>
              execute(query)
                .provideSomeLayer(driverLayer)
                .unit
          case _ => ZIO.fail(new Exception("User already exists"))
        }
      case Left(_) => ZIO.fail(new Exception("Adding error"))
    }
  }
}

object UserRepositoryImpl {
  val live: ZLayer[ConnectionPool, Throwable, UserRepository] =
    ZLayer.fromFunction(new UserRepositoryImpl(_))
}
