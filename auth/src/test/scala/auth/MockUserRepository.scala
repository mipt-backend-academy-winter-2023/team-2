package auth

import auth.model.User
import auth.repository.UserRepository
import zio.ZIO
import zio.stream.ZStream

import scala.collection.mutable
import scala.reflect.runtime.universe.Try
final class MockUserRepository(users: mutable.Map[String, User])
    extends UserRepository {
  override def add(user: User): ZIO[UserRepository, Throwable, Unit] = {
    if (!users.contains(user.username)) {
      users += user.username -> user
      ZIO.succeed()
    } else {
      ZIO.fail(new Exception("Username is already taken"))
    }
  }

  override def findUserByUsername(
      user: User
  ): ZStream[UserRepository, Throwable, User] = {
    try {
      ZStream.fromZIO(ZIO.succeed(users(user.username)))
    } catch {
      case _ => ZStream.empty
    }
  }

  override def findUser(user: User): ZStream[Any, Throwable, User] = {
    try {
      if (users(user.username).password != user.password) {
        throw new Exception("Wrong password")
      }
      ZStream.fromZIO(ZIO.succeed(user))
    } catch {
      case _ => ZStream.empty
    }
  }
}
