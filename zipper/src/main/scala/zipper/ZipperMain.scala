package zipper

import zio._
import zio.kafka.consumer._
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde._
import zio.stream.ZStream

object ZipperMain extends ZIOAppDefault {
  val consumer: ZStream[Consumer, Throwable, Nothing] =
    Consumer
      .plainStream(Subscription.topics("images"), Serde.string, Serde.string)
      .tap(r => Console.printLine(r.value))
      .map(_.offset)
      .aggregateAsync(Consumer.offsetBatches)
      .mapZIO(_.commit)
      .drain

  def consumerLayer =
    ZLayer.scoped(
      Consumer.make(
        ConsumerSettings(List("kafka-zipper:9092")).withGroupId("group")
      )
    )

  override def run = consumer.runDrain.provide(consumerLayer)
}
