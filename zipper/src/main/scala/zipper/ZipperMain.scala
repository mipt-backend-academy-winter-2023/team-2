package zipper

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import scala.math.sqrt
import zio._
import zio.kafka.consumer._
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde._
import zio.stream.ZStream

//import scala.io.Source

object ZipperMain extends ZIOAppDefault {
  def resizeImage(name: String) = {
    // read image
    val file: File = new File(name)
    val originalImage: BufferedImage = ImageIO.read(file)
    // get new resolution
    val scaleFactor: Float = sqrt(file.length / (3 * 1024 * 1024))
    if (scaleFactor > 1) {
      val newWidth = (originalImage.getWidth() / scaleFactor).toInt
      val newHeight = (originalImage.getHeight() / scaleFactor).toInt
      // resize image
      val resized = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT)
      val bufferedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)
      bufferedImage.getGraphics.drawImage(resized, 0, 0, null)
      // write image
      ImageIO.write(bufferedImage, "JPEG", file)
    }
  }

  val consumer: ZStream[Consumer, Throwable, Nothing] =
    Consumer
      .plainStream(Subscription.topics("images"), Serde.string, Serde.string)
      .map(r => {
        resizeImage(r.value)
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
