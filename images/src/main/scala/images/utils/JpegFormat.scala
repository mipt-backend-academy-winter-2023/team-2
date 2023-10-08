package images.utils

import zio.{Chunk, ZIO}
import zio.stream.{ZChannel, ZPipeline}

object JpegFormat {

  final val JpegHeader = Chunk[Byte](0xff.toByte, 0xd8.toByte, 0xff.toByte)

  sealed trait Error extends Throwable
  object Error {

    case object FileTooLarge extends Exception("File is too large") with Error
    case object Invalid extends Exception("This is not JPEG") with Error
    case object EmptyStream
      extends Exception("Input stream is empty")
        with Error
  }

  private def aux(
                   buffer: Chunk[Byte]
                 ): ZChannel[Any, Error, Chunk[Byte], Any, Error, Chunk[Byte], Any] = {
    ZChannel
      .readOrFail[Error, Chunk[Byte]](Error.EmptyStream)
      .flatMap { in =>
        val data = buffer ++ in
        if (data.length < JpegHeader.length) {
          aux(data)
        } else if (data.take(JpegHeader.length) == JpegHeader) {
          ZChannel.write(data) *> ZChannel.identity[Error, Chunk[Byte], Any]
        } else {
          ZChannel.fail(Error.Invalid)
        }
      }
  }

  final val validate = ZPipeline.fromChannel(aux(Chunk.empty[Byte]))
  final val cutMaxSize = ZPipeline[Byte].take(JpegFormat.maxSize + 1)

  def checkSize(size: Long): ZIO[Any, Error, Unit] =
    if (size > maxSize) {
      ZIO.fail(Error.FileTooLarge)
    } else {
      ZIO.succeed()
    }
  final val maxSize = 10 * 1024 * 1024
}