package auth.repo

import auth.model.User
import zio.{Task, ZIO}
import zio.stream.ZStream

trait UsersRepository {
  def find(user: User): ZStream[UsersRepository, Throwable, User]

  def add(user: User): ZIO[UsersRepository, Throwable, Unit]
}
object UsersRepository {
  def find(user: User): ZStream[UsersRepository, Throwable, User] = ZStream.serviceWithStream[UsersRepository](_.find(user))
  def add(user: User): ZIO[UsersRepository, Throwable, Unit] = ZIO.serviceWithZIO[UsersRepository](_.add(user))
}


