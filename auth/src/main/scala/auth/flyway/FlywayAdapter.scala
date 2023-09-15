package auth.flyway

import auth.config.DbConfig
import org.flywaydb.core.Flyway
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
          .configure().locations(s"classpath:db/migration/")
          .dataSource(dbConfig.url, dbConfig.user, dbConfig.password)
      )
      .map(new Flyway(_))
  }

  override def migration: UIO[Unit] =
    flyway.map(_.migrate())
}
