package images.zipper

import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.ScaleMethod.Bicubic
import com.sksamuel.scrimage.nio.JpegWriter
import zio.{Console, RIO, ZIO}
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.serde.Serde

import java.io.File
object Zipper {
  val consumer: RIO[Any, Unit] =
    Consumer
      .consumeWith(
        settings = ConsumerSettings(List("kafka-images-zipper:9093")),
        subscription = Subscription.topics("images"),
        keyDeserializer = Serde.long,
        valueDeserializer = Serde.string
      )(record => {
        val file = new File(record.value())
        val filesize = file.length()
        val maxSize = 3 * 1024 * 1024
        val factor = 0.95 * maxSize.toFloat / filesize.toFloat
        if (filesize > maxSize) {
          Image.fromFile(file).scale(factor, Bicubic).output(file)(JpegWriter())
          ZIO.logInfo(s"Zipper was triggered on image with id=${record.key()}")
        } else {
          ZIO.succeed(())
        }
      })
}
