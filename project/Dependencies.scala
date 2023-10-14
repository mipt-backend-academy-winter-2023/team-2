import Libs._
import sbt._

trait Dependencies {
  def dependencies: Seq[ModuleID]
}

object Dependencies {

  object Auth extends Dependencies {
    override def dependencies: Seq[ModuleID] =
      Seq(test, zio, pureconfig, flyway, circe, pdiJwt).flatten
  }

  object Routing extends Dependencies {
    override def dependencies: Seq[ModuleID] = Seq(test, zio, pureconfig, flyway, circe).flatten
  }

  object Helper extends Dependencies {
    override def dependencies: Seq[ModuleID] = Seq(test, zio, pureconfig).flatten
  }

  object Images extends Dependencies {
    override def dependencies: Seq[ModuleID] =
      Seq(test, zio, pureconfig, circe).flatten
  }
}
