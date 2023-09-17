package auth.repository

import auth.model.User
import zio.{Task, ZIO}
import zio.stream.ZStream

trait UserRepository {
  def findUser(user: User): ZStream[Any, Throwable, User]

  def findUserByUsername(user: User): ZStream[UserRepository, Throwable, User]

  def add(user: User): ZIO[UserRepository, Throwable, Unit]
}

object UserRepository {
  def findUser(user: User): ZStream[UserRepository, Throwable, User] = {
    println("UserRepository findUser")
    ZStream.serviceWithStream[UserRepository](_.findUser(user))
  }

  def findUserByUsername(user: User): ZStream[UserRepository, Throwable, User] =
    ZStream.serviceWithStream[UserRepository](_.findUserByUsername(user))

  def add(user: User): ZIO[UserRepository, Throwable, Unit] =
    ZIO.serviceWithZIO[UserRepository](_.add(user))
}
