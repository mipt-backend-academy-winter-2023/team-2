package auth.repository

import auth.model.User
import zio.{Task, ZIO}
import zio.stream.ZStream

trait UserRepository {
  def findAll(): ZStream[UserRepository, Throwable, User]//ZStream[Any, Throwable, User]

  def add(user: User): ZIO[UserRepository, Throwable, Unit]
}

object UserRepository {
  def findAll(): ZStream[UserRepository, Throwable, User] =
    ZStream.serviceWithStream[UserRepository](_.findAll())

  def add(user: User): ZIO[UserRepository, Throwable, Unit] =
    ZIO.serviceWithZIO[UserRepository](_.add(user))
}

