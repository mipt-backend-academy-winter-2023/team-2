package images

import images.api.HttpRoutes
import images.config.ServiceConfig
import images.zipper.Zipper
import zio.http.Server
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde
import zio.sql.ConnectionPool
import zio.stream.ZStream
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object ImagesMain extends ZIOAppDefault {
  def producerLayer =
    ZLayer.scoped(
      Producer.make(
        settings = ProducerSettings(List("kafka-images-upload:9092"))
      )
    )
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {

    val server =
      for {
        zipper <- Zipper.consumer.fork
        server <- zio.http.Server
          .serve(HttpRoutes.app)
        _ <- zipper.join
      } yield ()

    server.provide(
      Server.live,
      ServiceConfig.live,
      producerLayer
    )
  }
}
