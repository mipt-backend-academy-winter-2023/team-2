package auth

import zio.ZIO
import zio.stream.ZStream
import auth.repository.UserRepository
import auth.model.User
import scala.collection.mutable.ListBuffer

final class MockUserRepositoryImpl(users: ListBuffer[User])
    extends UserRepository {
  def add(user: User): ZIO[UserRepository, Throwable, Unit] = {
    if (users.forall(_.username != user.username)) {
      users += user
      ZIO.succeed()
    } else {
      ZIO.fail(new Exception("User already exists"))
    }
  }
  def findUser(user: User): ZStream[Any, Throwable, User] =
    users.find(x =>
      x.username == user.username && x.password == user.password
    ) match {
      case None            => ZStream.empty
      case Some(foundUser) => ZStream.fromZIO(ZIO.succeed(foundUser))
    }
  def findUserByUsername(user: User): ZStream[UserRepository, Throwable, User] =
    ZStream.fromZIO(ZIO.succeed(new User("", "")))
}
