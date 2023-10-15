package images

import images.api.HttpRoutes
import java.io.File
import scala.collection.mutable.ListBuffer
import scala.reflect.io.Directory
import zio.{Chunk, ZLayer}
import zio.http.{URL, Body, Request, !!}
import zio.http.model.{Status}
import zio.test.{ZIOSpecDefault, suite, test, assertTrue}

object ImagesSpec extends ZIOSpecDefault {
  val msgJPEG = Chunk[Byte](
    0xff.toByte,
    0xd8.toByte,
    0xff.toByte,
    0x31.toByte,
    0x41.toByte,
    0x59.toByte
  )
  val bodyJPEG = Body.fromChunk(msgJPEG)
  val bodyOther = Body.fromString("")

  (new Directory(new File(HttpRoutes.imageDir))).deleteRecursively()

  def spec =
    suite("Images tests")(
      test("Upload should return Ok if sent JPEG") {
        val imageId = "2"
        for {
          response <- HttpRoutes.app.runZIO(
            Request.post(
              bodyJPEG,
              URL(!! / "upload" / imageId)
            )
          )
        } yield {
          assertTrue(response.status == Status.Ok)
        }
      },
      test("Upload should return BadRequest if sent not JPEG") {
        val imageId = "3"
        for {
          response <- HttpRoutes.app.runZIO(
            Request.post(
              bodyOther,
              URL(!! / "upload" / imageId)
            )
          )
        } yield {
          assertTrue(response.status == Status.BadRequest)
        }
      },
      test("Download should return NotFound if no file exists") {
        val imageId = "4"
        for {
          response <- HttpRoutes.app.runZIO(
            Request.get(URL(!! / "download" / imageId))
          )
        } yield {
          assertTrue(response.status == Status.NotFound)
        }
      },
      test("Download should return same file as uploaded") {
        val imageId = "5"
        for {
          _ <- HttpRoutes.app.runZIO(
            Request.post(
              bodyJPEG,
              URL(!! / "upload" / imageId)
            )
          )
          response <- HttpRoutes.app.runZIO(
            Request.get(URL(!! / "download" / imageId))
          )
          body <- response.body.asChunk
        } yield {
          assertTrue(response.status == Status.Ok)
          assertTrue(body == msgJPEG)
        }
      }
    )
}
