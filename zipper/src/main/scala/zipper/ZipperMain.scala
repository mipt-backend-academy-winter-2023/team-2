package zipper

import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.JpegWriter
import java.io.File
import zio._
import zio.kafka.consumer._
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde._
import zio.stream.ZStream

object ZipperMain extends ZIOAppDefault {
  val consumer: ZStream[Consumer, Throwable, Nothing] =
    Consumer
      .plainStream(Subscription.topics("images"), Serde.string, Serde.string)
      .map(r => {
        println(r.value)
        try {
          val photo = new File(r.value)
          println(photo.length)
          val image = Image.fromFile(photo)
          //image.scale(0.3).forWriter(JpegWriter.Default).write(new File(r.value + "QQQ"));
        } catch {
          case e => println(e)
        }
        println("Well... should be written?")
        r
      })
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
