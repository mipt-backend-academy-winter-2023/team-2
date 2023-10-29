import Libs._
import sbt._

trait Dependencies {
  def dependencies: Seq[ModuleID]
}

object Dependencies {

  object Auth extends Dependencies {
    override def dependencies: Seq[ModuleID] =
      Seq(zio, test, pureconfig, flyway, circe, pdiJwt).flatten
  }

  object Routing extends Dependencies {
    override def dependencies: Seq[ModuleID] =
      Seq(zio, test, pureconfig, flyway, circe).flatten
  }

  object Helper extends Dependencies {
    override def dependencies: Seq[ModuleID] =
      Seq(zio, test, pureconfig).flatten
  }

  object Images extends Dependencies {
    override def dependencies: Seq[ModuleID] =
      Seq(zio, test, pureconfig, circe).flatten
  }

  object Utils extends Dependencies {
    override def dependencies: Seq[ModuleID] =
      Seq(zio, test, pureconfig).flatten
  }
}
