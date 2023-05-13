package auth.flyway

import auth.config.DbConfig
import org.flywaydb.core.Flyway
import zio.{UIO, ZIO, ZLayer}

object FlywayAdapter {
  trait Service {
    def migration: UIO[Unit]
  }

  val live: ZLayer[DbConfig, Nothing, FlywayAdapterImpl] = ZLayer.fromFunction(new FlywayAdapterImpl(_))
}

class FlywayAdapterImpl(dbConfig: DbConfig) extends FlywayAdapter.Service {
  val flyway: UIO[Flyway] = ZIO.succeed(
    Flyway
      .configure()
      //.validateMigrationNaming(true)
      .dataSource(dbConfig.url, dbConfig.user, dbConfig.password)
  ).map(new Flyway(_))

  override def migration: UIO[Unit] = flyway.map(_.migrate())
}

