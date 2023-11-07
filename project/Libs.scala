import sbt._

object V {
  val zio = "2.0.13"
  val zioHttp = "0.0.5"
  val zio_sql = "0.1.2"

  val test = "3.2.15"
  val zioTest = "2.0.15"
  val zioTestMock = "1.0.0-RC9"

  val pureconfig = "0.17.3"
  val flyway = "9.16.0"
  val circe_version = "0.14.1"
  val pdiJwt = "9.2.0"

  val sttp = "3.9.0"
  val rezilience = "0.9.3"
}

object Libs {

  val zio: List[ModuleID] = List(
    "dev.zio" %% "zio" % V.zio,
    "dev.zio" %% "zio-http" % V.zioHttp,
    "dev.zio" %% "zio-sql-postgres" % V.zio_sql
  )

  val test: List[ModuleID] = List(
    "org.scalatest" %% "scalatest" % V.test % Test,
    "dev.zio" %% "zio-test" % V.zioTest % Test,
    "dev.zio" %% "zio-test-sbt" % V.zioTest % Test,
    "dev.zio" %% "zio-test-magnolia" % V.zioTest % Test,
    "dev.zio" %% "zio-mock" % V.zioTestMock % Test
  )

  val flyway: List[ModuleID] = List(
    "org.flywaydb" % "flyway-core" % V.flyway
  )

  val pureconfig: List[ModuleID] = List(
    "com.github.pureconfig" %% "pureconfig" % V.pureconfig
  )

  val circe: List[ModuleID] = List(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % V.circe_version)

  val pdiJwt: List[ModuleID] = List(
    "com.github.jwt-scala" %% "jwt-core" % V.pdiJwt
  )

  val sttp: List[ModuleID] = List(
    "com.softwaremill.sttp.client3" %% "zio" % V.sttp,
    "com.softwaremill.sttp.client3" %% "circe" % V.sttp,
    "com.softwaremill.sttp.client3" %% "core" % V.sttp
  )

  val rezilience = List(
    "nl.vroste" %% "rezilience" % V.rezilience
  )

}
