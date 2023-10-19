package images

import images.api.HttpRoutes
import io.circe.Json
import io.circe.Encoder
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import zio.http.{!!, Body, Request, Response, URL}
import zio.{Chunk, ZLayer}
import zio.http.model.Status
import zio.stream.{ZSink, ZStream}
import zio.test.TestAspect.sequential
import zio.test.{ZIOSpecDefault, assertTrue, suite, test}

import java.io.File
import scala.collection.mutable
import java.nio.file.Files
import scala.io.Source

object ImagesSpec extends ZIOSpecDefault {

  def getFile(filename: String) = new File("images\\src\\test\\resources\\" + filename)

  private val heavyPicture = getFile("heavy_picture.jpg")
  private val textFile = getFile("not_a_picture.txt")
  private val lightPicture = getFile("light_picture.jpg")
  private val bufferFile = getFile("buffer_file.jpg")
  private val nodeId = 42
  def upload(file: File, id: Int = 1) = {
    HttpRoutes.app.runZIO(
      Request.post(
        Body.fromFile(file),
        URL(!! / "upload" / id.toString)
      )
    )
  }

  def download(id: Int = 1) = {
    HttpRoutes.app.runZIO(
      Request.get(
        URL(!! / "download" / id.toString)
      )
    )
  }

  def getData(file: File) = Files.readAllBytes(file.toPath)
  def shouldBeOk(response: Response) = response.status == Status.Ok
  def shouldBeBadRequest(response: Response) = response.status == Status.BadRequest
  def spec = suite("Images tests")(
    test("Shouldn't upload too heavy picture") {
      (for {
        upload_heavy_picture <- upload(heavyPicture)
      } yield {
        assertTrue(shouldBeBadRequest(upload_heavy_picture))
      })
    }, test("Shouldn't upload text file") {
      (for {
        upload_text_file <- upload(textFile)
      } yield {
        assertTrue(shouldBeBadRequest(upload_text_file))
      })
    }, test("Should upload light picture") {
      (for {
        upload_light_picture <- upload(lightPicture, nodeId)
        //FYI:fails while uploading picture for node that already has a picture
      } yield {
        assertTrue(shouldBeOk(upload_light_picture))
      })
    }, test ("Should be able to download picture") {
      (for {
        download_small_picture <- download(nodeId)
        _ <- download_small_picture
          .body
          .asStream
          .run(ZSink.fromFile(bufferFile))
      } yield {
        assertTrue(
          shouldBeOk(download_small_picture)
            && getData(bufferFile) == getData(lightPicture)
        )
      })
    }
  ) @@ sequential
}