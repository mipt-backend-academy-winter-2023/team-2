package images;

import images.api.HttpRoutes
import testutils.TestHelpers._
import zio.http._
import zio.test.{ZIOSpecDefault, assertTrue}
import zio.{Chunk, ZIO}

object ImagesSpec extends ZIOSpecDefault {

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

  def delete(nodeId: String): ZIO[Any, Throwable, Unit] = {
    import java.nio.file.{Files, Paths}

    ZIO.attempt {
      val path = Paths.get(s"./src/images/$nodeId.jpeg")
      if (Files.exists(path)) {
        Files.delete(path)
      } else {
        throw new Exception(s"File with nodeId $nodeId does not exist.")
      }
    }
  }

  def spec = suite("Images tests")(
    test("Successful upload") {
      for {
        upload_image1 <- upload("mock-id-1", simpleImageBody())
        upload_image2 <- upload("mock-id-2", simpleImageBody())
        _ <- delete("mock-id-1")
        _ <- delete("mock-id-2")
      } yield {
        assertOk(upload_image1) &&
        assertOk(upload_image2)
      }
    },
    test("Successful download") {
      for {
        upload_image1 <- upload("mock-id-downloaded", simpleImageBody())
        download_image1_response <- download("mock-id-downloaded")
        download_image1 <- download_image1_response.body.asChunk
        _ <- delete("mock-id-downloaded") // Добавленное удаление
      } yield {
        assertOk(upload_image1) &&
        assertTrue(download_image1 == bytesArray)
      }
    },
    test("Unsuccessful download") {
      for {
        download_image1 <- download("mock-id-3")
      } yield {
        assertNotFound(download_image1)
      }
    },
    test("Upload large file") {
      for {
        upload_image1 <- upload("mock-id-4", largeImageBody())
      } yield {
        assertBadRequest(upload_image1)
      }
    }
  )
}
