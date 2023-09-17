package routing.flyway

import org.flywaydb.core.Flyway
import routing.config.DbConfig
import zio._

object FlywayAdapter {
  trait Service {
    def migration: UIO[Unit]
  }
  val live: ZLayer[DbConfig, Nothing, FlywayAdapter.Service] =
    ZLayer.fromFunction(new FlywayAdapterImpl(_))
}

class FlywayAdapterImpl(dbConfig: DbConfig) extends FlywayAdapter.Service {
  val flyway: UIO[Flyway] = {
    ZIO
      .succeed(
        Flyway
          .configure()
          .locations("classpath:db/migration/")
          .dataSource(dbConfig.url, dbConfig.user, dbConfig.password)
      )
      .map(new Flyway(_))
  }

  override def migration: UIO[Unit] =
    flyway.map(_.migrate())
}
