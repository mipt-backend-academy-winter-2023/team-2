package images

import images.api.HttpRoutes
import images.config.ServiceConfig
import zio.http.Server
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.sql.ConnectionPool
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object ImagesMain extends ZIOAppDefault {
  def producerLayer =
    ZLayer.scoped(
      Producer.make(
        settings = ProducerSettings(List("kafka-images:9092"))
      )
    )

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val server =
      for {
        server <- zio.http.Server
          .serve(HttpRoutes.app)
      } yield ()

    server.provide(
      Server.live,
      ServiceConfig.live,
      producerLayer
    )
  }
}
