import sbt._

object V {
  val zio = "2.0.13"
  val zioHttp = "0.0.5"
  val flyway = "9.16.0"
  val pureconfig = "0.17.3"
  val zio_sql = "0.1.2"
  val circle_version = "0.14.1"
  val io_jwt_version = "0.9.1"
}


object Libs {

  val zio: List[ModuleID] = List(
    "dev.zio" %% "zio" % V.zio,
    "dev.zio" %% "zio-http" % V.zioHttp,
    "dev.zio" %% "zio-sql-postgres" % V.zio_sql
  )

  val flyway: List[ModuleID] = List(
    "org.flywaydb" % "flyway-core" % V.flyway
  )

  val pureconfig: List[ModuleID] = List(
    "com.github.pureconfig" %% "pureconfig" % V.pureconfig
  )

  val circle: List[ModuleID] = List(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % V.circle_version)

  val io_jwt: List[ModuleID] = List (
    "io.jsonwebtoken" % "jjwt"
  ).map(_ % V.io_jwt_version)
}
