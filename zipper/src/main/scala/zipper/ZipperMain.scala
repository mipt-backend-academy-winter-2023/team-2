package zipper

import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.ScaleMethod.Bicubic
import com.sksamuel.scrimage.nio.JpegWriter
import java.io.{File, FileInputStream}
import zio._
import zio.kafka.consumer._
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde._
import zio.stream.ZStream

//import scala.io.Source

object ZipperMain extends ZIOAppDefault {
  val consumer: ZStream[Consumer, Throwable, Nothing] =
    Consumer
      .plainStream(Subscription.topics("images"), Serde.string, Serde.string)
      .map(r => {
        println("\n\n\n\n\n\n\n\n\n")
        println(r.value)
        try {
          val photo = new File(r.value)
          println(photo.length)
          //val image = Image.fromFile(photo)
          //image.scale(0.3).forWriter(JpegWriter.Default).write(new File(r.value + "QQQ"));
          //ImmutableImage.loader().fromFile(photo).scale(0.5, Bicubic) //.output(new File("/var/img/tmp.jpg"))(JpegWriter())
          //ImmutableImage.loader().fromResource(r.value) //.scaleToWidth(640) //.writer(JpegWriter()).write("/var/img/scale_w400.jpg");
          //val fileIterator = Source.fromFile(r.value).getLines()
          val fileIterator = new FileInputStream(photo);
          ImmutableImage.loader().fromStream(fileIterator)
        } catch {
          case e => println(e)
        }
        println("Well... should be written?")
        println("\n\n\n\n\n\n\n\n\n")
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
