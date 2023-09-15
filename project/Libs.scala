import sbt._

object V {
  val zio = "2.0.13"
  val zioHttp = "0.0.5"
  val zio_sql = "0.1.2"

  val pureconfig = "0.17.3"
  val flyway = "9.16.0"
  val circe_version = "0.14.1"
  val pdiJwt = "9.2.0"
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

  val circe: List[ModuleID] = List(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % V.circe_version)

  val pdiJwt: List[ModuleID] = List(
    "com.github.jwt-scala" %% "jwt-core" % V.pdiJwt
  )

}
