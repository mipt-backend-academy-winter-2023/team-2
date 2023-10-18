package images;

import images.api.HttpRoutes
import zio.{Chunk, ULayer, ZIO, ZLayer}
import zio.http.{!!, Body, Request, Response, URL}
import zio.http.model.Status
import zio.test.{Spec, TestResult, ZIOSpecDefault, assertTrue}

import scala.reflect.io.Directory
import java.io.File

object Images extends ZIOSpecDefault {

  private val bytesArray = Chunk.fromArray(
    Array(
      0xff,
      0xd8,
      0xff,
      0x00
    ).map(_.toByte)
  )

  def simpleImageBody(): Body = {
    Body.fromChunk(bytesArray)
  }

  def largeImageBody(): Body = {
    val targetSize = 20 * 1024 * 1024
    val numberOfCopies =
      math.ceil(targetSize.toDouble / bytesArray.length).toInt
    val largeArray = Array.fill(numberOfCopies)(bytesArray).flatten
    val image: Chunk[Byte] = Chunk.fromArray(largeArray)
    Body.fromChunk(image)
  }

  def upload(
      imageId: String,
      body: Body
  ): ZIO[Any, Option[Response], Response] =
    HttpRoutes.app.runZIO(
      Request.post(
        body,
        URL(!! / "upload" / imageId)
      )
    )

  def download(imageId: String): ZIO[Any, Option[Response], Response] =
    HttpRoutes.app.runZIO(
      Request.get(
        URL(!! / "download" / imageId)
      )
    )

  def assertOk(response: zio.http.Response): TestResult =
    assertTrue(response.status == Status.Ok)

  def assertBadRequest(response: zio.http.Response): TestResult =
    assertTrue(response.status == Status.BadRequest)

  def assertNotFound(response: zio.http.Response): TestResult =
    assertTrue(response.status == Status.NotFound)

  private val mock_imageId1 = "mock-id-1"
  private val mock_imageId2 = "mock-id-2"

  def spec = suite("Images tests")(
    test("Successful upload") {
      for {
        upload_image1 <- upload(mock_imageId1, simpleImageBody())
        upload_image2 <- upload(mock_imageId2, simpleImageBody())
      } yield {
        assertOk(upload_image1)
        assertOk(upload_image2)
      }
    },
    test("Successful download") {
      for {
        upload_image1 <- upload(mock_imageId1, simpleImageBody())
        download_image1_response <- download(mock_imageId1)
        download_image1 <- download_image1_response.body.asChunk
      } yield {
        assertOk(upload_image1)
        assertTrue(download_image1 == bytesArray)
      }
    },
    test("Unsuccessful download") {
      for {
        download_image1 <- download(mock_imageId1)
      } yield {
        assertNotFound(download_image1)
      }
    },
    test("Upload large file") {
      for {
        upload_image1 <- upload(mock_imageId1, largeImageBody())
      } yield {
        assertBadRequest(upload_image1)
      }
    }
  )
}
