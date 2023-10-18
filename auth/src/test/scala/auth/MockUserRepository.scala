package auth

import zio.ZIO
import zio.stream.ZStream

import auth.model.User
import auth.repository.UserRepository

final class MockUserRepository(users: collection.mutable.ListBuffer[User])
    extends UserRepository {
  override def add(user: User): ZIO[UserRepository, Throwable, Unit] =
    if (users.exists(_.username == user.username))
      ZIO.fail(new Exception("User already exists"))
    else
      ZIO.succeed(users += user)

  override def findUser(user: User): ZStream[Any, Throwable, User] =
    users
      .find(u => u.username == user.username && u.password == user.password)
      .map(ZStream.succeed(_))
      .getOrElse(ZStream.empty)

  override def findUserByUsername(
      user: User
  ): ZStream[UserRepository, Throwable, User] =
    users.find(_.username == user.username) match {
      case None            => ZStream.empty
      case Some(foundUser) => ZStream.fromZIO(ZIO.succeed(foundUser))
    }
}
