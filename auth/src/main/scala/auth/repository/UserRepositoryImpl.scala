package auth.repository

import auth.model.User
import auth.security.PasswordEncryptor
import zio.{ZIO, ZLayer}
import zio.sql.ConnectionPool
import zio.stream.ZStream

final class UserRepositoryImpl(pool: ConnectionPool) extends PostgresTableDescription with UserRepository {
  val driverLayer: ZLayer[Any, Nothing, SqlDriver] =
    ZLayer.make[SqlDriver](SqlDriver.live, ZLayer.succeed(pool))

  override def findByCredentials(user: User): ZStream[Any, Throwable, User] = {
    val selectAll = select(username, password)
                      .from(userTable)
                      .where(username === user.username && password === PasswordEncryptor.encrypt(user.password))
    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute findAll is ${renderRead(selectAll)}")
    ) *> execute(selectAll.to((User.apply _).tupled)).provideSomeLayer(driverLayer)
  }

  override def add(user: User): ZIO[UserRepository, Throwable, Unit] = {
    findByCredentials(user).runCollect.map(_.toArray).either.flatMap {
      case Right(arr) => arr match {
        case Array() => {
          val query =
            insertInto(userTable)(username, password)
              .values(
                (
                  user.username,
                  PasswordEncryptor.encrypt(user.password)
                )
              )
            ZIO.logInfo(s"Query to insert user is ${renderInsert(query)}") *>
              execute(query).provideSomeLayer(driverLayer).unit
        }
        case _ => ZIO.fail(new Exception("User exists"))
      }
      case Left(_) => ZIO.fail(new Exception("Error"))
    }
  }
}

object UserRepositoryImpl {
  val live: ZLayer[ConnectionPool, Throwable, UserRepository] = ZLayer.fromFunction(new UserRepositoryImpl(_))
}

